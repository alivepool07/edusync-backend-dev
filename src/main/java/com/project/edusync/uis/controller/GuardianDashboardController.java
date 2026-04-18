package com.project.edusync.uis.controller;

import com.project.edusync.uis.model.dto.dashboard.IntelligenceResponseDTO;
import com.project.edusync.uis.model.dto.dashboard.OverviewResponseDTO;
import com.project.edusync.uis.service.GuardianDashboardService;
import com.project.edusync.common.security.AuthUtil;
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

import java.util.List;

@RestController
@RequestMapping("${api.url}/guardian/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Guardian Dashboard", description = "Aggregated dashboard for all students linked to the guardian")
public class GuardianDashboardController {
    private final GuardianDashboardService guardianDashboardService;
    private final AuthUtil authUtil;

    @GetMapping("/intelligence")
    @PreAuthorize("hasRole('GUARDIAN')")
    @Operation(summary = "Get dashboard intelligence for all linked students", description = "Returns dashboard intelligence for each student linked to the logged-in guardian.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard intelligence fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<IntelligenceResponseDTO>> getAllLinkedStudentsDashboardIntelligence() {
        Long userId = authUtil.getCurrentUserId();
        Long academicYearId = authUtil.getCurrentAcademicYearId();
        List<IntelligenceResponseDTO> dashboards = guardianDashboardService.getAllLinkedStudentsDashboardIntelligence(userId, academicYearId);
        return ResponseEntity.ok(dashboards);
    }

    @GetMapping("/overview")
    @PreAuthorize("hasRole('GUARDIAN')")
    @Operation(summary = "Get dashboard overview for all linked students", description = "Returns dashboard overview for each student linked to the logged-in guardian.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard overview fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<OverviewResponseDTO>> getAllLinkedStudentsDashboardOverview() {
        Long userId = authUtil.getCurrentUserId();
        Long academicYearId = authUtil.getCurrentAcademicYearId();
        List<OverviewResponseDTO> dashboards = guardianDashboardService.getAllLinkedStudentsDashboardOverview(userId, academicYearId);
        return ResponseEntity.ok(dashboards);
    }
}

