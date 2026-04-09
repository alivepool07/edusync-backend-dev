package com.project.edusync.em.model.dto.ResponseDTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AdmitCardResponseDTO {
    private Long admitCardId;
    private Long examId;
    private String examName;
    private UUID studentId;
    private String studentName;
    private String enrollmentNumber;
    private LocalDateTime generatedAt;
    private String status;
    private String pdfUrl;
    private String publishedBy;
    private LocalDateTime publishedAt;
    private List<AdmitCardEntryResponseDTO> entries;
}

