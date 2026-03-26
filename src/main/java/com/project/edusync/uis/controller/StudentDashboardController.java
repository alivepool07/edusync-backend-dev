package com.project.edusync.uis.controller;

import com.project.edusync.common.security.AuthUtil;
import com.project.edusync.uis.model.dto.dashboard.IntelligenceResponseDTO;
import com.project.edusync.uis.model.dto.dashboard.OverviewResponseDTO;
import com.project.edusync.uis.service.DashboardAggregatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.url}/student/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Student Dashboard Intelligence", description = "High-performance aggregated intelligence view for the logged-in student")
public class StudentDashboardController {

    private final DashboardAggregatorService dashboardAggregatorService;
    private final AuthUtil authUtil;

    @GetMapping("/intelligence")
    @PreAuthorize("hasAuthority('profile:read:own')")
    @Operation(
            summary = "Get Student Intelligence Dashboard",
            description = "Returns profile, live academic context, predictive attendance, financial summary, and recent activity for the authenticated student.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard intelligence fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Forbidden - missing profile:read:own authority"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ResponseEntity<IntelligenceResponseDTO> getDashboardIntelligence() {
        long startedAt = System.nanoTime();
        Long userId = authUtil.getCurrentUserId();
        Long academicYearId = authUtil.getCurrentAcademicYearId();
        log.info("Dashboard intelligence request received for userId={} academicYearId={}", userId, academicYearId);

        IntelligenceResponseDTO response = dashboardAggregatorService.getDashboardIntelligence(userId, academicYearId);

        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
        log.info("Dashboard intelligence request completed for userId={} in {} ms", userId, elapsedMs);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('dashboard:read:own')")
    @Operation(
            summary = "Get Student Dashboard Overview",
            description = "Returns profile, KPI snapshot, today's schedule, pending assignments, performance trend, and recent announcements for the authenticated student.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard overview fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Forbidden - missing dashboard:read:own authority"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ResponseEntity<OverviewResponseDTO> getDashboardOverview() {
        long startedAt = System.nanoTime();
        Long userId = authUtil.getCurrentUserId();
        Long academicYearId = authUtil.getCurrentAcademicYearId();
        log.info("Dashboard overview request received for userId={} academicYearId={}", userId, academicYearId);

        OverviewResponseDTO response = dashboardAggregatorService.getDashboardOverview(userId, academicYearId);

        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
        log.info("Dashboard overview request completed for userId={} in {} ms", userId, elapsedMs);
        return ResponseEntity.ok(response);
    }
}

