package com.project.edusync.ams.model.controller;

import com.project.edusync.ams.model.dto.request.StaffAttendanceRequestDTO;
import com.project.edusync.ams.model.dto.response.StaffAttendanceResponseDTO;
import com.project.edusync.ams.model.service.StaffAttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(value = "${api.url:/api/v1}/auth/ams/staff", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AMS Staff Attendance", description = "UUID-first staff attendance APIs")
public class StaffAttendanceController {

    private final StaffAttendanceService service;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create staff attendance")
    public ResponseEntity<StaffAttendanceResponseDTO> create(
            @Valid @RequestBody StaffAttendanceRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {

        log.debug("POST staff attendance request: staffUuid={}, date={}", request.getStaffUuid(), request.getAttendanceDate());
        StaffAttendanceResponseDTO dto = service.createAttendance(request, headerUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping(path = "/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Bulk create staff attendance")
    public ResponseEntity<List<StaffAttendanceResponseDTO>> bulkCreate(
            @Valid @RequestBody List<StaffAttendanceRequestDTO> requests,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {

        log.debug("POST bulk staff attendance request, count={}", requests == null ? 0 : requests.size());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.bulkCreate(requests, headerUserId));
    }

    @GetMapping
    @Operation(summary = "List staff attendance")
    public ResponseEntity<Page<StaffAttendanceResponseDTO>> list(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort,
            @Parameter(description = "Filter by staff UUID", schema = @Schema(format = "uuid"))
            @RequestParam(value = "staffUuid", required = false) UUID staffUuid,
            @RequestParam(value = "date", required = false) String dateStr) {

        String[] sortParts = sort.split(",");
        Sort s = sortParts.length >= 2
                ? Sort.by(Sort.Direction.fromString(sortParts[1]), sortParts[0])
                : Sort.by(Sort.Direction.DESC, sortParts[0]);

        Pageable pageable = PageRequest.of(page, size, s);
        Optional<LocalDate> date = Optional.ofNullable(dateStr).filter(s1 -> !s1.isBlank()).map(LocalDate::parse);

        return ResponseEntity.ok(service.listAttendances(pageable, Optional.ofNullable(staffUuid), date));
    }

    @GetMapping("/{recordUuid}")
    @Operation(summary = "Get staff attendance record by UUID")
    public ResponseEntity<StaffAttendanceResponseDTO> getById(
            @Parameter(description = "Staff attendance record UUID", schema = @Schema(format = "uuid"))
            @PathVariable UUID recordUuid) {
        return ResponseEntity.ok(service.getAttendance(recordUuid));
    }

    @PutMapping(path = "/{recordUuid}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update staff attendance record by UUID")
    public ResponseEntity<StaffAttendanceResponseDTO> update(
            @Parameter(description = "Staff attendance record UUID", schema = @Schema(format = "uuid"))
            @PathVariable UUID recordUuid,
            @Valid @RequestBody StaffAttendanceRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {

        return ResponseEntity.ok(service.updateAttendance(recordUuid, request, headerUserId));
    }

    @DeleteMapping("/{recordUuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete staff attendance record by UUID")
    public void delete(
                       @Parameter(description = "Staff attendance record UUID", schema = @Schema(format = "uuid"))
                       @PathVariable UUID recordUuid,
                       @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        service.deleteAttendance(recordUuid, headerUserId);
    }
}
