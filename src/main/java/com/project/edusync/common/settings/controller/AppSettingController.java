package com.project.edusync.common.settings.controller;

import com.project.edusync.common.settings.model.dto.AppSettingBulkUpsertRequestDto;
import com.project.edusync.common.settings.model.dto.AppSettingRequestDto;
import com.project.edusync.common.settings.model.dto.AppSettingResponseDto;
import com.project.edusync.common.settings.service.AppSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.url}/auth/app-settings")
@RequiredArgsConstructor
@Tag(name = "App Settings", description = "DB-backed runtime application settings")
public class AppSettingController {

    private final AppSettingService appSettingService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SCHOOL_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "List app settings", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<AppSettingResponseDto>> getAll(@RequestParam(defaultValue = "false") boolean revealSecrets) {
        return ResponseEntity.ok(appSettingService.getAllSettings(revealSecrets));
    }

    @GetMapping("/{key}")
    @PreAuthorize("hasAnyAuthority('ROLE_SCHOOL_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Get app setting by key", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AppSettingResponseDto> getByKey(
            @PathVariable String key,
            @RequestParam(defaultValue = "false") boolean revealSecrets) {
        return ResponseEntity.ok(appSettingService.getSetting(key, revealSecrets));
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasAnyAuthority('ROLE_SCHOOL_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Upsert app setting", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Setting saved")
    })
    public ResponseEntity<AppSettingResponseDto> upsert(
            @PathVariable String key,
            @Valid @RequestBody AppSettingRequestDto requestDto) {
        AppSettingRequestDto effective = new AppSettingRequestDto(
                key,
                requestDto.value(),
                requestDto.category(),
                requestDto.description(),
                requestDto.encrypted()
        );
        return ResponseEntity.ok(appSettingService.upsertSetting(effective));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyAuthority('ROLE_SCHOOL_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Bulk upsert app settings", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<AppSettingResponseDto>> bulkUpsert(@Valid @RequestBody AppSettingBulkUpsertRequestDto requestDto) {
        return ResponseEntity.ok(appSettingService.upsertBulk(requestDto.settings()));
    }
}

