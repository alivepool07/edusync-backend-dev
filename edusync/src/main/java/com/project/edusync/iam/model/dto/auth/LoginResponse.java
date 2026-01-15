package com.project.edusync.iam.model.dto.auth;

import com.project.edusync.iam.model.dto.user.UserDetailsDto;

import java.util.Set;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UserDetailsDto userDetailsDto,
        Set<String> roles,
        boolean requiresPasswordChange
) {}