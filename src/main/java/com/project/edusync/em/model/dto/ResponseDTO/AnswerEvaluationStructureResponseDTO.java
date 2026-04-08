package com.project.edusync.em.model.dto.ResponseDTO;

import com.project.edusync.em.model.enums.AnnotationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerEvaluationStructureResponseDTO {
    private Long answerSheetId;
    private Integer totalQuestions;
    private Integer totalMaxMarks;
    private String resultStatus;
    private List<SectionDTO> sections;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectionDTO {
        private String sectionName;
        private List<QuestionDTO> questions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDTO {
        private Integer questionNumber;
        private BigDecimal maxMarks;
        private BigDecimal marksObtained;
        private AnnotationType annotationType;
    }
}

