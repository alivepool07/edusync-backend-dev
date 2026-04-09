package com.project.edusync.em.model.dto.ResponseDTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdmitCardGenerationResponseDTO {
    private Long examId;
    private String examName;
    private int generatedCount;
    private LocalDateTime generatedAt;
    private String message;
}

