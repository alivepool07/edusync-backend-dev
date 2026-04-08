package com.project.edusync.em.model.dto.RequestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SaveEvaluationMarksRequestDTO {

    @Valid
    @NotEmpty(message = "questionMarks list cannot be empty")
    private List<SaveQuestionMarkRequestDTO> questionMarks;
}

