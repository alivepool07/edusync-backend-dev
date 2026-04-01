package com.project.edusync.common.settings.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppSettingResponseDto(
        UUID uuid,
        String key,
        String value,
        boolean encrypted,
        String category,
        String description,
        LocalDateTime updatedAt
) {
}

