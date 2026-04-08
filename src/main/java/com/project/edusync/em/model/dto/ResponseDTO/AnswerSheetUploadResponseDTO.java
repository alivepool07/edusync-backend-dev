package com.project.edusync.em.model.dto.ResponseDTO;

import com.project.edusync.em.model.enums.AnswerSheetStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnswerSheetUploadResponseDTO {
    private Long answerSheetId;
    private String fileUrl;
    private AnswerSheetStatus status;
    private LocalDateTime createdAt;
}

