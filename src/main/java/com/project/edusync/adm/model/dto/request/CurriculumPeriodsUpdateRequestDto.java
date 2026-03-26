package com.project.edusync.adm.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CurriculumPeriodsUpdateRequestDto {

    @NotNull(message = "periodsPerWeek is required")
    @Min(value = 0, message = "periodsPerWeek must be >= 0")
    @Max(value = 100, message = "periodsPerWeek must be <= 100")
    private Short periodsPerWeek;
}

