package com.project.edusync.em.model.dto.RequestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class AdmitCardGenerateRequestDTO {

    @NotNull(message = "examId is required")
    private UUID examId;

    // Optional: when provided, generate only for this specific schedule's students
    private Long scheduleId;
}
