package com.project.edusync.em.model.dto.ResponseDTO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class EvaluationResultResponseDTO {
    private Long answerSheetId;
    private BigDecimal totalMarks;
    private String status;
    private LocalDateTime evaluatedAt;
}

