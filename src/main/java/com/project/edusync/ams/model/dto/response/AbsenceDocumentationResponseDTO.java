package com.project.edusync.ams.model.dto.response;

import com.project.edusync.ams.model.enums.ApprovalStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import java.time.LocalDateTime;

/**
 * Full DTO for viewing or reporting on a specific absence documentation record.
 */
@Value
public class AbsenceDocumentationResponseDTO {

    @Deprecated
    @Schema(description = "Deprecated internal documentation id", deprecated = true)
    Long documentationId;

    @Deprecated
    @Schema(description = "Deprecated internal daily attendance id", deprecated = true)
    Long dailyAttendanceId; // Same as documentationId

    @Schema(description = "Documentation UUID", format = "uuid")
    String uuid;

    @Schema(description = "Linked daily attendance UUID", format = "uuid")
    String dailyAttendanceUuid;

    String reasonText;
    String documentationUrl;

    ApprovalStatus approvalStatus;
    String reviewerNotes;

    /** Logical FK to UIS.User.id - The submitter */
    @Schema(description = "Submitter user UUID", format = "uuid")
    String submittedByUserUuid;

    @Deprecated
    Long submittedByUserId;
    String submittedByUserName; // Denormalized name

    /** Logical FK to UIS.Staff.id - The approver */
    @Schema(description = "Approver staff UUID", format = "uuid")
    String approvedByStaffUuid;

    @Deprecated
    Long approvedByStaffId;
    String approvedByStaffName; // Denormalized name

    LocalDateTime createdAt;
}