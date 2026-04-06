package com.project.edusync.ams.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for presenting StudentDailyAttendance data to the client.
 */
@Value
public class StudentAttendanceResponseDTO {

    @Deprecated
    @Schema(description = "Deprecated internal record id", deprecated = true)
    Long dailyAttendanceId;

    @Schema(description = "Attendance record UUID", format = "uuid")
    String uuid;

    @Schema(description = "Student UUID", format = "uuid")
    String studentUuid;

    /** Logical FK to UIS.Student.id (deprecated for external clients) */
    @Deprecated
    Long studentId;

    /** Denormalized data from UIS for display */
    String studentFullName;

    LocalDate attendanceDate;

    String attendanceTypeShortCode; // e.g., P, A, L

    @Schema(description = "Staff UUID of attendance taker", format = "uuid")
    String takenByStaffUuid;

    /** Logical FK to UIS.Staff.id (deprecated for external clients) */
    @Deprecated
    Long takenByStaffId;

    /** Denormalized data from UIS for display */
    String takenByStaffName;

    /** Nested DTO for the internal AttendanceType */
    AttendanceTypeResponseDTO attendanceType;

    String notes;

    /** To show if an excuse has been submitted */
    AbsenceDocumentationSummaryResponseDTO absenceDocumentation;

    LocalDateTime createdAt;
    String createdBy;
}