package com.project.edusync.adm.service.impl;

import com.project.edusync.adm.exception.ResourceNotFoundException;
import com.project.edusync.adm.model.dto.response.AvailableSubjectDto;
import com.project.edusync.adm.model.dto.response.EditorContextResponseDto;
import com.project.edusync.adm.model.entity.CurriculumMap;
import com.project.edusync.adm.model.entity.Schedule;
import com.project.edusync.adm.model.entity.Section;
import com.project.edusync.adm.model.entity.Subject;
import com.project.edusync.adm.model.entity.Timeslot;
import com.project.edusync.adm.repository.CurriculumMapRepository;
import com.project.edusync.adm.repository.ScheduleRepository;
import com.project.edusync.adm.repository.SectionRepository;
import com.project.edusync.adm.repository.TimeslotRepository;
import com.project.edusync.adm.service.EditorContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EditorContextServiceImpl implements EditorContextService {

    private final SectionRepository sectionRepository;
    private final TimeslotRepository timeslotRepository;
    private final ScheduleRepository scheduleRepository;
    private final CurriculumMapRepository curriculumMapRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "editorContext", key = "#sectionId")
    public EditorContextResponseDto getEditorContext(UUID sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("No section resource found with id: " + sectionId));

        List<Timeslot> timeslots = timeslotRepository.findAllActive();
        List<Schedule> existingSchedule = scheduleRepository.findAllActiveWithReferencesBySectionUuid(sectionId);
        List<ScheduleRepository.TeacherSubjectPairProjection> teacherPairs = scheduleRepository.findTeacherSubjectPairs();

        return EditorContextResponseDto.builder()
                .section(EditorContextResponseDto.SectionSummaryDto.builder()
                        .uuid(section.getUuid())
                        .sectionName(section.getSectionName())
                        .className(section.getAcademicClass().getName())
                        .build())
                .timeslots(timeslots.stream().map(this::toTimeslotItem).toList())
                .availableSubjects(extractSubjects(section))
                .teachers(buildTeachers(teacherPairs))
                .existingSchedule(existingSchedule.stream().map(this::toExistingScheduleItem).toList())
                .build();
    }

    private EditorContextResponseDto.TimeslotItemDto toTimeslotItem(Timeslot timeslot) {
        return EditorContextResponseDto.TimeslotItemDto.builder()
                .uuid(timeslot.getUuid())
                .dayOfWeek(timeslot.getDayOfWeek())
                .startTime(timeslot.getStartTime())
                .endTime(timeslot.getEndTime())
                .slotLabel(timeslot.getSlotLabel())
                .isBreak(timeslot.getIsBreak())
                .build();
    }

    private List<AvailableSubjectDto> extractSubjects(Section section) {
        return curriculumMapRepository.findActiveByClassUuid(section.getAcademicClass().getUuid()).stream()
                .map(CurriculumMap::getSubject)
                .filter(s -> s != null && Boolean.TRUE.equals(s.getIsActive()))
                .map(this::toAvailableSubject)
                .toList();
    }

    private AvailableSubjectDto toAvailableSubject(Subject subject) {
        return AvailableSubjectDto.builder()
                .uuid(subject.getUuid())
                .name(subject.getName())
                .subjectCode(subject.getSubjectCode())
                .color(subject.getColor())
                .build();
    }

    private List<EditorContextResponseDto.TeacherItemDto> buildTeachers(List<ScheduleRepository.TeacherSubjectPairProjection> pairs) {
        Map<Long, TeacherAccumulator> byTeacherId = new LinkedHashMap<>();

        for (ScheduleRepository.TeacherSubjectPairProjection pair : pairs) {
            TeacherAccumulator accumulator = byTeacherId.computeIfAbsent(pair.getTeacherId(), id ->
                    new TeacherAccumulator(String.valueOf(id), buildName(pair.getFirstName(), pair.getLastName()), new ArrayList<>()));

            if (pair.getSubjectUuid() != null && !accumulator.teachableSubjectIds.contains(pair.getSubjectUuid())) {
                accumulator.teachableSubjectIds.add(pair.getSubjectUuid());
            }
        }

        return byTeacherId.values().stream()
                .map(a -> EditorContextResponseDto.TeacherItemDto.builder()
                        .id(a.id)
                        .name(a.name)
                        .teachableSubjectIds(a.teachableSubjectIds)
                        .build())
                .toList();
    }

    private EditorContextResponseDto.ExistingScheduleItemDto toExistingScheduleItem(Schedule schedule) {
        return EditorContextResponseDto.ExistingScheduleItemDto.builder()
                .uuid(schedule.getUuid())
                .subjectId(schedule.getSubject().getUuid())
                .teacherId(String.valueOf(schedule.getTeacher().getId()))
                .roomId(schedule.getRoom().getUuid())
                .timeslotId(schedule.getTimeslot().getUuid())
                .slotLabel(schedule.getTimeslot().getSlotLabel())
                .build();
    }

    private String buildName(String firstName, String lastName) {
        String first = firstName == null ? "" : firstName;
        String last = lastName == null ? "" : lastName;
        return (first + " " + last).trim();
    }

    private record TeacherAccumulator(String id, String name, List<UUID> teachableSubjectIds) {}
}


