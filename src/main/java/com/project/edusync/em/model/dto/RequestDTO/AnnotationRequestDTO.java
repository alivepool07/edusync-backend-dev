package com.project.edusync.em.model.dto.RequestDTO;

import com.project.edusync.em.model.enums.AnnotationType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class AnnotationRequestDTO {

    @NotNull(message = "pageNumber is required")
    private Integer pageNumber;

    @NotNull(message = "x coordinate is required")
    private Double x;

    @NotNull(message = "y coordinate is required")
    private Double y;

    @NotNull(message = "annotation type is required")
    private AnnotationType type;

    private Map<String, Object> metadata;
}

