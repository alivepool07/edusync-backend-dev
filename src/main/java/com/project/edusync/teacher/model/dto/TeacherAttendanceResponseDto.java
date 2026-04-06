package com.project.edusync.teacher.model.dto;

import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

@Value
public class TeacherAttendanceResponseDto {
    UUID studentUuid;
    LocalDate date;
    Boolean present;
    String recordedBy;
}


