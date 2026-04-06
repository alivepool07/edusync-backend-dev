package com.project.edusync.teacher.controller;

import com.project.edusync.teacher.model.dto.TeacherAttendanceMarkRequestDto;
import com.project.edusync.teacher.model.dto.TeacherAttendanceResponseDto;
import com.project.edusync.teacher.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/teacher/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/mark")
    public ResponseEntity<List<TeacherAttendanceResponseDto>> markAttendance(@Valid @RequestBody List<TeacherAttendanceMarkRequestDto> attendanceList) {
        return ResponseEntity.ok(attendanceService.markAttendance(attendanceList));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<TeacherAttendanceResponseDto>> getAttendanceByDate(
            @PathVariable LocalDate date,
            @RequestParam String teacherUsername) {
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(date, teacherUsername));
    }
}