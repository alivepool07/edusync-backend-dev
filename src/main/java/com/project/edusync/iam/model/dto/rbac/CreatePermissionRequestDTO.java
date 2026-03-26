package com.project.edusync.iam.model.dto.rbac;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePermissionRequestDTO(
        @NotBlank(message = "Permission name is required")
        @Size(max = 100, message = "Permission name must be at most 100 characters")
        @Pattern(
                regexp = "^[a-z]+:[a-z]+:[a-z]+$",
                message = "Permission must follow format domain:action:scope (lowercase)"
        )
        String name
) {
}

