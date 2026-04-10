package com.project.edusync.ams.model.controller;

import com.project.edusync.ams.model.dto.request.BulkStaffShiftMapRequestDTO;
import com.project.edusync.ams.model.dto.request.ShiftCreateDTO;
import com.project.edusync.ams.model.dto.request.StaffShiftMapRequestDTO;
import com.project.edusync.ams.model.dto.response.ShiftMappingResultDTO;
import com.project.edusync.ams.model.dto.response.ShiftResponseDTO;
import com.project.edusync.ams.model.dto.response.StaffShiftMappingResponseDTO;
import com.project.edusync.ams.model.service.ShiftManagementService;
import com.project.edusync.uis.model.enums.StaffCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.url:/api/v1}/auth/ams/shifts")
@RequiredArgsConstructor
@Tag(name = "AMS Shifts", description = "Shift definitions and staff-shift mappings")
public class ShiftManagementController {

    private final ShiftManagementService shiftManagementService;

    @GetMapping
    @Operation(summary = "List all shift definitions")
    public ResponseEntity<List<ShiftResponseDTO>> list() {
        return ResponseEntity.ok(shiftManagementService.listShifts());
    }

    @PostMapping
    @Operation(summary = "Create a shift definition")
    public ResponseEntity<ShiftResponseDTO> create(@Valid @RequestBody ShiftCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shiftManagementService.createShift(request));
    }

    @GetMapping("/{shiftUuid}")
    @Operation(summary = "Get shift by UUID")
    public ResponseEntity<ShiftResponseDTO> get(@PathVariable UUID shiftUuid) {
        return ResponseEntity.ok(shiftManagementService.getShift(shiftUuid));
    }

    @PutMapping("/{shiftUuid}")
    @Operation(summary = "Update shift by UUID")
    public ResponseEntity<ShiftResponseDTO> update(@PathVariable UUID shiftUuid, @Valid @RequestBody ShiftCreateDTO request) {
        return ResponseEntity.ok(shiftManagementService.updateShift(shiftUuid, request));
    }

    @DeleteMapping("/{shiftUuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft delete shift")
    public void delete(@PathVariable UUID shiftUuid) {
        shiftManagementService.deleteShift(shiftUuid);
    }

    @PostMapping("/map")
    @Operation(summary = "Map single staff to a shift")
    public ResponseEntity<ShiftMappingResultDTO> mapSingle(@Valid @RequestBody StaffShiftMapRequestDTO request) {
        return ResponseEntity.ok(shiftManagementService.mapSingle(request));
    }

    @PostMapping("/map/bulk")
    @Operation(summary = "Bulk map staff to a shift")
    public ResponseEntity<ShiftMappingResultDTO> mapBulk(@Valid @RequestBody BulkStaffShiftMapRequestDTO request) {
        return ResponseEntity.ok(shiftManagementService.mapBulk(request));
    }

    @DeleteMapping("/mappings/{mappingUuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a single staff-shift mapping")
    public void deleteMapping(@PathVariable UUID mappingUuid) {
        shiftManagementService.deleteMapping(mappingUuid);
    }

    @GetMapping("/mappings")
    @Operation(summary = "List current staff-shift mappings")
    public ResponseEntity<Page<StaffShiftMappingResponseDTO>> listMappings(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort,
            @RequestParam(value = "shiftUuid", required = false) UUID shiftUuid,
            @RequestParam(value = "staffCategory", required = false) StaffCategory staffCategory
    ) {
        String[] sortParts = sort.split(",");
        Sort s = sortParts.length >= 2
                ? Sort.by(Sort.Direction.fromString(sortParts[1]), sortParts[0])
                : Sort.by(Sort.Direction.DESC, sortParts[0]);
        Pageable pageable = PageRequest.of(page, size, s);
        return ResponseEntity.ok(shiftManagementService.listMappings(pageable, shiftUuid, staffCategory));
    }

    @GetMapping("/mappings/staff/{staffUuid}")
    @Operation(summary = "Get current shift mapping by staff UUID")
    public ResponseEntity<StaffShiftMappingResponseDTO> getStaffMapping(@PathVariable UUID staffUuid) {
        return ResponseEntity.ok(shiftManagementService.getCurrentMappingByStaff(staffUuid));
    }
}

