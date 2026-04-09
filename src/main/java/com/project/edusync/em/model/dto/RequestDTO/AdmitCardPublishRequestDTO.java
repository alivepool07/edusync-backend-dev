package com.project.edusync.em.model.dto.RequestDTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AdmitCardPublishRequestDTO {

    @NotEmpty(message = "scheduleIds is required")
    private List<@NotNull(message = "scheduleId cannot be null") Long> scheduleIds;
}

