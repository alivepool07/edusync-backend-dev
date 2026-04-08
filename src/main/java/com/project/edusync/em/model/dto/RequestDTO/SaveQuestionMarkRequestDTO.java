package com.project.edusync.em.model.dto.RequestDTO;

import com.project.edusync.em.model.enums.AnnotationType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SaveQuestionMarkRequestDTO {

    @NotBlank(message = "sectionName is required")
    private String sectionName;

    @NotNull(message = "questionNumber is required")
    private Integer questionNumber;

    @NotNull(message = "marksObtained is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "marksObtained cannot be negative")
    private BigDecimal marksObtained;

    private AnnotationType annotationType = AnnotationType.NONE;
}

