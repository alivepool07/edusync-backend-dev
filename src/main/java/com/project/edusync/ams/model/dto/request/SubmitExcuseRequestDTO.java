package com.project.edusync.ams.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

@Value
public class SubmitExcuseRequestDTO {

    @Schema(description = "Attendance record UUID (preferred)", format = "uuid")
    UUID attendanceUuid;

    @Deprecated
    @Schema(description = "Deprecated legacy attendance id. Use attendanceUuid.", deprecated = true)
    Long attendanceId;

    @Schema(description = "Submitter user UUID (preferred)", format = "uuid")
    UUID submittedByParentUuid;

    @Deprecated
    @Schema(description = "Deprecated legacy submittedByParentId. Use submittedByParentUuid.", deprecated = true)
    Long submittedByParentId;

    @Size(max = 1000, message = "Document URL cannot exceed 1000 chars")
    String documentUrl; // optional pointer to uploaded file

    @Size(max = 1000, message = "Note cannot exceed 1000 chars")
    String note;

    /**
     * Use attendanceDate for verification if you want (optional).
     * If provided, must be PastOrPresent - not future dates.
     */
    @PastOrPresent(message = "Attendance date cannot be in future")
    LocalDate attendanceDate;

    @AssertTrue(message = "Either attendanceUuid or deprecated attendanceId must be provided")
    private boolean hasAttendanceIdentifier() {
        return attendanceUuid != null || attendanceId != null;
    }

    @AssertTrue(message = "Either submittedByParentUuid or deprecated submittedByParentId must be provided")
    private boolean hasSubmitterIdentifier() {
        return submittedByParentUuid != null || submittedByParentId != null;
    }
}
