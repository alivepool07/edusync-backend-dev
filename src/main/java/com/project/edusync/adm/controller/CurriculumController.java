package com.project.edusync.adm.controller;

import com.project.edusync.adm.model.dto.request.CurriculumPeriodsUpdateRequestDto;
import com.project.edusync.adm.model.dto.request.CurriculumSubjectUpsertRequestDto;
import com.project.edusync.adm.model.dto.response.CurriculumClassSubjectResponseDto;
import com.project.edusync.adm.model.dto.response.CurriculumOverviewResponseDto;
import com.project.edusync.adm.service.CurriculumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.url}/auth/curriculum")
@RequiredArgsConstructor
@Tag(name = "Curriculum Management", description = "Manage class-to-subject curriculum definitions and coverage")
public class CurriculumController {

    private final CurriculumService curriculumService;

    @GetMapping("/classes/{classId}")
    @Operation(
            summary = "Get class curriculum",
            description = "Returns all subjects mapped to a class curriculum with configured periods and scheduled periods.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Class curriculum fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Class not found")
    })
    public ResponseEntity<List<CurriculumClassSubjectResponseDto>> getClassCurriculum(@PathVariable UUID classId) {
        return ResponseEntity.ok(curriculumService.getClassCurriculum(classId));
    }

    @PostMapping("/classes/{classId}/subjects")
    @Operation(
            summary = "Add subject to class curriculum",
            description = "Creates a curriculum mapping for a class and subject with periods per week.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subject added to curriculum successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT"),
            @ApiResponse(responseCode = "409", description = "Subject already exists in class curriculum")
    })
    public ResponseEntity<CurriculumClassSubjectResponseDto> addSubjectToClass(
            @PathVariable UUID classId,
            @Valid @RequestBody CurriculumSubjectUpsertRequestDto requestDto) {
        return ResponseEntity.ok(curriculumService.addSubjectToClass(classId, requestDto));
    }

    @PutMapping("/{curriculumMapId}")
    @Operation(
            summary = "Update curriculum periods per week",
            description = "Updates configured periods per week for an existing class-subject curriculum mapping.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Curriculum mapping updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Curriculum mapping not found")
    })
    public ResponseEntity<CurriculumClassSubjectResponseDto> updatePeriodsPerWeek(
            @PathVariable UUID curriculumMapId,
            @Valid @RequestBody CurriculumPeriodsUpdateRequestDto requestDto) {
        return ResponseEntity.ok(curriculumService.updatePeriodsPerWeek(curriculumMapId, requestDto));
    }

    @DeleteMapping("/{curriculumMapId}")
    @Operation(
            summary = "Remove subject from curriculum",
            description = "Deletes a class-subject curriculum mapping.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Curriculum mapping removed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Curriculum mapping not found")
    })
    public ResponseEntity<Void> removeSubjectFromCurriculum(@PathVariable UUID curriculumMapId) {
        curriculumService.removeSubjectFromCurriculum(curriculumMapId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/overview")
    @Operation(
            summary = "Get curriculum coverage overview",
            description = "Returns institution-wide curriculum summary by class, including planned and scheduled coverage.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Curriculum overview fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT")
    })
    public ResponseEntity<List<CurriculumOverviewResponseDto>> getCurriculumOverview() {
        return ResponseEntity.ok(curriculumService.getOverview());
    }
}


