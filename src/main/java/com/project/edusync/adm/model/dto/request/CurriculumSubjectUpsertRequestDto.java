package com.project.edusync.adm.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CurriculumSubjectUpsertRequestDto {

    @NotNull(message = "Subject ID is required")
    private UUID subjectId;

    @NotNull(message = "periodsPerWeek is required")
    @Min(value = 0, message = "periodsPerWeek must be >= 0")
    @Max(value = 100, message = "periodsPerWeek must be <= 100")
    private Short periodsPerWeek;
}

