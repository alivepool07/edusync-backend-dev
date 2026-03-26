package com.project.edusync.iam.model.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required.")
        String refreshToken,
        Long academicYearId
) {}