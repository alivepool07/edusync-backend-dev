package com.project.edusync.em.model.dto.RequestDTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSectionRequestDTO {

    @NotBlank(message = "Section name is required")
    private String sectionName;

    @Min(value = 1, message = "sectionOrder must be at least 1")
    private Integer sectionOrder;

    @Min(value = 1, message = "questionCount must be at least 1")
    private Integer questionCount;

    @Min(value = 1, message = "marksPerQuestion must be at least 1")
    private Integer marksPerQuestion;

    private Boolean isObjective;
    private Boolean isSubjective;
}

