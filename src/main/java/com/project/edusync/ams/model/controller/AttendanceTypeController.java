package com.project.edusync.ams.model.controller;

import com.project.edusync.ams.model.dto.request.AttendanceTypeRequestDTO;
import com.project.edusync.ams.model.dto.response.AttendanceTypeResponseDTO;
import com.project.edusync.ams.model.exception.AttendanceTypeInUseException;
import com.project.edusync.ams.model.exception.AttendanceTypeNotFoundException;
import com.project.edusync.ams.model.service.AttendanceTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.url}/auth/ams/types")
@RequiredArgsConstructor
@Tag(name = "AMS Attendance Types", description = "UUID-first attendance type configuration APIs")
public class AttendanceTypeController {

    private final AttendanceTypeService attendanceTypeService;

    /**
     * POST /api/v1/ams/types
     * Creates a new attendance type configuration.
     * Permission: ams:config:create
     */
    @PostMapping
    @Operation(summary = "Create attendance type")
    public ResponseEntity<AttendanceTypeResponseDTO> createType(
            @Valid @RequestBody AttendanceTypeRequestDTO requestDTO) {

        AttendanceTypeResponseDTO response = attendanceTypeService.create(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * GET /api/v1/ams/types
     * Retrieves all active attendance types for dropdowns and UI legends.
     * Permission: ams:config:read (Low-level read, usually public/authenticated)
     */
    @GetMapping
    @Operation(summary = "List active attendance types")
    public ResponseEntity<List<AttendanceTypeResponseDTO>> getAllTypes() {
        List<AttendanceTypeResponseDTO> response = attendanceTypeService.findAllActive();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/ams/types/{typeUuid}
     * Retrieves details for a specific active attendance type.
     * Permission: ams:config:read
     */
    @GetMapping("/{typeUuid}")
    @Operation(summary = "Get attendance type by UUID")
    public ResponseEntity<AttendanceTypeResponseDTO> getTypeById(
            @Parameter(description = "Attendance type UUID", schema = @Schema(format = "uuid"))
            @PathVariable UUID typeUuid) {
        AttendanceTypeResponseDTO response = attendanceTypeService.findByUuid(typeUuid);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/v1/ams/types/{typeUuid}
     * Updates an existing attendance type configuration.
     * Permission: ams:config:update
     */
    @PutMapping("/{typeUuid}")
    @Operation(summary = "Update attendance type by UUID")
    public ResponseEntity<AttendanceTypeResponseDTO> updateType(
            @Parameter(description = "Attendance type UUID", schema = @Schema(format = "uuid"))
            @PathVariable UUID typeUuid,
            @Valid @RequestBody AttendanceTypeRequestDTO requestDTO) {

        AttendanceTypeResponseDTO response = attendanceTypeService.update(typeUuid, requestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/ams/types/{typeUuid}
     * Soft deletes (archives) an attendance type by setting isActive=false.
     * Returns 204 No Content on successful soft deletion.
     * Permission: ams:config:delete
     *
     * Note: We handle the specific exceptions in a centralized @ControllerAdvice
     * but define the expected behavior here.
     */
    @DeleteMapping("/{typeUuid}")
    @Operation(summary = "Delete attendance type by UUID")
    public ResponseEntity<Void> deleteType(
            @Parameter(description = "Attendance type UUID", schema = @Schema(format = "uuid"))
            @PathVariable UUID typeUuid) {
        attendanceTypeService.softDelete(typeUuid);
        return ResponseEntity.noContent().build();
    }

    // --- Centralized Exception Handling Example ---
    // In a professional project, a single @ControllerAdvice component would handle
    // all exceptions across all controllers. Here is an example of what it handles:

    @ExceptionHandler(AttendanceTypeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(AttendanceTypeNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(AttendanceTypeInUseException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict is appropriate when business rules prevent the action
    public String handleInUseException(AttendanceTypeInUseException ex) {
        return ex.getMessage();
    }
}