package com.project.edusync.em.model.dto.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaperQuestionMapResponseDTO {
    private Long paperQuestionId;
    private String questionNumber;
    private BigDecimal marksForQuestion;

    // Include Question details for rendering
    private UUID questionUuid;
    private String questionText;
    // You could include more QuestionBankResponseDTO fields here if needed
}