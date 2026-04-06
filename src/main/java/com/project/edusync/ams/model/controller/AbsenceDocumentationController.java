package com.project.edusync.ams.model.controller;

import com.project.edusync.ams.model.dto.request.SubmitExcuseRequestDTO;
import com.project.edusync.ams.model.dto.response.AbsenceDocumentationResponseDTO;
import com.project.edusync.ams.model.service.AbsenceDocumentationService;
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

import java.util.Map;
import java.util.UUID;

/**
 * Controller for Absence / Excuse workflow.
 * Base path uses testing-friendly ${api.url:/api/v1}/auth/ams/excuses
 */
@RestController
@RequestMapping("${api.url:/api/v1}/auth/ams/excuses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AMS Absence Documentation", description = "UUID-first excuse workflow APIs")
public class AbsenceDocumentationController {

    private final AbsenceDocumentationService service;

    /**
     * Submit an excuse documentation for an attendance record.
     * POST /auth/ams/excuses/submit
     */
    @PostMapping(path = "/submit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Submit absence documentation")
    public ResponseEntity<AbsenceDocumentationResponseDTO> submit(@Valid @RequestBody SubmitExcuseRequestDTO req) {
        log.debug("Submit excuse for attendanceUuid={}, by userUuid={}", req.getAttendanceUuid(), req.getSubmittedByParentUuid());
        AbsenceDocumentationResponseDTO dto = service.submit(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * List pending documentation entries (paginated).
     * GET /auth/ams/excuses/pending
     */
    @GetMapping(path = "/pending", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List pending absence documentation")
    public ResponseEntity<Page<AbsenceDocumentationResponseDTO>> pending(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(service.listPending(pageable));
    }

    /**
     * Get documentation by id.
     * GET /auth/ams/excuses/{docUuid}
     */
    @GetMapping(path = "/{docUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get absence documentation by UUID")
    public ResponseEntity<AbsenceDocumentationResponseDTO> get(
            @Parameter(description = "Absence documentation UUID", schema = @Schema(format = "uuid"))
            @PathVariable UUID docUuid) {
        return ResponseEntity.ok(service.getByUuid(docUuid));
    }

    /**
     * Approve a pending documentation (marks StudentDailyAttendance to Excused).
     * POST /auth/ams/excuses/{docUuid}/approve
     */
    @PostMapping(path = "/{docUuid}/approve", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Approve absence documentation by UUID")
    public ResponseEntity<AbsenceDocumentationResponseDTO> approve(
            @Parameter(description = "Absence documentation UUID", schema = @Schema(format = "uuid"))
            @PathVariable UUID docUuid,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {

        AbsenceDocumentationResponseDTO dto = service.approve(docUuid, headerUserId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Reject a pending documentation with optional reason in the body.
     * POST /auth/ams/excuses/{docUuid}/reject
     */
    @PostMapping(path = "/{docUuid}/reject", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Reject absence documentation by UUID")
    public ResponseEntity<AbsenceDocumentationResponseDTO> reject(
            @Parameter(description = "Absence documentation UUID", schema = @Schema(format = "uuid"))
            @PathVariable UUID docUuid,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestBody(required = false) Map<String, String> body) {

        String reason = body == null ? null : body.get("rejectionReason");
        AbsenceDocumentationResponseDTO dto = service.reject(docUuid, headerUserId, reason);
        return ResponseEntity.ok(dto);
    }
}
