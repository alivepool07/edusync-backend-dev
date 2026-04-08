package com.project.edusync.em.model.dto.ResponseDTO;

import com.project.edusync.em.model.enums.AnswerSheetStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AnswerSheetImageGroupResponseDTO {
    private Long answerSheetId;
    private UUID studentId;
    private Long examScheduleId;
    private AnswerSheetStatus status;
    private LocalDateTime updatedAt;
    private List<AnswerSheetImagePageResponseDTO> pages;
}

