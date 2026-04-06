package com.project.edusync.ams.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for creating a single StudentDailyAttendance record.
 */
@Value
public class StudentAttendanceRequestDTO {

    @Schema(description = "Student public UUID (preferred)", format = "uuid")
    UUID studentUuid;

    /**
     * Legacy fallback for one release cycle.
     */
    @Deprecated
    @Schema(description = "Deprecated legacy student id. Use studentUuid.", deprecated = true)
    Long studentId;

    /**
     * JPA Foreign Key to AMS.AttendanceType.id. Cannot be null.
     */
    @NotBlank(message = "Attendance short code (P, A, L) is required.")
    @Size(min = 1, max = 10, message = "Short code must be between 1 and 10 characters.")
    String attendanceShortCode; // Use P, A, or L

    @Schema(description = "Attendance date", example = "2026-04-06")
    @jakarta.validation.constraints.NotNull(message = "Attendance date is required")
    @PastOrPresent(message = "Attendance date cannot be in the future")
    LocalDate attendanceDate;

    @Schema(description = "Staff public UUID of the attendance taker (preferred)", format = "uuid")
    UUID takenByStaffUuid;

    /**
     * Legacy fallback for one release cycle.
     */
    @Deprecated
    @Schema(description = "Deprecated legacy staff id. Use takenByStaffUuid.", deprecated = true)
    Long takenByStaffId;

    @Size(max = 255, message = "Notes cannot exceed 255 characters")
    String notes;

    @AssertTrue(message = "Either studentUuid or deprecated studentId must be provided")
    private boolean hasStudentIdentifier() {
        return studentUuid != null || studentId != null;
    }

    @AssertTrue(message = "Either takenByStaffUuid or deprecated takenByStaffId must be provided")
    private boolean hasTakenByStaffIdentifier() {
        return takenByStaffUuid != null || takenByStaffId != null;
    }
}