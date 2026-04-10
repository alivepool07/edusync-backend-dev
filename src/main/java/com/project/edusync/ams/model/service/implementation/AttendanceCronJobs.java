package com.project.edusync.ams.model.service.implementation;

import com.project.edusync.ams.model.entity.AttendanceType;
import com.project.edusync.ams.model.entity.StaffDailyAttendance;
import com.project.edusync.ams.model.entity.StaffShiftMapping;
import com.project.edusync.ams.model.enums.AttendanceSource;
import com.project.edusync.ams.model.repository.AttendanceTypeRepository;
import com.project.edusync.ams.model.repository.StaffDailyAttendanceRepository;
import com.project.edusync.ams.model.repository.StaffShiftMappingRepository;
import com.project.edusync.hrms.model.enums.DayType;
import com.project.edusync.hrms.model.enums.LeaveApplicationStatus;
import com.project.edusync.hrms.repository.AcademicCalendarEventRepository;
import com.project.edusync.hrms.repository.LeaveApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AttendanceCronJobs {

    private static final Set<DayType> NON_WORKING_DAY_TYPES = EnumSet.of(DayType.HOLIDAY, DayType.VACATION);

    private final StaffShiftMappingRepository staffShiftMappingRepository;
    private final StaffDailyAttendanceRepository staffDailyAttendanceRepository;
    private final LeaveApplicationRepository leaveApplicationRepository;
    private final AttendanceTypeRepository attendanceTypeRepository;
    private final AcademicCalendarEventRepository academicCalendarEventRepository;

    /**
     * Nightly batch processor to resolve End-of-Day Attendance exceptions.
     * Runs every day at 11:30 PM (23:30).
     */
    @Scheduled(cron = "0 30 23 * * ?")
    @Transactional
    public void processEndOfDayAttendance() {
        log.info("CronJob: Starting End of Day Staff Attendance checks.");
        LocalDate today = LocalDate.now();

        if (academicCalendarEventRepository.existsByDateAndDayTypeInAndAppliesToStaffTrueAndIsActiveTrue(today, NON_WORKING_DAY_TYPES)) {
            log.info("CronJob: Skipping End of Day Staff Attendance checks due to holiday/vacation calendar event on {}.", today);
            return;
        }

        // Retrieve the standard 'Absent' Attendance Type via predefined ShortCode (often 'A')
        Optional<AttendanceType> absentTypeOpt = attendanceTypeRepository.findByShortCodeIgnoreCase("A");

        if (absentTypeOpt.isEmpty()) {
            log.error("CronJob: Missing AttendanceType with shortCode 'A'. Cannot auto-mark absentees.");
            return;
        }

        AttendanceType absentType = absentTypeOpt.get();

        // 1. Fetch all active shift mappings for today
        List<StaffShiftMapping> activeMappings = staffShiftMappingRepository.findAll().stream()
                .filter(m -> !m.getEffectiveFrom().isAfter(today) && 
                            (m.getEffectiveTo() == null || !m.getEffectiveTo().isBefore(today)))
                .toList();

        for (StaffShiftMapping mapping : activeMappings) {
            Long staffId = mapping.getStaff().getId();

            // 2. Check for overlapping Approved Leaves today
            boolean existsApprovedLeave = leaveApplicationRepository.existsOverlapping(
                    staffId,
                    today,
                    today,
                    List.of(LeaveApplicationStatus.APPROVED)
            );

            if (existsApprovedLeave) {
                log.debug("CronJob: StaffId {} is on approved leave. Skipping absent rule.", staffId);
                continue; // Skip any attendance processing for this employee
            }

            // 3. Evaluate Daily Check-ins
            Optional<StaffDailyAttendance> dailyRecordOpt =
                    staffDailyAttendanceRepository.findByStaffIdAndAttendanceDate(staffId, today);

            if (dailyRecordOpt.isEmpty()) {
                // 3a. NO check-in -> Create an 'Absent' record.
                StaffDailyAttendance absentRecord = new StaffDailyAttendance();
                absentRecord.setStaffId(staffId);
                absentRecord.setAttendanceDate(today);
                absentRecord.setAttendanceType(absentType);
                absentRecord.setSource(AttendanceSource.SYSTEM);
                absentRecord.setNotes("SYSTEM WARNING: Auto-marked as Absent for not checking in.");
                
                staffDailyAttendanceRepository.save(absentRecord);
                log.info("CronJob: Auto-marked StaffId {} as Absent.", staffId);

            } else {
                // 3b. Missing Out-Punch -> Append Warning Exception
                StaffDailyAttendance record = dailyRecordOpt.get();

                if (record.getTimeIn() != null && record.getTimeOut() == null) {
                    String missingSwipeMsg = "SYSTEM WARNING: Missing Out Punch. Requires School Admin Review.";

                    if (record.getNotes() == null || !record.getNotes().contains("Missing Out Punch")) {
                        String newNotes = (record.getNotes() == null || record.getNotes().isBlank())
                                ? missingSwipeMsg
                                : record.getNotes() + " | " + missingSwipeMsg;
                        
                        // Limit size to max length 500 equivalent if needed
                        if (newNotes.length() > 500) {
                            newNotes = newNotes.substring(0, 497) + "...";
                        }
                        
                        record.setNotes(newNotes);
                        staffDailyAttendanceRepository.save(record);
                        log.info("CronJob: Flagged StaffId {} for missing checkout.", staffId);
                    }
                }
            }
        }
        
        log.info("CronJob: Finished End of Day Staff Attendance checks.");
    }
}
