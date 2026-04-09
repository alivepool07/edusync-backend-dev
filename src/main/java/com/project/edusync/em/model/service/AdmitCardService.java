package com.project.edusync.em.model.service;

import com.project.edusync.em.model.dto.ResponseDTO.AdmitCardGenerationResponseDTO;
import com.project.edusync.em.model.dto.ResponseDTO.AdmitCardResponseDTO;
import com.project.edusync.em.model.dto.ResponseDTO.ScheduleAdmitCardStatusDTO;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.UUID;

public interface AdmitCardService {

    AdmitCardGenerationResponseDTO generateAdmitCards(UUID examUuid);

    AdmitCardGenerationResponseDTO generateAdmitCardsForSchedule(UUID examUuid, Long scheduleId);

    List<ScheduleAdmitCardStatusDTO> getAdmitCardStatusByExam(UUID examUuid);

    int publishAdmitCards(UUID examUuid);

    int publishAdmitCardsForSchedules(UUID examUuid, List<Long> scheduleIds);

    byte[] downloadAdmitCardsZip(UUID examUuid);

    AdmitCardResponseDTO getStudentAdmitCard(UUID examUuid);

    Resource downloadStudentAdmitCardPdf(UUID examUuid);
}
