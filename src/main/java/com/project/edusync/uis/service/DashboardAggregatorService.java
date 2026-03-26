package com.project.edusync.uis.service;

import com.project.edusync.uis.model.dto.dashboard.IntelligenceResponseDTO;
import com.project.edusync.uis.model.dto.dashboard.OverviewResponseDTO;

public interface DashboardAggregatorService {

    IntelligenceResponseDTO getDashboardIntelligence(Long userId, Long academicYearId);

    OverviewResponseDTO getDashboardOverview(Long userId, Long academicYearId);
}

