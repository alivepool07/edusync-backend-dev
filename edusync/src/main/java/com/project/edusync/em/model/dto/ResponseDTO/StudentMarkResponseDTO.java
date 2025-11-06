package com.project.edusync.em.model.dto.ResponseDTO;
import com.project.edusync.em.model.enums.StudentAttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for sending StudentMark data back to the client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentMarkResponseDTO {

    private UUID markUuid;
    private Long scheduleId;

    // --- Rich Student Data for UI Grid ---
    private Long studentId;
    private String studentName;       // e.g., "John Doe"
    private String enrollmentNumber;  // e.g., "ENR-2025-123"

    // --- Mark Data ---
    private BigDecimal marksObtained;
    private StudentAttendanceStatus attendanceStatus;
    private String grade;
    private String remarks;
}