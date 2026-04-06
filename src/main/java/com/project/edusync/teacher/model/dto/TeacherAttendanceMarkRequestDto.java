package com.project.edusync.teacher.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

@Value
public class TeacherAttendanceMarkRequestDto {

    @NotNull(message = "studentUuid is required")
    @Schema(description = "Student UUID", format = "uuid")
    UUID studentUuid;

    @NotNull(message = "date is required")
    @Schema(description = "Attendance date", example = "2026-04-06")
    LocalDate date;

    @NotNull(message = "present is required")
    Boolean present;

    @NotNull(message = "recordedBy is required")
    String recordedBy;
}


