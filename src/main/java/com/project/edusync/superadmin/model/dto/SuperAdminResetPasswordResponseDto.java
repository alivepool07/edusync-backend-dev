package com.project.edusync.superadmin.model.dto;

public record SuperAdminResetPasswordResponseDto(
        String message,
        String temporaryPassword
) {
}

