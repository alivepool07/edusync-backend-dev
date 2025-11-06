package com.project.edusync.em.model.dto.RequestDTO;


import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Nested DTO for a single rule within a GradeSystem.
 * This is not expected to be created on its own.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeScaleRequestDTO {
    @NotBlank(message = "Grade name is required")
    private String gradeName;

    @NotNull(message = "Minimum percentage is required")
    @DecimalMin(value = "0.0", message = "Minimum percentage cannot be less than 0")
    @DecimalMax(value = "100.0", message = "Minimum percentage cannot be more than 100")
    private BigDecimal minPercentage;

    @NotNull(message = "Maximum percentage is required")
    @DecimalMin(value = "0.0", message = "Maximum percentage cannot be less than 0")
    @DecimalMax(value = "100.0", message = "Maximum percentage cannot be more than 100")
    private BigDecimal maxPercentage;

    @DecimalMin(value = "0.0", message = "Grade points cannot be negative")
    private BigDecimal gradePoints;

}