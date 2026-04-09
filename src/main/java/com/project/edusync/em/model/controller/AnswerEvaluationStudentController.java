package com.project.edusync.em.model.controller;

import com.project.edusync.em.model.dto.ResponseDTO.StudentResultDetailResponseDTO;
import com.project.edusync.em.model.dto.ResponseDTO.StudentResultResponseDTO;
import com.project.edusync.em.model.service.AnswerEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student/results")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class AnswerEvaluationStudentController {

    private final AnswerEvaluationService answerEvaluationService;

    @GetMapping
    public ResponseEntity<List<StudentResultResponseDTO>> getMyResults() {
        return ResponseEntity.ok(answerEvaluationService.getStudentPublishedResults());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResultDetailResponseDTO> getMyResult(@PathVariable("id") Long resultId) {
        return ResponseEntity.ok(answerEvaluationService.getStudentPublishedResult(resultId));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadMyResultPdf(@PathVariable("id") Long resultId) {
        byte[] pdf = answerEvaluationService.generateStudentResultPdf(resultId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=result-" + resultId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}

