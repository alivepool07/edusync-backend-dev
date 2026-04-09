package com.project.edusync.em.model.controller;

import com.project.edusync.em.model.dto.ResponseDTO.AdminResultReviewResponseDTO;
import com.project.edusync.em.model.dto.ResponseDTO.EvaluationResultResponseDTO;
import com.project.edusync.em.model.enums.EvaluationResultStatus;
import com.project.edusync.em.model.service.AnswerEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/results")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SCHOOL_ADMIN','ROLE_SUPER_ADMIN')")
public class AnswerEvaluationResultAdminController {

    private final AnswerEvaluationService answerEvaluationService;

    @GetMapping
    public ResponseEntity<List<AdminResultReviewResponseDTO>> getResults(
            @RequestParam(required = false) EvaluationResultStatus status) {
        return ResponseEntity.ok(answerEvaluationService.getResultsForAdmin(status));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<EvaluationResultResponseDTO> approveResult(@PathVariable("id") Long resultId) {
        return ResponseEntity.ok(answerEvaluationService.approveResult(resultId));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<EvaluationResultResponseDTO> rejectResult(@PathVariable("id") Long resultId) {
        return ResponseEntity.ok(answerEvaluationService.rejectResult(resultId));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<EvaluationResultResponseDTO> publishResult(@PathVariable("id") Long resultId) {
        return ResponseEntity.ok(answerEvaluationService.publishResult(resultId));
    }
}

