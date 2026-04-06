package com.project.edusync.teacher.service;

import com.project.edusync.iam.model.entity.User;
import com.project.edusync.iam.repository.UserRepository;
import com.project.edusync.teacher.model.dto.TeacherAttendanceMarkRequestDto;
import com.project.edusync.teacher.model.dto.TeacherAttendanceResponseDto;
import com.project.edusync.teacher.model.entity.Attendance;
import com.project.edusync.teacher.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    public List<TeacherAttendanceResponseDto> markAttendance(List<TeacherAttendanceMarkRequestDto> attendanceList) {
        List<Attendance> entities = attendanceList.stream()
                .map(this::toEntity)
                .toList();
        return attendanceRepository.saveAll(entities).stream().map(this::toDto).toList();
    }

    public List<TeacherAttendanceResponseDto> getAttendanceByDate(LocalDate date, String teacherUsername) {
        return attendanceRepository.findByDateAndRecordedBy(date, teacherUsername)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private Attendance toEntity(TeacherAttendanceMarkRequestDto req) {
        User student = userRepository.findByUuid(req.getStudentUuid())
                .orElseThrow(() -> new IllegalArgumentException("Student user not found for uuid: " + req.getStudentUuid()));

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setDate(req.getDate());
        attendance.setPresent(req.getPresent());
        attendance.setRecordedBy(req.getRecordedBy());
        return attendance;
    }

    private TeacherAttendanceResponseDto toDto(Attendance attendance) {
        UUID studentUuid = attendance.getStudent() == null ? null : attendance.getStudent().getUuid();
        return new TeacherAttendanceResponseDto(
                studentUuid,
                attendance.getDate(),
                attendance.getPresent(),
                attendance.getRecordedBy()
        );
    }
}