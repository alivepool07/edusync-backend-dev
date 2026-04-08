package com.project.edusync.em.model.dto.RequestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamTemplateRequestDTO {

    @NotBlank(message = "Template name is required")
    private String name;

    @Valid
    @NotEmpty(message = "At least one section is required")
    private List<TemplateSectionRequestDTO> sections;
}

