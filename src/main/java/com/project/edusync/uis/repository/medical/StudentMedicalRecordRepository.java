package com.project.edusync.uis.repository.medical;

import com.project.edusync.uis.model.entity.medical.StudentMedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentMedicalRecordRepository extends JpaRepository<StudentMedicalRecord, Long> {
    Optional<StudentMedicalRecord> findByStudent_Id(Long studentId);
}
