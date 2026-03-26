package com.project.edusync.adm.service;

import com.project.edusync.adm.model.dto.request.CurriculumPeriodsUpdateRequestDto;
import com.project.edusync.adm.model.dto.request.CurriculumSubjectUpsertRequestDto;
import com.project.edusync.adm.model.dto.response.CurriculumClassSubjectResponseDto;
import com.project.edusync.adm.model.dto.response.CurriculumOverviewResponseDto;

import java.util.List;
import java.util.UUID;

public interface CurriculumService {
    List<CurriculumClassSubjectResponseDto> getClassCurriculum(UUID classId);

    CurriculumClassSubjectResponseDto addSubjectToClass(UUID classId, CurriculumSubjectUpsertRequestDto requestDto);

    CurriculumClassSubjectResponseDto updatePeriodsPerWeek(UUID curriculumMapId, CurriculumPeriodsUpdateRequestDto requestDto);

    void removeSubjectFromCurriculum(UUID curriculumMapId);

    List<CurriculumOverviewResponseDto> getOverview();
}

