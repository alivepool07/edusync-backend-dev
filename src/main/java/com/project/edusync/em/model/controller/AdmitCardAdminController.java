package com.project.edusync.em.model.controller;

import com.project.edusync.em.model.dto.RequestDTO.AdmitCardGenerateRequestDTO;
import com.project.edusync.em.model.dto.RequestDTO.AdmitCardPublishRequestDTO;
import com.project.edusync.em.model.dto.ResponseDTO.AdmitCardGenerationResponseDTO;
import com.project.edusync.em.model.dto.ResponseDTO.ScheduleAdmitCardStatusDTO;
import com.project.edusync.em.model.service.AdmitCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Collections;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/admit-cards")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SCHOOL_ADMIN','SUPER_ADMIN')")
public class AdmitCardAdminController {

    private final AdmitCardService admitCardService;

    @PostMapping("/generate")
    public ResponseEntity<AdmitCardGenerationResponseDTO> generate(@Valid @RequestBody AdmitCardGenerateRequestDTO requestDTO) {
        if (requestDTO.getScheduleId() != null) {
            // Per-schedule generation
            return ResponseEntity.ok(
                    admitCardService.generateAdmitCardsForSchedule(requestDTO.getExamId(), requestDTO.getScheduleId()));
        }
        // Legacy: generate for entire exam
        return ResponseEntity.ok(admitCardService.generateAdmitCards(requestDTO.getExamId()));
    }

    @PostMapping("/generate/schedule/{scheduleId}")
    public ResponseEntity<AdmitCardGenerationResponseDTO> generateForSchedule(
            @Valid @RequestBody AdmitCardGenerateRequestDTO requestDTO,
            @PathVariable Long scheduleId) {
        return ResponseEntity.ok(
                admitCardService.generateAdmitCardsForSchedule(requestDTO.getExamId(), scheduleId));
    }

    @GetMapping("/status/{examUuid}")
    public ResponseEntity<List<ScheduleAdmitCardStatusDTO>> getStatus(@PathVariable UUID examUuid) {
        return ResponseEntity.ok(admitCardService.getAdmitCardStatusByExam(examUuid));
    }

    @PostMapping("/publish/{examUuid}")
    public ResponseEntity<String> publish(@PathVariable UUID examUuid) {
        int published = admitCardService.publishAdmitCards(examUuid);
        return ResponseEntity.ok("Published admit cards: " + published);
    }

    @PostMapping("/publish/{examUuid}/schedule/{scheduleId}")
    public ResponseEntity<String> publishForSchedule(@PathVariable UUID examUuid,
                                                     @PathVariable Long scheduleId) {
        int published = admitCardService.publishAdmitCardsForSchedules(examUuid, Collections.singletonList(scheduleId));
        return ResponseEntity.ok("Published admit cards for schedule " + scheduleId + ": " + published);
    }

    @PostMapping("/publish/{examUuid}/schedules")
    public ResponseEntity<String> publishForSchedules(@PathVariable UUID examUuid,
                                                      @Valid @RequestBody AdmitCardPublishRequestDTO requestDTO) {
        int published = admitCardService.publishAdmitCardsForSchedules(examUuid, requestDTO.getScheduleIds());
        return ResponseEntity.ok("Published admit cards for selected schedules: " + published);
    }

    @GetMapping("/download/{examUuid}")
    public ResponseEntity<byte[]> download(@PathVariable UUID examUuid) {
        byte[] zip = admitCardService.downloadAdmitCardsZip(examUuid);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=admit-cards-exam-" + examUuid + ".zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zip);
    }
}
