package com.project.edusync.ams.model.dto.response;

import com.project.edusync.ams.model.enums.AttendanceSource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffAttendanceResponseDTO {

    @Deprecated
    @Schema(description = "Deprecated internal attendance id", deprecated = true)
    private Long staffAttendanceId;

    @Schema(description = "Staff attendance UUID", format = "uuid")
    private String uuid;

    @Schema(description = "Staff UUID", format = "uuid")
    private String staffUuid;

    @Deprecated
    @Schema(description = "Deprecated internal staff id", deprecated = true)
    private Long staffId;
    private String staffName;
    private String jobTitle;

    private LocalDate attendanceDate;

    private String attendanceMark;
    private String shortCode;
    private String colorCode;

    private LocalTime timeIn;
    private LocalTime timeOut;
    private Double totalHours;

    private AttendanceSource source;
    private String notes;
}
