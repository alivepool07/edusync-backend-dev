package com.project.edusync.hrms.repository;

import com.project.edusync.hrms.model.entity.SalaryTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalaryTemplateRepository extends JpaRepository<SalaryTemplate, Long> {

    List<SalaryTemplate> findByActiveTrueOrderByTemplateNameAsc();
}

