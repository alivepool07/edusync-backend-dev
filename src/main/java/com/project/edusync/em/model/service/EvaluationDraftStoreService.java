package com.project.edusync.em.model.service;

import com.project.edusync.em.model.dto.RequestDTO.SaveQuestionMarkRequestDTO;

import java.util.List;

public interface EvaluationDraftStoreService {

    void saveDraft(Long answerSheetId, List<SaveQuestionMarkRequestDTO> questionMarks);

    List<SaveQuestionMarkRequestDTO> getDraft(Long answerSheetId);

    void deleteDraft(Long answerSheetId);
}

