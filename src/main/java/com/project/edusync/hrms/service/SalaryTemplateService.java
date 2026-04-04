package com.project.edusync.hrms.service;

import com.project.edusync.hrms.dto.salary.SalaryTemplateCreateDTO;
import com.project.edusync.hrms.dto.salary.SalaryTemplateResponseDTO;
import com.project.edusync.hrms.dto.salary.SalaryTemplateUpdateDTO;

import java.util.List;

public interface SalaryTemplateService {

    List<SalaryTemplateResponseDTO> listAll();

    SalaryTemplateResponseDTO getById(Long templateId);

    SalaryTemplateResponseDTO create(SalaryTemplateCreateDTO dto);

    SalaryTemplateResponseDTO update(Long templateId, SalaryTemplateUpdateDTO dto);

    void delete(Long templateId);
}

