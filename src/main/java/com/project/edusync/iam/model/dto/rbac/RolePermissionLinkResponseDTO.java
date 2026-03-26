package com.project.edusync.iam.model.dto.rbac;

public record RolePermissionLinkResponseDTO(
        Integer roleId,
        String roleName,
        Integer permissionId,
        String permissionName,
        String message
) {
}

