package com.project.edusync.em.model.controller;

import com.project.edusync.em.model.dto.ResponseDTO.AdmitCardResponseDTO;
import com.project.edusync.em.model.service.AdmitCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/admit-card")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class AdmitCardStudentController {

    private final AdmitCardService admitCardService;

    @GetMapping("/{examUuid}")
    public ResponseEntity<AdmitCardResponseDTO> getOwnAdmitCard(@PathVariable UUID examUuid) {
        return ResponseEntity.ok(admitCardService.getStudentAdmitCard(examUuid));
    }

    @GetMapping("/{examUuid}/pdf")
    public ResponseEntity<Resource> downloadOwnAdmitCardPdf(@PathVariable UUID examUuid) {
        Resource pdf = admitCardService.downloadStudentAdmitCardPdf(examUuid);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=admit-card-exam-" + examUuid + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}

