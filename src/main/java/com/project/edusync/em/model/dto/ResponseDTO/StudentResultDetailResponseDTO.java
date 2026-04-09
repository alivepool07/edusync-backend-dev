package com.project.edusync.em.model.dto.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResultDetailResponseDTO {
    private Long resultId;
    private Long scheduleId;
    private String examName;
    private String subjectName;
    private LocalDate examDate;
    private BigDecimal totalMarks;
    private Integer maxMarks;
    private LocalDateTime publishedAt;
    private List<SubjectMarkDTO> subjectMarks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectMarkDTO {
        private String subjectName;
        private BigDecimal marksObtained;
        private Integer maxMarks;
    }
}

