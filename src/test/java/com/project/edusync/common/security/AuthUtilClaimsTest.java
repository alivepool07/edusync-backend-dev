package com.project.edusync.common.security;

import com.project.edusync.iam.model.entity.Permission;
import com.project.edusync.iam.model.entity.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthUtilClaimsTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void generatesAndExtractsDashboardClaims() {
        AuthUtil authUtil = new AuthUtil();
        ReflectionTestUtils.setField(authUtil, "secretKey", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(authUtil, "jwtExpirationTime", 3600000L);
        authUtil.init();

        Permission permission = new Permission();
        permission.setName("profile:update:own");

        Role role = new Role();
        role.setName("ROLE_STUDENT");
        role.setPermissions(Set.of(permission));

        Long userId = 42L;
        Long academicYearId = 2026L;
        String token = authUtil.generateAccessToken("student.user", Set.of(role), userId, academicYearId);

        assertEquals(userId, ((Number) authUtil.getClaimValueFromToken(token, "user_id")).longValue());
        assertEquals(academicYearId, ((Number) authUtil.getClaimValueFromToken(token, "academic_year_id")).longValue());

        Set<String> authorities = authUtil.getAuthoritiesFromToken(token)
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertTrue(authorities.contains("ROLE_STUDENT"));
        assertTrue(authorities.contains("profile:update:own"));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("student.user", null, authUtil.getAuthoritiesFromToken(token));
        authentication.setDetails(Map.of("academic_year_id", authUtil.getClaimValueFromToken(token, "academic_year_id")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertEquals(academicYearId, authUtil.getCurrentAcademicYearId());
    }
}
