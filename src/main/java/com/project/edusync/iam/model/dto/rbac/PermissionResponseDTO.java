package com.project.edusync.iam.model.dto.rbac;

public record PermissionResponseDTO(
        Integer id,
        String name,
        boolean active
) {
}

