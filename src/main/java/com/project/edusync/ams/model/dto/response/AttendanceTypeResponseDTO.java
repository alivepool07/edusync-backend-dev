package com.project.edusync.ams.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import java.util.UUID;

/**
 * DTO used for server responses (GET, or after POST/PUT) to send AttendanceType details
 * back to the client. Includes read-only, system-managed fields.
 */
@Value
public class AttendanceTypeResponseDTO {

    /** Internal Primary Key */
    @Deprecated
    @Schema(description = "Deprecated internal id", deprecated = true)
    Long id;

    /** External Public Identifier */
    UUID uuid;

    String typeName;
    String shortCode;
    boolean isPresentMark;
    boolean isAbsenceMark;
    boolean isLateMark;
    String colorCode;
}