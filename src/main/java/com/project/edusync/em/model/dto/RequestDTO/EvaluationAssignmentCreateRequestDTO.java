package com.project.edusync.em.model.dto.RequestDTO;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class EvaluationAssignmentCreateRequestDTO {
    @NotNull(message = "examScheduleId is required")
    private Long examScheduleId;

    @NotNull(message = "teacherId is required")
    private UUID teacherId;

    @FutureOrPresent(message = "dueDate must be today or a future date")
    private LocalDate dueDate;
}

