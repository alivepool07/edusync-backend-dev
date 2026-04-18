package com.project.edusync.uis.service.impl;

import com.project.edusync.uis.model.dto.dashboard.IntelligenceResponseDTO;
import com.project.edusync.uis.model.dto.dashboard.OverviewResponseDTO;
import com.project.edusync.uis.model.entity.Guardian;
import com.project.edusync.uis.model.entity.Student;
import com.project.edusync.uis.model.entity.StudentGuardianRelationship;
import com.project.edusync.uis.repository.GuardianRepository;
import com.project.edusync.uis.repository.StudentGuardianRelationshipRepository;
import com.project.edusync.uis.service.DashboardAggregatorService;
import com.project.edusync.uis.service.GuardianDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuardianDashboardServiceImpl implements GuardianDashboardService {
    private final GuardianRepository guardianRepository;
    private final StudentGuardianRelationshipRepository relationshipRepository;
    private final DashboardAggregatorService dashboardAggregatorService;

    @Override
    @Transactional(readOnly = true)
    public List<IntelligenceResponseDTO> getAllLinkedStudentsDashboardIntelligence(Long guardianUserId, Long academicYearId) {
        log.info("Fetching dashboard intelligence for all students linked to guardian userId={}", guardianUserId);
        Guardian guardian = guardianRepository.findByUserProfile_User_Id(guardianUserId)
                .orElseThrow(() -> new IllegalArgumentException("Guardian not found for userId=" + guardianUserId));
        
        List<StudentGuardianRelationship> relationships = relationshipRepository.findByGuardian(guardian);
        List<IntelligenceResponseDTO> dashboards = new ArrayList<>();
        
        for (StudentGuardianRelationship rel : relationships) {
            Student student = rel.getStudent();
            if (student.getUserProfile() != null && student.getUserProfile().getUser() != null) {
                Long studentUserId = student.getUserProfile().getUser().getId();
                dashboards.add(dashboardAggregatorService.getDashboardIntelligence(studentUserId, academicYearId));
            } else {
                log.warn("Student id={} linked to guardian userId={} has no user profile mapping; skipping intelligence aggregation", 
                        student.getId(), guardianUserId);
            }
        }
        return dashboards;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OverviewResponseDTO> getAllLinkedStudentsDashboardOverview(Long guardianUserId, Long academicYearId) {
        log.info("Fetching dashboard overview for all students linked to guardian userId={}", guardianUserId);
        Guardian guardian = guardianRepository.findByUserProfile_User_Id(guardianUserId)
                .orElseThrow(() -> new IllegalArgumentException("Guardian not found for userId=" + guardianUserId));
        
        List<StudentGuardianRelationship> relationships = relationshipRepository.findByGuardian(guardian);
        List<OverviewResponseDTO> dashboards = new ArrayList<>();
        
        for (StudentGuardianRelationship rel : relationships) {
            Student student = rel.getStudent();
            if (student.getUserProfile() != null && student.getUserProfile().getUser() != null) {
                Long studentUserId = student.getUserProfile().getUser().getId();
                dashboards.add(dashboardAggregatorService.getDashboardOverview(studentUserId, academicYearId));
            } else {
                log.warn("Student id={} linked to guardian userId={} has no user profile mapping; skipping overview aggregation", 
                        student.getId(), guardianUserId);
            }
        }
        return dashboards;
    }
}

