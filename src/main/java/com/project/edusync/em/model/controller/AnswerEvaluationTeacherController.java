package com.project.edusync.em.model.controller;

import com.project.edusync.em.model.dto.RequestDTO.AnnotationRequestDTO;
import com.project.edusync.em.model.dto.RequestDTO.SaveEvaluationMarksRequestDTO;
import com.project.edusync.em.model.dto.ResponseDTO.AnnotationResponseDTO;
import com.project.edusync.em.model.dto.ResponseDTO.AnswerEvaluationStructureResponseDTO;
import com.project.edusync.em.model.dto.ResponseDTO.AnswerSheetImageGroupResponseDTO;
import com.project.edusync.em.model.dto.ResponseDTO.AnswerSheetUploadResponseDTO;
import com.project.edusync.em.model.dto.ResponseDTO.EvaluationAssignmentResponseDTO;
import com.project.edusync.em.model.dto.ResponseDTO.EvaluationResultResponseDTO;
import com.project.edusync.em.model.dto.ResponseDTO.TeacherEvaluationStudentResponseDTO;
import com.project.edusync.em.model.service.AnswerEvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_TEACHER','ROLE_ADMIN','ROLE_SCHOOL_ADMIN','ROLE_SUPER_ADMIN')")
public class AnswerEvaluationTeacherController {

    private final AnswerEvaluationService answerEvaluationService;

    @GetMapping("/evaluation/assignments")
    public ResponseEntity<List<EvaluationAssignmentResponseDTO>> getMyAssignments() {
        return ResponseEntity.ok(answerEvaluationService.getAssignmentsForCurrentTeacher());
    }

    @GetMapping("/evaluation/{scheduleId}/students")
    public ResponseEntity<List<TeacherEvaluationStudentResponseDTO>> getStudentsForSchedule(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(answerEvaluationService.getStudentsForAssignedSchedule(scheduleId));
    }

    @GetMapping("/evaluation/{scheduleId}/students/paged")
    public ResponseEntity<Page<TeacherEvaluationStudentResponseDTO>> getStudentsForSchedulePaged(
            @PathVariable Long scheduleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        return ResponseEntity.ok(answerEvaluationService.getStudentsForAssignedSchedule(scheduleId, page, size));
    }

    @PostMapping(value = "/answer-sheets/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnswerSheetUploadResponseDTO> uploadAnswerSheet(
            @RequestParam Long scheduleId,
            @RequestParam UUID studentId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(answerEvaluationService.uploadAnswerSheet(scheduleId, studentId, file));
    }

    @PostMapping(value = "/answer-sheets/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnswerSheetImageGroupResponseDTO> uploadAnswerSheetImages(
            @RequestParam Long scheduleId,
            @RequestParam UUID studentId,
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam(required = false) List<Integer> pageNumbers) {
        return ResponseEntity.ok(answerEvaluationService.uploadAnswerSheetImages(scheduleId, studentId, files, pageNumbers));
    }

    @GetMapping("/answer-sheets/{studentId}/{scheduleId}")
    public ResponseEntity<AnswerSheetImageGroupResponseDTO> getAnswerSheetImages(
            @PathVariable UUID studentId,
            @PathVariable Long scheduleId) {
        return ResponseEntity.ok(answerEvaluationService.getAnswerSheetImages(studentId, scheduleId));
    }

    @PostMapping("/answer-sheets/images/complete")
    public ResponseEntity<AnswerSheetImageGroupResponseDTO> completeImageUpload(
            @RequestParam Long scheduleId,
            @RequestParam UUID studentId) {
        return ResponseEntity.ok(answerEvaluationService.completeImageUpload(scheduleId, studentId));
    }

    @PostMapping("/evaluation/{scheduleId}/upload-complete")
    public ResponseEntity<EvaluationAssignmentResponseDTO> markScheduleUploadComplete(
            @PathVariable Long scheduleId) {
        return ResponseEntity.ok(answerEvaluationService.markScheduleUploadComplete(scheduleId));
    }

    @GetMapping("/evaluation/{answerSheetId}/structure")
    public ResponseEntity<AnswerEvaluationStructureResponseDTO> getEvaluationStructure(@PathVariable Long answerSheetId) {
        return ResponseEntity.ok(answerEvaluationService.getEvaluationStructure(answerSheetId));
    }

    @PostMapping("/evaluation/{answerSheetId}/marks")
    public ResponseEntity<EvaluationResultResponseDTO> saveDraftMarks(
            @PathVariable Long answerSheetId,
            @Valid @RequestBody SaveEvaluationMarksRequestDTO requestDTO) {
        return ResponseEntity.ok(answerEvaluationService.saveDraftMarks(answerSheetId, requestDTO));
    }

    @PostMapping("/evaluation/{answerSheetId}/submit")
    public ResponseEntity<EvaluationResultResponseDTO> submitMarks(@PathVariable Long answerSheetId) {
        return ResponseEntity.ok(answerEvaluationService.submitMarks(answerSheetId));
    }

    @PostMapping("/evaluation/{answerSheetId}/publish")
    public ResponseEntity<EvaluationResultResponseDTO> publishAlias(@PathVariable Long answerSheetId) {
        return ResponseEntity.ok(answerEvaluationService.submitMarks(answerSheetId));
    }

    @PostMapping("/answer-sheets/{id}/annotations")
    public ResponseEntity<AnnotationResponseDTO> createAnnotation(
            @PathVariable("id") Long answerSheetId,
            @Valid @RequestBody AnnotationRequestDTO requestDTO) {
        return ResponseEntity.ok(answerEvaluationService.createAnnotation(answerSheetId, requestDTO));
    }

    @GetMapping("/answer-sheets/{id}/annotations")
    public ResponseEntity<List<AnnotationResponseDTO>> getAnnotations(@PathVariable("id") Long answerSheetId) {
        return ResponseEntity.ok(answerEvaluationService.getAnnotations(answerSheetId));
    }

    @GetMapping("/answer-sheets/{id}/signed-url")
    public ResponseEntity<String> getSignedUrl(@PathVariable("id") Long answerSheetId) {
        return ResponseEntity.ok(answerEvaluationService.generateSignedFileUrl(answerSheetId));
    }

    @GetMapping("/answer-sheets/{id}/file")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Resource> downloadAnswerSheet(
            @PathVariable("id") Long answerSheetId,
            @RequestParam String token,
            @RequestParam long expires) {
        Resource resource = answerEvaluationService.loadSignedAnswerSheet(answerSheetId, token, expires);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=answer-sheet-" + answerSheetId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @GetMapping("/evaluation/{scheduleId}/answer-sheets")
    public ResponseEntity<Page<AnswerSheetUploadResponseDTO>> getAnswerSheets(
            @PathVariable Long scheduleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        return ResponseEntity.ok(answerEvaluationService.getAnswerSheetsForAssignedSchedule(scheduleId, page, size));
    }
}

