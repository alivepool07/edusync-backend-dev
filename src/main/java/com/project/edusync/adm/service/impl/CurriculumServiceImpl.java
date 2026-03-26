package com.project.edusync.adm.service.impl;

import com.project.edusync.adm.exception.DuplicateEntryException;
import com.project.edusync.adm.exception.ResourceNotFoundException;
import com.project.edusync.adm.model.dto.request.CurriculumPeriodsUpdateRequestDto;
import com.project.edusync.adm.model.dto.request.CurriculumSubjectUpsertRequestDto;
import com.project.edusync.adm.model.dto.response.CurriculumClassSubjectResponseDto;
import com.project.edusync.adm.model.dto.response.CurriculumOverviewResponseDto;
import com.project.edusync.adm.model.entity.AcademicClass;
import com.project.edusync.adm.model.entity.CurriculumMap;
import com.project.edusync.adm.model.entity.Subject;
import com.project.edusync.adm.repository.AcademicClassRepository;
import com.project.edusync.adm.repository.CurriculumMapRepository;
import com.project.edusync.adm.repository.ScheduleRepository;
import com.project.edusync.adm.repository.SubjectRepository;
import com.project.edusync.adm.service.CurriculumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumServiceImpl implements CurriculumService {

    private final CurriculumMapRepository curriculumMapRepository;
    private final AcademicClassRepository academicClassRepository;
    private final SubjectRepository subjectRepository;
    private final ScheduleRepository scheduleRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "classCurriculum", key = "#classId")
    public List<CurriculumClassSubjectResponseDto> getClassCurriculum(UUID classId) {
        AcademicClass academicClass = findClassByUuid(classId);

        List<CurriculumMap> maps = curriculumMapRepository.findActiveByClassUuid(classId);
        if (maps.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, Long> scheduledBySubject = getScheduledPeriodsBySubject(classId, maps);
        return maps.stream()
                .map(map -> toClassSubjectResponse(academicClass.getUuid(), map, scheduledBySubject.getOrDefault(map.getSubject().getUuid(), 0L)))
                .toList();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "classCurriculum", key = "#classId"),
            @CacheEvict(value = "editorContext", allEntries = true),
            @CacheEvict(value = "availableSubjects", allEntries = true)
    })
    public CurriculumClassSubjectResponseDto addSubjectToClass(UUID classId, CurriculumSubjectUpsertRequestDto requestDto) {
        AcademicClass academicClass = findClassByUuid(classId);
        Subject subject = findSubjectByUuid(requestDto.getSubjectId());

        if (curriculumMapRepository.existsActiveByClassAndSubject(classId, requestDto.getSubjectId())) {
            throw new DuplicateEntryException("Subject already exists in class curriculum.");
        }

        CurriculumMap curriculumMap = new CurriculumMap();
        curriculumMap.setAcademicClass(academicClass);
        curriculumMap.setSubject(subject);
        curriculumMap.setPeriodsPerWeek(requestDto.getPeriodsPerWeek());
        curriculumMap.setIsActive(true);

        CurriculumMap saved = curriculumMapRepository.save(curriculumMap);
        Long scheduledPeriods = scheduleRepository.findScheduledPeriodsByClassAndSubjectIds(classId, List.of(subject.getUuid())).stream()
                .findFirst()
                .map(ScheduleRepository.SubjectScheduledPeriodsProjection::getScheduledPeriods)
                .orElse(0L);

        return toClassSubjectResponse(classId, saved, scheduledPeriods);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "classCurriculum", allEntries = true),
            @CacheEvict(value = "editorContext", allEntries = true),
            @CacheEvict(value = "availableSubjects", allEntries = true)
    })
    public CurriculumClassSubjectResponseDto updatePeriodsPerWeek(UUID curriculumMapId, CurriculumPeriodsUpdateRequestDto requestDto) {
        CurriculumMap curriculumMap = curriculumMapRepository.findActiveByUuid(curriculumMapId)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum map not found with id: " + curriculumMapId));

        curriculumMap.setPeriodsPerWeek(requestDto.getPeriodsPerWeek());
        CurriculumMap updated = curriculumMapRepository.save(curriculumMap);

        Long scheduledPeriods = scheduleRepository.findScheduledPeriodsByClassAndSubjectIds(
                        updated.getAcademicClass().getUuid(),
                        List.of(updated.getSubject().getUuid()))
                .stream()
                .findFirst()
                .map(ScheduleRepository.SubjectScheduledPeriodsProjection::getScheduledPeriods)
                .orElse(0L);

        return toClassSubjectResponse(updated.getAcademicClass().getUuid(), updated, scheduledPeriods);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "classCurriculum", allEntries = true),
            @CacheEvict(value = "editorContext", allEntries = true),
            @CacheEvict(value = "availableSubjects", allEntries = true)
    })
    public void removeSubjectFromCurriculum(UUID curriculumMapId) {
        CurriculumMap curriculumMap = curriculumMapRepository.findActiveByUuid(curriculumMapId)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum map not found with id: " + curriculumMapId));

        curriculumMap.setIsActive(false);
        curriculumMapRepository.save(curriculumMap);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurriculumOverviewResponseDto> getOverview() {
        return curriculumMapRepository.getCurriculumOverview().stream()
                .map(this::toOverviewResponse)
                .toList();
    }

    private AcademicClass findClassByUuid(UUID classId) {
        return academicClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));
    }

    private Subject findSubjectByUuid(UUID subjectId) {
        return subjectRepository.findActiveById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));
    }

    private Map<UUID, Long> getScheduledPeriodsBySubject(UUID classId, List<CurriculumMap> maps) {
        List<UUID> subjectIds = maps.stream().map(cm -> cm.getSubject().getUuid()).toList();
        return scheduleRepository.findScheduledPeriodsByClassAndSubjectIds(classId, subjectIds).stream()
                .collect(Collectors.toMap(
                        ScheduleRepository.SubjectScheduledPeriodsProjection::getSubjectId,
                        ScheduleRepository.SubjectScheduledPeriodsProjection::getScheduledPeriods,
                        (a, b) -> a
                ));
    }

    private CurriculumClassSubjectResponseDto toClassSubjectResponse(UUID classId, CurriculumMap map, Long scheduledPeriods) {
        return CurriculumClassSubjectResponseDto.builder()
                .curriculumMapId(map.getUuid())
                .classId(classId)
                .subjectId(map.getSubject().getUuid())
                .subjectName(map.getSubject().getName())
                .subjectCode(map.getSubject().getSubjectCode())
                .color(map.getSubject().getColor())
                .periodsPerWeek(map.getPeriodsPerWeek())
                .totalScheduledPeriods(scheduledPeriods)
                .build();
    }

    private CurriculumOverviewResponseDto toOverviewResponse(CurriculumMapRepository.CurriculumOverviewProjection row) {
        long totalPeriods = row.getTotalPeriodsPerWeek() == null ? 0L : row.getTotalPeriodsPerWeek();
        long scheduled = row.getScheduledPeriods() == null ? 0L : row.getScheduledPeriods();

        BigDecimal coverage = totalPeriods == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(scheduled)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalPeriods), 2, RoundingMode.HALF_UP);

        return CurriculumOverviewResponseDto.builder()
                .classId(row.getClassId())
                .className(row.getClassName())
                .totalSubjects(row.getTotalSubjects())
                .totalPeriodsPerWeek(totalPeriods)
                .scheduledPeriods(scheduled)
                .coveragePercent(coverage)
                .build();
    }
}


