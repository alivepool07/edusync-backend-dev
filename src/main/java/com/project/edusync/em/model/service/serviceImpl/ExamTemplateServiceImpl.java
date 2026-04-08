package com.project.edusync.em.model.service.serviceImpl;

import com.project.edusync.common.exception.emException.EdusyncException;
import com.project.edusync.common.config.CacheNames;
import com.project.edusync.em.model.dto.RequestDTO.ExamTemplateRequestDTO;
import com.project.edusync.em.model.dto.RequestDTO.TemplateSectionRequestDTO;
import com.project.edusync.em.model.dto.ResponseDTO.EvaluationStructureResponseDTO;
import com.project.edusync.em.model.dto.ResponseDTO.ExamTemplateResponseDTO;
import com.project.edusync.em.model.dto.ResponseDTO.TemplateSectionResponseDTO;
import com.project.edusync.em.model.entity.ExamTemplate;
import com.project.edusync.em.model.entity.TemplateSection;
import com.project.edusync.em.model.repository.ExamTemplateRepository;
import com.project.edusync.em.model.service.ExamTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExamTemplateServiceImpl implements ExamTemplateService {

    private final ExamTemplateRepository examTemplateRepository;

    @Override
    public ExamTemplateResponseDTO createTemplate(ExamTemplateRequestDTO requestDTO) {
        validateTemplateRequest(requestDTO);

        ExamTemplate template = new ExamTemplate();
        template.setName(requestDTO.getName().trim());
        template.setInUse(false);
        applySections(template, requestDTO.getSections());

        ExamTemplate saved = examTemplateRepository.save(template);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamTemplateResponseDTO> getAllTemplates() {
        return examTemplateRepository.findAllWithSections().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.EXAM_TEMPLATES, key = "#templateId")
    public ExamTemplateResponseDTO getTemplateById(UUID templateId) {
        return toResponse(getTemplateEntityWithSections(templateId));
    }

    @Override
    @CacheEvict(value = CacheNames.EXAM_TEMPLATES, key = "#templateId")
    public ExamTemplateResponseDTO updateTemplate(UUID templateId, ExamTemplateRequestDTO requestDTO) {
        validateTemplateRequest(requestDTO);

        ExamTemplate template = getTemplateEntityWithSections(templateId);
        assertMutable(template);

        template.setName(requestDTO.getName().trim());
        applySections(template, requestDTO.getSections());

        return toResponse(examTemplateRepository.save(template));
    }

    @Override
    @CacheEvict(value = CacheNames.EXAM_TEMPLATES, key = "#templateId")
    public void deleteTemplate(UUID templateId) {
        ExamTemplate template = getTemplateEntityWithSections(templateId);
        assertMutable(template);
        examTemplateRepository.delete(template);
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluationStructureResponseDTO getTemplatePreview(UUID templateId) {
        ExamTemplate template = getTemplateEntityWithSections(templateId);

        int runningQuestionNo = 1;
        List<EvaluationStructureResponseDTO.EvaluationSectionDTO> sections = new ArrayList<>();
        for (TemplateSection section : orderedSections(template.getSections())) {
            List<EvaluationStructureResponseDTO.EvaluationQuestionDTO> questions = new ArrayList<>();
            for (int i = 0; i < section.getQuestionCount(); i++) {
                questions.add(EvaluationStructureResponseDTO.EvaluationQuestionDTO.builder()
                        .qNo(runningQuestionNo + i)
                        .maxMarks(section.getMarksPerQuestion())
                        .build());
            }
            runningQuestionNo += section.getQuestionCount();

            sections.add(EvaluationStructureResponseDTO.EvaluationSectionDTO.builder()
                    .name(section.getSectionName())
                    .questions(questions)
                    .build());
        }

        return EvaluationStructureResponseDTO.builder()
                .sections(sections)
                .build();
    }

    private ExamTemplate getTemplateEntityWithSections(UUID templateId) {
        return examTemplateRepository.findByUuidWithSections(templateId)
                .orElseThrow(() -> new EdusyncException("EM-404", "Exam template not found", HttpStatus.NOT_FOUND));
    }

    private void assertMutable(ExamTemplate template) {
        if (template.isInUse()) {
            throw new EdusyncException("EM-409", "Template is already in use and cannot be modified", HttpStatus.CONFLICT);
        }
    }

    private void validateTemplateRequest(ExamTemplateRequestDTO requestDTO) {
        if (requestDTO.getSections() == null || requestDTO.getSections().isEmpty()) {
            throw new EdusyncException("EM-400", "At least one section is required", HttpStatus.BAD_REQUEST);
        }

        Set<Integer> sectionOrders = new HashSet<>();
        for (TemplateSectionRequestDTO section : requestDTO.getSections()) {
            if (section.getQuestionCount() == null || section.getQuestionCount() < 1) {
                throw new EdusyncException("EM-400", "questionCount must be at least 1", HttpStatus.BAD_REQUEST);
            }
            if (section.getMarksPerQuestion() == null || section.getMarksPerQuestion() < 1) {
                throw new EdusyncException("EM-400", "marksPerQuestion must be at least 1", HttpStatus.BAD_REQUEST);
            }
            if (section.getSectionOrder() == null || section.getSectionOrder() < 1) {
                throw new EdusyncException("EM-400", "sectionOrder must be at least 1", HttpStatus.BAD_REQUEST);
            }
            if (!sectionOrders.add(section.getSectionOrder())) {
                throw new EdusyncException("EM-400", "sectionOrder must be unique per template", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private void applySections(ExamTemplate template, List<TemplateSectionRequestDTO> sectionRequests) {
        List<TemplateSection> sections = sectionRequests.stream()
                .map(section -> TemplateSection.builder()
                        .template(template)
                        .sectionName(section.getSectionName().trim())
                        .sectionOrder(section.getSectionOrder())
                        .questionCount(section.getQuestionCount())
                        .marksPerQuestion(section.getMarksPerQuestion())
                        .isObjective(section.getIsObjective())
                        .isSubjective(section.getIsSubjective())
                        .build())
                .sorted(Comparator.comparing(TemplateSection::getSectionOrder))
                .collect(Collectors.toList());

        int totalMarks = sections.stream()
                .mapToInt(section -> section.getQuestionCount() * section.getMarksPerQuestion())
                .sum();
        int totalQuestions = sections.stream()
                .mapToInt(TemplateSection::getQuestionCount)
                .sum();

        template.getSections().clear();
        template.getSections().addAll(sections);
        template.setTotalMarks(totalMarks);
        template.setTotalQuestions(totalQuestions);
    }

    private List<TemplateSection> orderedSections(List<TemplateSection> sections) {
        return sections.stream()
                .sorted(Comparator.comparing(TemplateSection::getSectionOrder))
                .collect(Collectors.toList());
    }

    private ExamTemplateResponseDTO toResponse(ExamTemplate template) {
        return ExamTemplateResponseDTO.builder()
                .id(template.getUuid())
                .name(template.getName())
                .totalMarks(template.getTotalMarks())
                .totalQuestions(template.getTotalQuestions())
                .inUse(template.isInUse())
                .createdAt(template.getCreatedAt())
                .sections(orderedSections(template.getSections()).stream()
                        .map(section -> TemplateSectionResponseDTO.builder()
                                .id(section.getUuid())
                                .sectionName(section.getSectionName())
                                .sectionOrder(section.getSectionOrder())
                                .questionCount(section.getQuestionCount())
                                .marksPerQuestion(section.getMarksPerQuestion())
                                .isObjective(section.getIsObjective())
                                .isSubjective(section.getIsSubjective())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}

