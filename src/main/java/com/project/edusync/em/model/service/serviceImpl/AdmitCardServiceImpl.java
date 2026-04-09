package com.project.edusync.em.model.service.serviceImpl;

import com.project.edusync.common.exception.emException.EdusyncException;
import com.project.edusync.common.security.AuthUtil;
import com.project.edusync.em.model.dto.ResponseDTO.AdmitCardEntryResponseDTO;
import com.project.edusync.em.model.dto.ResponseDTO.AdmitCardGenerationResponseDTO;
import com.project.edusync.em.model.dto.ResponseDTO.AdmitCardResponseDTO;
import com.project.edusync.em.model.dto.ResponseDTO.ScheduleAdmitCardStatusDTO;
import com.project.edusync.em.model.entity.AdmitCard;
import com.project.edusync.em.model.entity.AdmitCardEntry;
import com.project.edusync.em.model.entity.Exam;
import com.project.edusync.em.model.entity.ExamSchedule;
import com.project.edusync.em.model.entity.SeatAllocation;
import com.project.edusync.em.model.enums.AdmitCardStatus;
import com.project.edusync.em.model.repository.AdmitCardEntryRepository;
import com.project.edusync.em.model.repository.AdmitCardRepository;
import com.project.edusync.em.model.repository.ExamRepository;
import com.project.edusync.em.model.repository.ExamScheduleRepository;
import com.project.edusync.em.model.repository.SeatAllocationRepository;
import com.project.edusync.em.model.service.AdmitCardService;
import com.project.edusync.finance.service.PdfGenerationService;
import com.project.edusync.iam.model.entity.User;
import com.project.edusync.uis.model.entity.Student;
import com.project.edusync.uis.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdmitCardServiceImpl implements AdmitCardService {

    private final AdmitCardRepository admitCardRepository;
    private final AdmitCardEntryRepository admitCardEntryRepository;
    private final ExamRepository examRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final SeatAllocationRepository seatAllocationRepository;
    private final StudentRepository studentRepository;
    private final PdfGenerationService pdfGenerationService;
    private final AuthUtil authUtil;

    @Value("${app.admit-card.storage.private-dir:uploads-private/admit-cards}")
    private String admitCardStorageDir;

    // ═══════════════════════════════════════════════════════════════════
    //  Generate for entire exam (legacy / convenience — calls per-schedule internally)
    // ═══════════════════════════════════════════════════════════════════
    @Override
    public AdmitCardGenerationResponseDTO generateAdmitCards(UUID examUuid) {
        requireAdmin();

        Exam exam = examRepository.findByUuid(examUuid)
                .orElseThrow(() -> new EdusyncException("EM-404", "Exam not found", HttpStatus.NOT_FOUND));

        Long examId = exam.getId();

        List<ExamSchedule> schedules = examScheduleRepository.findByExamIdWithDetails(examId);
        if (schedules.isEmpty()) {
            throw new EdusyncException("EM-400", "No exam schedules found for admit card generation", HttpStatus.BAD_REQUEST);
        }

        int totalGenerated = 0;
        for (ExamSchedule schedule : schedules) {
            AdmitCardGenerationResponseDTO result = doGenerateForSchedule(exam, schedule);
            totalGenerated += result.getGeneratedCount();
        }

        return AdmitCardGenerationResponseDTO.builder()
                .examId(exam.getId())
                .examName(exam.getName())
                .generatedCount(totalGenerated)
                .generatedAt(LocalDateTime.now())
                .message("Admit cards generated for all schedules")
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Generate for a single schedule (the new primary method)
    // ═══════════════════════════════════════════════════════════════════
    @Override
    public AdmitCardGenerationResponseDTO generateAdmitCardsForSchedule(UUID examUuid, Long scheduleId) {
        requireAdmin();

        Exam exam = examRepository.findByUuid(examUuid)
                .orElseThrow(() -> new EdusyncException("EM-404", "Exam not found", HttpStatus.NOT_FOUND));

        Long examId = exam.getId();

        // Load all schedules for the exam (needed to build complete PDF per student)
        List<ExamSchedule> allSchedules = examScheduleRepository.findByExamIdWithDetails(examId);
        ExamSchedule targetSchedule = allSchedules.stream()
                .filter(s -> s.getId().equals(scheduleId))
                .findFirst()
                .orElseThrow(() -> new EdusyncException("EM-404",
                        "Schedule not found or does not belong to this exam", HttpStatus.NOT_FOUND));

        return doGenerateForSchedule(exam, targetSchedule);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Get admit card generation status per schedule
    // ═══════════════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public List<ScheduleAdmitCardStatusDTO> getAdmitCardStatusByExam(UUID examUuid) {
        requireAdmin();

        Exam exam = examRepository.findByUuid(examUuid)
                .orElseThrow(() -> new EdusyncException("EM-404", "Exam not found", HttpStatus.NOT_FOUND));

        Long examId = exam.getId();

        List<ExamSchedule> schedules = examScheduleRepository.findByExamIdWithDetails(examId);
        if (schedules.isEmpty()) {
            return List.of();
        }

        // Get count of generated cards per schedule
        Map<Long, Long> generatedPerSchedule = new HashMap<>();
        List<Object[]> counts = admitCardRepository.countGeneratedPerSchedule(examId);
        for (Object[] row : counts) {
            Long sid = (Long) row[0];
            Long count = (Long) row[1];
            generatedPerSchedule.put(sid, count);
        }

        // Get count of published cards per schedule
        Map<Long, Long> publishedPerSchedule = new HashMap<>();
        List<Object[]> pubCounts = admitCardRepository.countPublishedPerSchedule(examId);
        for (Object[] row : pubCounts) {
            Long sid = (Long) row[0];
            Long count = (Long) row[1];
            publishedPerSchedule.put(sid, count);
        }

        List<ScheduleAdmitCardStatusDTO> statuses = new ArrayList<>();
        for (ExamSchedule schedule : schedules) {
            int totalStudents = countStudentsForSchedule(schedule);
            long generated = generatedPerSchedule.getOrDefault(schedule.getId(), 0L);
            long published = publishedPerSchedule.getOrDefault(schedule.getId(), 0L);

            Optional<LocalDateTime> lastGen = admitCardRepository
                    .findLastGeneratedAtForSchedule(examId, schedule.getId());

            ScheduleAdmitCardStatusDTO dto = ScheduleAdmitCardStatusDTO.builder()
                    .scheduleId(schedule.getId())
                    .className(schedule.getAcademicClass().getName())
                    .sectionName(schedule.getSection() != null ? schedule.getSection().getSectionName() : null)
                    .subjectName(schedule.getSubject().getName())
                    .examDate(schedule.getExamDate().toString())
                    .totalStudents(totalStudents)
                    .generatedCount((int) generated)
                    .allGenerated(generated >= totalStudents && totalStudents > 0)
                    .publishedCount((int) published)
                    .allPublished(published >= totalStudents && totalStudents > 0)
                    .lastGeneratedAt(lastGen.orElse(null))
                    .build();
            statuses.add(dto);
        }

        return statuses;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Core per-schedule generation logic
    // ═══════════════════════════════════════════════════════════════════
    private AdmitCardGenerationResponseDTO doGenerateForSchedule(Exam exam, ExamSchedule targetSchedule) {

        Long examId = exam.getId();

        // Validate the target schedule
        validateSchedules(List.of(targetSchedule));

        // Get students for this schedule's class/section
        List<Student> students = getStudentsForSchedule(targetSchedule);
        if (students.isEmpty()) {
            throw new EdusyncException("EM-400",
                    "No students found for schedule (class: " + targetSchedule.getAcademicClass().getName()
                            + ", subject: " + targetSchedule.getSubject().getName() + ")",
                    HttpStatus.BAD_REQUEST);
        }

        // Fetch seat allocations for the target schedule only
        List<Long> studentIds = students.stream().map(Student::getId).toList();
        Map<String, SeatAllocation> seatAllocationMap = fetchSeatAllocations(
                List.of(targetSchedule), students);

        // Delete old entries for this schedule (not the whole exam!)
        admitCardEntryRepository.deleteByExamScheduleId(targetSchedule.getId());

        // Find or create admit cards for these students
        List<AdmitCard> existingCards = admitCardRepository.findByExamIdAndStudentIds(examId, studentIds);
        Map<Long, AdmitCard> cardsByStudentId = new HashMap<>();
        for (AdmitCard card : existingCards) {
            cardsByStudentId.put(card.getStudent().getId(), card);
        }

        LocalDateTime generatedAt = LocalDateTime.now();
        int generated = 0;

        for (Student student : students) {
            // Get or create admit card for this student
            AdmitCard admitCard = cardsByStudentId.get(student.getId());
            if (admitCard == null) {
                admitCard = AdmitCard.builder()
                        .student(student)
                        .exam(exam)
                        .generatedAt(generatedAt)
                        .status(AdmitCardStatus.DRAFT)
                        .build();
                admitCard = admitCardRepository.save(admitCard);
                cardsByStudentId.put(student.getId(), admitCard);
            } else {
                admitCard.setGeneratedAt(generatedAt);
            }

            // Create the entry for the target schedule
            SeatAllocation allocation = seatAllocationMap.get(
                    allocationKey(targetSchedule.getId(), student.getId()));
            if (allocation == null || allocation.getSeat() == null || allocation.getSeat().getRoom() == null) {
                throw new EdusyncException(
                        "EM-400",
                        "Cannot generate admit cards. Missing seat/room allocation for student "
                                + student.getEnrollmentNumber()
                                + " in subject " + targetSchedule.getSubject().getName(),
                        HttpStatus.BAD_REQUEST);
            }

            AdmitCardEntry entry = AdmitCardEntry.builder()
                    .admitCard(admitCard)
                    .examSchedule(targetSchedule)
                    .subjectId(targetSchedule.getSubject().getId())
                    .subjectName(targetSchedule.getSubject().getName())
                    .examDate(targetSchedule.getExamDate())
                    .startTime(targetSchedule.getTimeslot().getStartTime())
                    .endTime(targetSchedule.getTimeslot().getEndTime())
                    .roomId(allocation.getSeat().getRoom().getId())
                    .roomName(allocation.getSeat().getRoom().getName())
                    .seatId(allocation.getSeat().getId())
                    .seatLabel(allocation.getSeat().getLabel())
                    .build();
            admitCardEntryRepository.save(entry);

            // Rebuild PDF with ALL entries for this student (across all schedules)
            List<AdmitCardEntry> allEntries = admitCardEntryRepository
                    .findByAdmitCardIdWithSchedule(admitCard.getId());
            byte[] pdfBytes = buildAdmitCardPdf(admitCard, allEntries);
            String storedPath = savePdf(admitCard, pdfBytes);
            admitCard.setPdfUrl(storedPath);
            admitCardRepository.save(admitCard);

            generated++;
        }

        return AdmitCardGenerationResponseDTO.builder()
                .examId(exam.getId())
                .examName(exam.getName())
                .generatedCount(generated)
                .generatedAt(generatedAt)
                .message("Admit cards generated for " + targetSchedule.getSubject().getName()
                        + " (" + targetSchedule.getAcademicClass().getName()
                        + (targetSchedule.getSection() != null ? " - " + targetSchedule.getSection().getSectionName() : "")
                        + ")")
                .build();
    }

    @Override
    public int publishAdmitCards(UUID examUuid) {
        requireAdmin();
        Exam exam = examRepository.findByUuid(examUuid)
                .orElseThrow(() -> new EdusyncException("EM-404", "Exam not found", HttpStatus.NOT_FOUND));
        Long examId = exam.getId();

        List<AdmitCard> cards = admitCardRepository.findByExamIdWithStudent(examId);
        if (cards.isEmpty()) {
            throw new EdusyncException("EM-404", "No admit cards found for exam", HttpStatus.NOT_FOUND);
        }

        User publisher = authUtil.getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        cards.forEach(card -> {
            card.setStatus(AdmitCardStatus.PUBLISHED);
            card.setPublishedAt(now);
            card.setPublishedBy(publisher);
        });
        admitCardRepository.saveAll(cards);
        return cards.size();
    }

    @Override
    public int publishAdmitCardsForSchedules(UUID examUuid, List<Long> scheduleIds) {
        requireAdmin();

        if (scheduleIds == null || scheduleIds.isEmpty()) {
            throw new EdusyncException("EM-400", "At least one scheduleId is required", HttpStatus.BAD_REQUEST);
        }

        List<Long> normalizedScheduleIds = scheduleIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (normalizedScheduleIds.isEmpty() || normalizedScheduleIds.stream().anyMatch(id -> id <= 0)) {
            throw new EdusyncException("EM-400", "All scheduleIds must be positive numbers", HttpStatus.BAD_REQUEST);
        }

        Exam exam = examRepository.findByUuid(examUuid)
                .orElseThrow(() -> new EdusyncException("EM-404", "Exam not found", HttpStatus.NOT_FOUND));

        Set<Long> examScheduleIds = examScheduleRepository.findByExamIdWithDetails(exam.getId()).stream()
                .map(ExamSchedule::getId)
                .collect(Collectors.toSet());
        if (examScheduleIds.isEmpty()) {
            throw new EdusyncException("EM-404", "No schedules found for exam", HttpStatus.NOT_FOUND);
        }

        List<Long> invalidScheduleIds = normalizedScheduleIds.stream()
                .filter(id -> !examScheduleIds.contains(id))
                .toList();
        if (!invalidScheduleIds.isEmpty()) {
            throw new EdusyncException(
                    "EM-400",
                    "Invalid scheduleIds for exam: " + invalidScheduleIds,
                    HttpStatus.BAD_REQUEST);
        }

        List<AdmitCard> cards = admitCardRepository.findDraftCardsByExamAndScheduleIds(exam.getId(), normalizedScheduleIds);
        if (cards.isEmpty()) {
            throw new EdusyncException("EM-404", "No draft admit cards found for requested schedules", HttpStatus.NOT_FOUND);
        }

        User publisher = authUtil.getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        cards.forEach(card -> {
            card.setStatus(AdmitCardStatus.PUBLISHED);
            card.setPublishedAt(now);
            card.setPublishedBy(publisher);
        });
        admitCardRepository.saveAll(cards);
        return cards.size();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadAdmitCardsZip(UUID examUuid) {
        requireAdmin();
        Exam exam = examRepository.findByUuid(examUuid)
                .orElseThrow(() -> new EdusyncException("EM-404", "Exam not found", HttpStatus.NOT_FOUND));
        Long examId = exam.getId();
        
        List<AdmitCard> cards = admitCardRepository.findByExamIdWithStudent(examId);
        if (cards.isEmpty()) {
            throw new EdusyncException("EM-404", "No admit cards found for exam", HttpStatus.NOT_FOUND);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (AdmitCard card : cards) {
                if (card.getPdfUrl() == null) {
                    continue;
                }
                Path pdfPath = resolveStoredPath(card.getPdfUrl());
                if (!Files.exists(pdfPath)) {
                    continue;
                }
                String studentCode = card.getStudent().getEnrollmentNumber() != null
                        ? card.getStudent().getEnrollmentNumber()
                        : String.valueOf(card.getStudent().getId());
                String entryName = "exam-" + examId + "/" + studentCode + "-admit-card.pdf";
                zos.putNextEntry(new ZipEntry(entryName));
                zos.write(Files.readAllBytes(pdfPath));
                zos.closeEntry();
            }
            zos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new EdusyncException("EM-500", "Unable to build admit card archive", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AdmitCardResponseDTO getStudentAdmitCard(UUID examUuid) {
        Student student = getCurrentStudent();
        Exam exam = examRepository.findByUuid(examUuid)
                .orElseThrow(() -> new EdusyncException("EM-404", "Exam not found", HttpStatus.NOT_FOUND));
        
        AdmitCard card = admitCardRepository.findByExamIdAndStudentIdWithContext(exam.getId(), student.getId())
                .orElseThrow(() -> new EdusyncException("EM-404", "Admit card not found", HttpStatus.NOT_FOUND));

        if (card.getStatus() != AdmitCardStatus.PUBLISHED) {
            throw new EdusyncException("EM-403", "Admit card is not published yet", HttpStatus.FORBIDDEN);
        }
        return toResponse(card, admitCardEntryRepository.findByAdmitCardIdWithSchedule(card.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadStudentAdmitCardPdf(UUID examUuid) {
        Student student = getCurrentStudent();
        Exam exam = examRepository.findByUuid(examUuid)
                .orElseThrow(() -> new EdusyncException("EM-404", "Exam not found", HttpStatus.NOT_FOUND));
                
        AdmitCard card = admitCardRepository.findByExamIdAndStudentIdWithContext(exam.getId(), student.getId())
                .orElseThrow(() -> new EdusyncException("EM-404", "Admit card not found", HttpStatus.NOT_FOUND));

        if (card.getStatus() != AdmitCardStatus.PUBLISHED) {
            throw new EdusyncException("EM-403", "Admit card is not published yet", HttpStatus.FORBIDDEN);
        }
        if (card.getPdfUrl() == null) {
            throw new EdusyncException("EM-404", "Admit card PDF not found", HttpStatus.NOT_FOUND);
        }

        try {
            Path path = resolveStoredPath(card.getPdfUrl());
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new EdusyncException("EM-404", "Admit card PDF not found", HttpStatus.NOT_FOUND);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new EdusyncException("EM-500", "Unable to load admit card PDF", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Private helpers
    // ═══════════════════════════════════════════════════════════════════

    private List<Student> getStudentsForSchedule(ExamSchedule schedule) {
        if (schedule.getSection() != null) {
            return studentRepository.findBySectionIdOrderByRollNoAsc(schedule.getSection().getId());
        }
        return studentRepository.findBySection_AcademicClass_IdOrderByRollNoAsc(schedule.getAcademicClass().getId());
    }

    private int countStudentsForSchedule(ExamSchedule schedule) {
        return getStudentsForSchedule(schedule).size();
    }

    private void validateSchedules(List<ExamSchedule> schedules) {
        for (ExamSchedule schedule : schedules) {
            if (schedule.getSubject() == null || schedule.getTimeslot() == null || schedule.getExamDate() == null) {
                throw new EdusyncException(
                        "EM-400",
                        "Cannot generate admit cards. Exam schedule is incomplete for schedule id " + schedule.getId(),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    private Map<String, SeatAllocation> fetchSeatAllocations(List<ExamSchedule> schedules, Collection<Student> students) {
        List<Long> scheduleIds = schedules.stream().map(ExamSchedule::getId).toList();
        List<Long> studentIds = students.stream().map(Student::getId).toList();
        List<SeatAllocation> allocations = seatAllocationRepository
                .findByExamScheduleIdsAndStudentIdsWithSeat(scheduleIds, studentIds);

        Map<String, SeatAllocation> map = new HashMap<>();
        for (SeatAllocation allocation : allocations) {
            map.put(allocationKey(allocation.getExamSchedule().getId(), allocation.getStudent().getId()), allocation);
        }
        return map;
    }


    private byte[] buildAdmitCardPdf(AdmitCard card, List<AdmitCardEntry> entries) {
        Student student = card.getStudent();
        String studentName = (student.getUserProfile().getFirstName() + " " + student.getUserProfile().getLastName()).trim();

        List<AdmitCardEntryResponseDTO> rows = entries.stream()
                .map(this::toEntryResponse)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("examName", card.getExam().getName());
        data.put("examType", card.getExam().getExamType() != null ? card.getExam().getExamType().name() : "N/A");
        data.put("studentName", studentName);
        data.put("enrollmentNumber", student.getEnrollmentNumber());
        data.put("generatedAt", card.getGeneratedAt());
        data.put("entries", rows);
        return pdfGenerationService.generatePdfFromHtml("em/admit-card", data);
    }

    private String savePdf(AdmitCard card, byte[] pdfBytes) {
        Path root = ensureStorageRoot();
        String studentCode = card.getStudent().getEnrollmentNumber() != null
                ? card.getStudent().getEnrollmentNumber().replaceAll("[^a-zA-Z0-9_-]", "_")
                : "student-" + card.getStudent().getId();
        Path relative = Paths.get("exam-" + card.getExam().getId(), studentCode + "-admit-card.pdf");
        Path absolute = root.resolve(relative).normalize();

        try {
            Files.createDirectories(absolute.getParent());
            Files.write(absolute, pdfBytes);
            return relative.toString().replace('\\', '/');
        } catch (IOException e) {
            throw new EdusyncException("EM-500", "Unable to store admit card PDF", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Path ensureStorageRoot() {
        Path root = Paths.get(admitCardStorageDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
            return root;
        } catch (IOException e) {
            throw new EdusyncException("EM-500", "Unable to initialize admit card storage", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Path resolveStoredPath(String storedPath) {
        return ensureStorageRoot().resolve(storedPath).normalize();
    }

    private AdmitCardResponseDTO toResponse(AdmitCard card, List<AdmitCardEntry> entries) {
        Student student = card.getStudent();
        String studentName = (student.getUserProfile().getFirstName() + " " + student.getUserProfile().getLastName()).trim();
        return AdmitCardResponseDTO.builder()
                .admitCardId(card.getId())
                .examId(card.getExam().getId())
                .examName(card.getExam().getName())
                .studentId(student.getUuid())
                .studentName(studentName)
                .enrollmentNumber(student.getEnrollmentNumber())
                .generatedAt(card.getGeneratedAt())
                .status(card.getStatus().name())
                .pdfUrl(card.getPdfUrl())
                .publishedBy(card.getPublishedBy() != null ? card.getPublishedBy().getUsername() : null)
                .publishedAt(card.getPublishedAt())
                .entries(entries.stream().map(this::toEntryResponse).collect(Collectors.toList()))
                .build();
    }

    private AdmitCardEntryResponseDTO toEntryResponse(AdmitCardEntry entry) {
        return AdmitCardEntryResponseDTO.builder()
                .examScheduleId(entry.getExamSchedule().getId())
                .subjectId(entry.getSubjectId())
                .subjectName(entry.getSubjectName())
                .examDate(entry.getExamDate())
                .startTime(entry.getStartTime())
                .endTime(entry.getEndTime())
                .roomId(entry.getRoomId())
                .roomName(entry.getRoomName())
                .seatId(entry.getSeatId())
                .seatLabel(entry.getSeatLabel())
                .build();
    }

    private Student getCurrentStudent() {
        Long userId = authUtil.getCurrentUserId();
        return studentRepository.findByUserProfile_User_Id(userId)
                .orElseThrow(() -> new EdusyncException("EM-403", "Current user is not mapped to student", HttpStatus.FORBIDDEN));
    }

    private void requireAdmin() {
        User currentUser = authUtil.getCurrentUser();
        Set<String> roleNames = currentUser.getRoles().stream()
                .map(role -> role.getName() == null ? "" : role.getName().toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
        if (!(roleNames.contains("ROLE_ADMIN")
                || roleNames.contains("ROLE_SCHOOL_ADMIN")
                || roleNames.contains("ROLE_SUPER_ADMIN"))) {
            throw new EdusyncException("EM-403", "Admin privileges required", HttpStatus.FORBIDDEN);
        }
    }

    private String allocationKey(Long scheduleId, Long studentId) {
        return scheduleId + "#" + studentId;
    }
}
