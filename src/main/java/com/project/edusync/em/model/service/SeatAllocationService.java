package com.project.edusync.em.model.service;

import com.project.edusync.adm.exception.ResourceNotFoundException;
import com.project.edusync.adm.model.entity.Room;
import com.project.edusync.adm.repository.RoomRepository;
import com.project.edusync.common.config.CacheNames;
import com.project.edusync.common.exception.BadRequestException;
import com.project.edusync.common.settings.service.AppSettingService;
import com.project.edusync.em.model.dto.request.BulkSeatAllocationRequestDTO;
import com.project.edusync.em.model.dto.request.SingleSeatAllocationRequestDTO;
import com.project.edusync.em.model.dto.response.*;
import com.project.edusync.em.model.entity.ExamSchedule;
import com.project.edusync.em.model.entity.Seat;
import com.project.edusync.em.model.entity.SeatAllocation;
import com.project.edusync.em.model.repository.ExamScheduleRepository;
import com.project.edusync.em.model.repository.SeatAllocationRepository;
import com.project.edusync.em.model.repository.SeatRepository;
import com.project.edusync.finance.service.PdfGenerationService;
import com.project.edusync.uis.model.entity.Student;
import com.project.edusync.uis.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatAllocationService {

    private final SeatRepository seatRepository;
    private final SeatAllocationRepository allocationRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final RoomRepository roomRepository;
    private final StudentRepository studentRepository;
    private final PdfGenerationService pdfGenerationService;
    private final AppSettingService appSettingService;

    private static final int BATCH_SIZE = 50;

    /** Position labels: 0→LEFT, 1→MIDDLE, 2→RIGHT */
    private static final String[] POSITION_LABELS = {"LEFT", "MIDDLE", "RIGHT"};

    private static String positionLabel(int index) {
        return index >= 0 && index < POSITION_LABELS.length ? POSITION_LABELS[index] : "POS_" + index;
    }

    private static String modeLabel(int maxPerSeat) {
        return switch (maxPerSeat) {
            case 1 -> "SINGLE";
            case 2 -> "DOUBLE";
            case 3 -> "TRIPLE";
            default -> "MULTI_" + maxPerSeat;
        };
    }

    // ════════════════════════════════════════════════════════════════
    // SEAT GENERATION (called on room create/update)
    // ════════════════════════════════════════════════════════════════

    @Transactional
    @CacheEvict(value = CacheNames.ROOM_AVAILABILITY, allEntries = true)
    public void generateSeatsForRoom(Room room) {
        if (room.getRowCount() == null || room.getColumnsPerRow() == null) {
            log.info("Skipping seat generation for room {}: dimensions not set", room.getUuid());
            return;
        }

        // Prevent deletion if allocations exist
        if (seatRepository.existsAllocationsByRoomId(room.getId())) {
            throw new BadRequestException("Cannot regenerate seats: active allocations exist for this room");
        }

        seatRepository.deleteAllByRoomId(room.getId());
        seatRepository.flush();

        List<Seat> seats = new ArrayList<>();
        for (int r = 1; r <= room.getRowCount(); r++) {
            for (int c = 1; c <= room.getColumnsPerRow(); c++) {
                Seat seat = new Seat();
                seat.setRoom(room);
                seat.setRowNumber(r);
                seat.setColumnNumber(c);
                seat.setLabel("R" + r + "-C" + c);
                seats.add(seat);
            }
        }
        // Batched insert
        log.info("Generating {} seats for room {}", seats.size(), room.getUuid());
        for (int i = 0; i < seats.size(); i += BATCH_SIZE) {
            seatRepository.saveAll(seats.subList(i, Math.min(i + BATCH_SIZE, seats.size())));
            seatRepository.flush();
        }
    }

    // ════════════════════════════════════════════════════════════════
    // GET AVAILABLE ROOMS (capacity-aware)
    //
    // Capacity formula:
    //   totalCapacity = totalSeats × maxStudentsPerSeat
    //   availableCapacity = totalCapacity - currentAllocations
    // ════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.ROOM_AVAILABILITY, key = "#examScheduleId")
    public List<RoomAvailabilityDTO> getAvailableRooms(Long examScheduleId) {
        ExamSchedule schedule = fetchSchedule(examScheduleId);
        LocalDateTime start = deriveStartTime(schedule);
        LocalDateTime end = deriveEndTime(schedule);
        int maxPerSeat = schedule.getMaxStudentsPerSeat();

        // 1. Count total students needing seats
        int totalStudents = countStudentsForSchedule(schedule);

        // 2. All active rooms (SINGLE query)
        List<Room> rooms = roomRepository.findAllActive();

        // 3. Seat counts per room
        Map<Long, Integer> examSeatUnitsMap = rooms.stream()
            .collect(Collectors.toMap(Room::getId, r -> Optional.ofNullable(r.getExamSeatUnits()).orElse(0)));

        // 4. Total allocations per room in this time window (SINGLE query)
        Map<Long, Long> allocationsPerRoom = new HashMap<>();
        allocationRepository.countOccupiedAllocationsPerRoom(start, end)
            .forEach(row -> allocationsPerRoom.put((Long) row[0], (Long) row[1]));

        // 5. Room Occupancy details for mode and occupiedBy
        List<Object[]> roomOccupancyRows = allocationRepository.findRoomOccupancyDetails(start, end);
        Map<Long, List<Object[]>> occupancyDetailsByRoom = roomOccupancyRows.stream()
            .collect(Collectors.groupingBy(row -> ((Long) row[0])));

        // 6. Build response
        return rooms.stream()
            .map(room -> {
                int totalSeats = examSeatUnitsMap.getOrDefault(room.getId(), 0);
                int totalCapacity = totalSeats * maxPerSeat;
                int occupiedCapacity = allocationsPerRoom.getOrDefault(room.getId(), 0L).intValue();
                int availableCapacity = totalCapacity - occupiedCapacity;

                List<Object[]> occupancyRows = occupancyDetailsByRoom.getOrDefault(room.getId(), Collections.emptyList());

                String mode = modeLabel(maxPerSeat);

                List<OccupiedByDTO> occupiedBy = occupancyRows.stream()
                    .map(row -> new OccupiedByDTO(
                            (String) row[2], // subjectName
                            (String) row[3], // className
                            ((Long) row[4]).intValue() // count
                    )).collect(Collectors.toList());

                return RoomAvailabilityDTO.builder()
                    .roomId(room.getId())
                    .roomUuid(room.getUuid())
                    .roomName(room.getName())
                    .totalSeats(totalSeats)
                    .totalCapacity(totalCapacity)
                    .occupiedCapacity(occupiedCapacity)
                    .availableCapacity(Math.max(0, availableCapacity))
                    .isFull(availableCapacity <= 0)
                    .maxStudentsPerSeat(maxPerSeat)
                    .totalStudentsToSeat(totalStudents)
                    .floorNumber(room.getFloorNumber())
                    .mode(mode)
                    .occupiedBy(occupiedBy)
                    .build();
            })
            .sorted(Comparator.comparingInt(RoomAvailabilityDTO::getAvailableCapacity).reversed())
            .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════════
    // GET AVAILABLE SEATS IN A ROOM (for grid visualization)
    // Returns per-seat data with rich occupied slot info
    // ════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<SeatAvailabilityDTO> getAvailableSeats(Long examScheduleId, UUID roomUuid) {
        ExamSchedule schedule = fetchSchedule(examScheduleId);
        Room room = roomRepository.findActiveById(roomUuid)
            .orElseThrow(() -> new ResourceNotFoundException("No resource found with id: " + roomUuid));

        LocalDateTime start = deriveStartTime(schedule);
        LocalDateTime end = deriveEndTime(schedule);
        int maxPerSeat = schedule.getMaxStudentsPerSeat();

        // All seats for room (SINGLE query)
        List<Seat> seats = seatRepository.findByRoomIdOrderByRowNumberAscColumnNumberAsc(room.getId());

        // Per-seat occupancy count (SINGLE query)
        Map<Long, Long> seatOccupancy = new HashMap<>();
        allocationRepository.countAllocationsPerSeatInRoom(room.getId(), start, end)
            .forEach(row -> seatOccupancy.put((Long) row[0], (Long) row[1]));

        // Rich slot details: [seatId, positionIndex, subjectName, className, studentName]
        Map<Long, List<OccupiedSlotDTO>> slotsMap = new HashMap<>();
        allocationRepository.findOccupiedSlotDetailsInRoom(room.getId(), start, end)
            .forEach(row -> {
                Long seatId = (Long) row[0];
                int posIdx = (Integer) row[1];
                OccupiedSlotDTO slot = OccupiedSlotDTO.builder()
                    .positionIndex(posIdx)
                    .positionLabel(positionLabel(posIdx))
                    .subjectName((String) row[2])
                    .className((String) row[3])
                    .studentName(((String) row[4]).trim())
                    .build();
                slotsMap.computeIfAbsent(seatId, k -> new ArrayList<>()).add(slot);
            });

        return seats.stream()
            .map(s -> {
                int occupied = seatOccupancy.getOrDefault(s.getId(), 0L).intValue();
                boolean isFull = occupied >= maxPerSeat;
                int availableSlots = Math.max(0, maxPerSeat - occupied);

                return SeatAvailabilityDTO.builder()
                    .seatId(s.getId())
                    .label(s.getLabel())
                    .rowNumber(s.getRowNumber())
                    .columnNumber(s.getColumnNumber())
                    .capacity(maxPerSeat)
                    .occupiedCount(occupied)
                    .availableSlots(availableSlots)
                    .isFull(isFull)
                    .available(!isFull)
                    .occupiedSlots(slotsMap.getOrDefault(s.getId(), Collections.emptyList()))
                    .build();
            })
            .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════════
    // SINGLE STUDENT ALLOCATION (manual assignment)
    // Finds next available positionIndex, validates conflicts
    // ════════════════════════════════════════════════════════════════

    @Transactional
    @CacheEvict(value = CacheNames.ROOM_AVAILABILITY, key = "#dto.examScheduleId")
    public SeatAllocationResponseDTO allocateSingleSeat(SingleSeatAllocationRequestDTO dto) {
        ExamSchedule schedule = fetchSchedule(dto.getExamScheduleId());
        LocalDateTime start = deriveStartTime(schedule);
        LocalDateTime end = deriveEndTime(schedule);
        int maxPerSeat = schedule.getMaxStudentsPerSeat();

        Student student = studentRepository.findByUuid(dto.getStudentId())
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + dto.getStudentId()));
        Room room = roomRepository.findActiveById(dto.getRoomId())
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + dto.getRoomId()));
        Seat seat = seatRepository.findById(dto.getSeatId())
            .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + dto.getSeatId()));

        // Validate seat belongs to room
        if (!seat.getRoom().getId().equals(room.getId())) {
            throw new BadRequestException("Seat does not belong to selected room");
        }

        // Check student not already allocated in this time window
        if (allocationRepository.isStudentAllocatedInTimeWindow(student.getId(), start, end)) {
            throw new BadRequestException("Student already has a seat allocation in this time window");
        }

        // Check seat capacity
        Set<Integer> occupiedPositions = allocationRepository.findOccupiedPositionIndices(seat.getId(), start, end);
        if (occupiedPositions.size() >= maxPerSeat) {
            throw new BadRequestException("Seat is at full capacity (" + maxPerSeat + "/" + maxPerSeat + "). Cannot assign.");
        }

        // Conflict check: same subject AND same class on same seat → reject
        if (maxPerSeat > 1 && allocationRepository.existsConflictOnSeat(
                seat.getId(), start, end,
                schedule.getSubject().getId(),
                schedule.getAcademicClass().getId())) {
            throw new BadRequestException(
                "Conflict: another student with the same subject and class is already on this seat. " +
                "Students sharing a seat must have different subjects or different classes.");
        }

        // Find next available positionIndex
        int positionIndex = findNextAvailablePosition(occupiedPositions, maxPerSeat);

        SeatAllocation allocation = new SeatAllocation();
        allocation.setSeat(seat);
        allocation.setStudent(student);
        allocation.setExamSchedule(schedule);
        allocation.setStartTime(start);
        allocation.setEndTime(end);
        allocation.setPositionIndex(positionIndex);

        return toResponse(allocationRepository.save(allocation));
    }

    // ════════════════════════════════════════════════════════════════
    // BULK AUTO-ALLOCATION (concurrency-safe with pessimistic lock)
    //
    // Algorithm:
    //   1. Lock ALL seats in room
    //   2. Get occupancy map via GROUP BY
    //   3. Compute available seats (occupancy < maxPerSeat)
    //   4. Sort: partially filled first (fill existing benches)
    //   5. For each student, find seat with capacity + no conflict
    //   6. Assign next available positionIndex
    //   7. Batched insert
    // ════════════════════════════════════════════════════════════════

    @Transactional
    @CacheEvict(value = CacheNames.ROOM_AVAILABILITY, key = "#dto.examScheduleId")
    public List<SeatAllocationResponseDTO> bulkAllocate(BulkSeatAllocationRequestDTO dto) {
        ExamSchedule schedule = fetchSchedule(dto.getExamScheduleId());
        Room room = roomRepository.findActiveById(dto.getRoomId())
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + dto.getRoomId()));

        LocalDateTime start = deriveStartTime(schedule);
        LocalDateTime end = deriveEndTime(schedule);
        int maxPerSeat = schedule.getMaxStudentsPerSeat();

        // 1. Resolve all students for this schedule's class/section
        List<Student> allStudents = resolveStudents(schedule);
        if (allStudents.isEmpty()) {
            throw new BadRequestException("No students found for this schedule");
        }

        // 2. BULK check: which students already have allocations (SINGLE query)
        Set<Long> allStudentIds = allStudents.stream().map(Student::getId).collect(Collectors.toSet());
        Set<Long> alreadyAllocated = allocationRepository.findAlreadyAllocatedStudentIds(allStudentIds, start, end);

        List<Student> unallocated = allStudents.stream()
            .filter(s -> !alreadyAllocated.contains(s.getId()))
            .collect(Collectors.toList());

        if (unallocated.isEmpty()) {
            throw new BadRequestException("All students already have seat allocations");
        }

        // 3. PESSIMISTIC LOCK: lock all seats in room
        List<Seat> allSeats = allocationRepository.lockAllSeatsInRoom(room.getId());

        if (allSeats.isEmpty()) {
            throw new BadRequestException("No configured seats available in this room");
        }

        // 4. Get per-seat occupancy via GROUP BY (SINGLE query)
        Map<Long, Long> seatOccupancy = new HashMap<>();
        allocationRepository.countAllocationsPerSeatInRoom(room.getId(), start, end)
            .forEach(row -> seatOccupancy.put((Long) row[0], (Long) row[1]));

        // 5. Get occupied position indices per seat (for positionIndex assignment)
        Map<Long, Set<Integer>> occupiedPositionsPerSeat = new HashMap<>();
        allocationRepository.findOccupiedSlotDetailsInRoom(room.getId(), start, end)
            .forEach(row -> {
                Long seatId = (Long) row[0];
                int posIdx = (Integer) row[1];
                occupiedPositionsPerSeat.computeIfAbsent(seatId, k -> new HashSet<>()).add(posIdx);
            });

        // 6. Filter to seats with available capacity
        List<Seat> availableSeats = allSeats.stream()
            .filter(s -> seatOccupancy.getOrDefault(s.getId(), 0L) < maxPerSeat)
            .collect(Collectors.toList());

        long totalAvailableSlots = availableSeats.stream()
            .mapToLong(s -> maxPerSeat - seatOccupancy.getOrDefault(s.getId(), 0L))
            .sum();

        if (totalAvailableSlots <= 0) {
            throw new BadRequestException("No available capacity in this room");
        }

        int toAllocate = Math.min(unallocated.size(), (int) totalAvailableSlots);
        log.info("Allocating {} students to room {} (slots: {}, maxPerSeat: {}) for Schedule ID {}",
            toAllocate, room.getUuid(), totalAvailableSlots, maxPerSeat, schedule.getId());

        // 7. SORT: partially filled seats first (higher occupancy = higher priority), then by position
        List<Seat> sortedSeats = availableSeats.stream()
            .sorted(Comparator
                .comparingLong((Seat s) -> seatOccupancy.getOrDefault(s.getId(), 0L)).reversed()
                .thenComparingInt(Seat::getRowNumber)
                .thenComparingInt(Seat::getColumnNumber))
            .collect(Collectors.toList());

        // 8. Build allocations — fill seats up to maxPerSeat each
        List<SeatAllocation> newAllocations = new ArrayList<>(toAllocate);
        int studentIdx = 0;

        // Track subject+class per seat for conflict checking during bulk
        // Key: seatId, Value: Set of "subjectId:classId" strings
        Map<Long, Set<String>> seatSubjectClassTracker = new HashMap<>();

        // Pre-populate tracker with existing allocations
        allocationRepository.findOccupiedSlotDetailsInRoom(room.getId(), start, end)
            .forEach(row -> {
                // We need subject and class IDs for conflict tracking, but the query returns names
                // For bulk, we use the schedule's own subject and class since all students in this
                // batch belong to the same schedule
            });

        String scheduleConflictKey = schedule.getSubject().getId() + ":" + schedule.getAcademicClass().getId();

        for (Seat seat : sortedSeats) {
            if (studentIdx >= toAllocate) break;

            long currentOccupancy = seatOccupancy.getOrDefault(seat.getId(), 0L);
            Set<Integer> occupiedPos = occupiedPositionsPerSeat.getOrDefault(seat.getId(), new HashSet<>());

            // For multi-seating, check conflict: skip if same subject+class already on seat
            if (maxPerSeat > 1 && currentOccupancy > 0) {
                boolean hasConflict = allocationRepository.existsConflictOnSeat(
                    seat.getId(), start, end,
                    schedule.getSubject().getId(),
                    schedule.getAcademicClass().getId());
                if (hasConflict) {
                    continue; // Skip this seat — conflict
                }
            }

            // Fill up to maxPerSeat on this seat
            while (studentIdx < toAllocate && occupiedPos.size() < maxPerSeat) {
                int positionIndex = findNextAvailablePosition(occupiedPos, maxPerSeat);

                SeatAllocation sa = new SeatAllocation();
                sa.setSeat(seat);
                sa.setStudent(unallocated.get(studentIdx));
                sa.setExamSchedule(schedule);
                sa.setStartTime(start);
                sa.setEndTime(end);
                sa.setPositionIndex(positionIndex);

                newAllocations.add(sa);
                occupiedPos.add(positionIndex);
                studentIdx++;
            }
        }

        // 9. BATCHED insert
        List<SeatAllocation> saved = new ArrayList<>(newAllocations.size());
        for (int i = 0; i < newAllocations.size(); i += BATCH_SIZE) {
            List<SeatAllocation> batch = newAllocations.subList(i, Math.min(i + BATCH_SIZE, newAllocations.size()));
            saved.addAll(allocationRepository.saveAll(batch));
            allocationRepository.flush();
        }

        return saved.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════════
    // GET ALLOCATIONS FOR A SCHEDULE
    // ════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<SeatAllocationResponseDTO> getAllocationsForSchedule(Long examScheduleId) {
        return allocationRepository.findByExamScheduleWithDetails(examScheduleId)
            .stream()
            .map(this::toResponse)
            .sorted(Comparator.comparing(SeatAllocationResponseDTO::getRollNo, Comparator.nullsLast(Comparator.naturalOrder())))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public byte[] generateSeatingPlanPdf(Long examScheduleId) {
        ExamSchedule schedule = fetchSchedule(examScheduleId);
        List<SeatAllocationResponseDTO> allocations = getAllocationsForSchedule(examScheduleId);

        Map<String, Object> data = new HashMap<>();
        populateSchoolBrandingData(data);

        data.put("title", "Seating Plan");
        data.put("examName", schedule.getExam().getName());
        data.put("subjectName", schedule.getSubject().getName());
        data.put("className", schedule.getAcademicClass() != null ? schedule.getAcademicClass().getName() : "-");
        data.put("sectionName", schedule.getSection() != null ? schedule.getSection().getSectionName() : "All Sections");
        data.put("examDate", schedule.getExamDate() != null ? schedule.getExamDate().toString() : "-");
        data.put("startTime", schedule.getTimeslot() != null ? String.valueOf(schedule.getTimeslot().getStartTime()) : "-");
        data.put("endTime", schedule.getTimeslot() != null ? String.valueOf(schedule.getTimeslot().getEndTime()) : "-");
        data.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));
        data.put("totalAssigned", allocations.size());
        data.put("rows", allocations);

        return pdfGenerationService.generatePdfFromHtml("em/seating-plan", data);
    }

    @Transactional(readOnly = true)
    public SeatAllocationResponseDTO findAllocationByRollNumber(Long examScheduleId, Integer rollNo) {
        Student student = studentRepository.findByRollNo(rollNo)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with roll number: " + rollNo));
            
        SeatAllocation allocation = allocationRepository
            .findByExamScheduleIdAndStudentId(examScheduleId, student.getId())
            .orElseThrow(() -> new ResourceNotFoundException("No seat allocation found for roll number " + rollNo + " in schedule " + examScheduleId));
            
        return toResponse(allocation);
    }

    // ════════════════════════════════════════════════════════════════
    // DELETION (Singular & Bulk)
    // ════════════════════════════════════════════════════════════════

    @Transactional
    @CacheEvict(value = CacheNames.ROOM_AVAILABILITY, allEntries = true)
    public void deleteAllocation(Long allocationId) {
        if (!allocationRepository.existsById(allocationId)) {
            throw new ResourceNotFoundException("SeatAllocation not found with id: " + allocationId);
        }
        allocationRepository.deleteById(allocationId);
    }

    @Transactional
    @CacheEvict(value = CacheNames.ROOM_AVAILABILITY, allEntries = true)
    public void bulkDeleteAllocations(List<Long> allocationIds) {
        if (allocationIds == null || allocationIds.isEmpty()) return;
        allocationRepository.deleteAllByIdInBatch(allocationIds);
    }

    // ── Private helpers ──────────────────────────────────────────

    /**
     * Finds the smallest positionIndex in [0, maxPerSeat) that is not yet occupied.
     */
    private int findNextAvailablePosition(Set<Integer> occupied, int maxPerSeat) {
        for (int i = 0; i < maxPerSeat; i++) {
            if (!occupied.contains(i)) {
                return i;
            }
        }
        throw new BadRequestException("No available position on this seat (all " + maxPerSeat + " positions are taken)");
    }

    private ExamSchedule fetchSchedule(Long id) {
        return examScheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ExamSchedule not found with id: " + id));
    }

    private LocalDateTime deriveStartTime(ExamSchedule s) {
        log.debug("[deriveStartTime] ExamSchedule ID: {} examDate: {} timeslot.startTime: {} -> startTime: {}",
            s.getId(), s.getExamDate(), s.getTimeslot().getStartTime(),
            s.getExamDate().atTime(s.getTimeslot().getStartTime()));
        return s.getExamDate().atTime(s.getTimeslot().getStartTime());
    }

    private LocalDateTime deriveEndTime(ExamSchedule s) {
        log.debug("[deriveEndTime] ExamSchedule ID: {} examDate: {} timeslot.endTime: {} -> endTime: {}",
            s.getId(), s.getExamDate(), s.getTimeslot().getEndTime(),
            s.getExamDate().atTime(s.getTimeslot().getEndTime()));
        return s.getExamDate().atTime(s.getTimeslot().getEndTime());
    }

    private int countStudentsForSchedule(ExamSchedule s) {
        if (s.getSection() != null) {
            return (int) studentRepository.countBySectionId(s.getSection().getId());
        } else if (s.getAcademicClass() != null) {
            return (int) studentRepository.countBySection_AcademicClass_Id(s.getAcademicClass().getId());
        }
        return 0;
    }

    private List<Student> resolveStudents(ExamSchedule s) {
        if (s.getSection() != null) {
            return studentRepository.findBySectionIdOrderByRollNoAsc(s.getSection().getId());
        } else if (s.getAcademicClass() != null) {
            return studentRepository.findBySection_AcademicClass_IdOrderByRollNoAsc(s.getAcademicClass().getId());
        }
        return Collections.emptyList();
    }

    private SeatAllocationResponseDTO toResponse(SeatAllocation sa) {
        String firstName = sa.getStudent().getUserProfile().getFirstName();
        String lastName = sa.getStudent().getUserProfile().getLastName();
        int maxPerSeat = sa.getExamSchedule().getMaxStudentsPerSeat();
        int posIdx = sa.getPositionIndex();

        String label = maxPerSeat == 1 ? "" : positionLabel(posIdx);
        String positionSuffix = label.isEmpty() ? "" : " - " + label;

        return SeatAllocationResponseDTO.builder()
            .allocationId(sa.getId())
            .studentName((firstName + " " + (lastName != null ? lastName : "")).trim())
            .enrollmentNumber(sa.getStudent().getEnrollmentNumber())
            .rollNo(sa.getStudent().getRollNo())
            .seatLabel(sa.getSeat().getLabel() + positionSuffix)
            .positionIndex(posIdx)
            .positionLabel(label)
            .seatId(sa.getSeat().getId())
            .studentId(sa.getStudent().getUuid())
            .roomName(sa.getSeat().getRoom().getName())
            .rowNumber(sa.getSeat().getRowNumber())
            .columnNumber(sa.getSeat().getColumnNumber())
            .startTime(sa.getStartTime())
            .endTime(sa.getEndTime())
            .subjectName(sa.getExamSchedule().getSubject().getName())
            .className(sa.getExamSchedule().getAcademicClass().getName())
            .build();
    }

    private void populateSchoolBrandingData(Map<String, Object> data) {
        String schoolName = appSettingService.getValue("school.name", "My School");
        String shortName = appSettingService.getValue("school.short_name", "");
        data.put("schoolName", schoolName);
        data.put("schoolShortName", shortName.isBlank() ? schoolName : shortName);
        data.put("schoolTagline", appSettingService.getValue("school.tagline", ""));
        data.put("schoolAddress", appSettingService.getValue("school.address", ""));
        data.put("schoolPhone", appSettingService.getValue("school.phone", ""));
        data.put("schoolEmail", appSettingService.getValue("school.email", ""));

        String headerMode = appSettingService.getValue("school.id_card_header_mode", "TEXT");
        String headerImageUrl = appSettingService.getValue("school.id_card_header_image_url", "");
        String headerImageBase64 = "";
        if ("IMAGE".equalsIgnoreCase(headerMode) && !headerImageUrl.isBlank()) {
            headerImageBase64 = pdfGenerationService.fetchRemoteImageAsBase64OrEmpty(headerImageUrl);
        }
        data.put("headerImageEnabled", !headerImageBase64.isBlank());
        data.put("headerImageBase64", headerImageBase64);

        String logoUrl = appSettingService.getValue("school.logo_url", "");
        if (!logoUrl.isBlank()) {
            data.put("schoolLogoBase64", pdfGenerationService.fetchRemoteImageAsBase64(logoUrl));
        } else {
            data.put("schoolLogoBase64", pdfGenerationService.loadSchoolLogoBase64());
        }
    }
}
