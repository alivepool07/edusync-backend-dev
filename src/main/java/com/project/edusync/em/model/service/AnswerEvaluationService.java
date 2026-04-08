package com.project.edusync.em.model.service;

import com.project.edusync.em.model.dto.RequestDTO.AnnotationRequestDTO;
import com.project.edusync.em.model.dto.RequestDTO.EvaluationAssignmentCreateRequestDTO;
import com.project.edusync.em.model.dto.RequestDTO.SaveEvaluationMarksRequestDTO;
import com.project.edusync.em.model.dto.ResponseDTO.*;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface AnswerEvaluationService {

    EvaluationAssignmentResponseDTO assignTeacher(EvaluationAssignmentCreateRequestDTO request);

    List<EvaluationAssignmentResponseDTO> getAssignmentsForAdmin(UUID teacherIdFilter);

    List<EvaluationAssignmentResponseDTO> getAssignmentsForCurrentTeacher();

    List<TeacherEvaluationStudentResponseDTO> getStudentsForAssignedSchedule(Long scheduleId);

    Page<TeacherEvaluationStudentResponseDTO> getStudentsForAssignedSchedule(Long scheduleId, int page, int size);

    AnswerSheetUploadResponseDTO uploadAnswerSheet(Long scheduleId, UUID studentId, MultipartFile file);

    AnswerSheetImageGroupResponseDTO uploadAnswerSheetImages(Long scheduleId,
                                                             UUID studentId,
                                                             List<MultipartFile> files,
                                                             List<Integer> pageNumbers);

    AnswerSheetImageGroupResponseDTO getAnswerSheetImages(UUID studentId, Long scheduleId);

    AnswerSheetImageGroupResponseDTO completeImageUpload(Long scheduleId, UUID studentId);

    AnswerEvaluationStructureResponseDTO getEvaluationStructure(Long answerSheetId);

    EvaluationResultResponseDTO saveDraftMarks(Long answerSheetId, SaveEvaluationMarksRequestDTO requestDTO);

    EvaluationResultResponseDTO publishMarks(Long answerSheetId);

    AnnotationResponseDTO createAnnotation(Long answerSheetId, AnnotationRequestDTO requestDTO);

    List<AnnotationResponseDTO> getAnnotations(Long answerSheetId);

    String generateSignedFileUrl(Long answerSheetId);

    Resource loadSignedAnswerSheet(Long answerSheetId, String token, long expires);

    Page<AnswerSheetUploadResponseDTO> getAnswerSheetsForAssignedSchedule(Long scheduleId, int page, int size);
}

