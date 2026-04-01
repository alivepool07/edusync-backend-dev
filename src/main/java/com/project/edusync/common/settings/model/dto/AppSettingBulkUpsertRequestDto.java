package com.project.edusync.common.settings.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AppSettingBulkUpsertRequestDto(
        @NotEmpty List<@Valid AppSettingRequestDto> settings
) {
}

