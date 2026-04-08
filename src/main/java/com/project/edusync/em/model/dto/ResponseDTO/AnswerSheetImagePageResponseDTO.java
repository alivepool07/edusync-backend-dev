package com.project.edusync.em.model.dto.ResponseDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnswerSheetImagePageResponseDTO {
    private Integer pageNumber;
    private String imageUrl;
}

