package com.project.edusync.em.model.dto.ResponseDTO;

import com.project.edusync.em.model.enums.AnnotationType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AnnotationResponseDTO {
    private Long id;
    private Integer pageNumber;
    private Double x;
    private Double y;
    private AnnotationType type;
    private Map<String, Object> metadata;
}

