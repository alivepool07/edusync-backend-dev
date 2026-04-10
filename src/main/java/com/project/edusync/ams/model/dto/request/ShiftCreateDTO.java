package com.project.edusync.ams.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class ShiftCreateDTO {
    @NotBlank
    private String shiftName;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @Min(0)
    @Max(180)
    private Integer graceMinutes = 0;

    @NotNull
    private List<Integer> applicableDays;

    private Boolean isDefault = false;
}

