package com.project.edusync.ams.model.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Builder
public record ShiftResponseDTO(
        Long id,
        String uuid,
        String shiftName,
        LocalTime startTime,
        LocalTime endTime,
        Integer graceMinutes,
        List<Integer> applicableDays,
        Boolean isDefault,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

