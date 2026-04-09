package com.project.edusync.em.model.dto.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResultResponseDTO {
    private Long resultId;
    private Long scheduleId;
    private String examName;
    private String subjectName;
    private BigDecimal marksObtained;
    private Integer maxMarks;
    private LocalDateTime publishedAt;
}

