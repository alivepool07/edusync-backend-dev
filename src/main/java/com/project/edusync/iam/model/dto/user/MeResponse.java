package com.project.edusync.iam.model.dto.user;

import com.project.edusync.uis.model.dto.AddressDTO;
import com.project.edusync.uis.model.enums.Gender;

import java.time.LocalDate;
import java.util.Set;

public record MeResponse(
        // From iam.model.entity.User
        Long userId,
        String username,
        String email,
        Set<String> roles,

        // From uis.model.entity.UserProfile
        Long profileId,
        String firstName,
        String lastName,
        String preferredName,
        String profileUrl,
        LocalDate dateOfBirth,
        Gender gender,

        // From uis.model.entity.Address
        Set<AddressDTO> addresses
) {}