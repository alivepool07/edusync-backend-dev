package com.project.edusync.common.settings.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppSettingRequestDto(
        @NotBlank @Size(max = 150) String key,
        @NotBlank @Size(max = 4000) String value,
        @Size(max = 100) String category,
        @Size(max = 500) String description,
        Boolean encrypted
) {
}

