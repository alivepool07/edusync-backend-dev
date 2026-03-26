package com.project.edusync.uis.repository.medical;

import com.project.edusync.uis.model.entity.medical.StudentMedicalAllergy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentMedicalAllergyRepository extends JpaRepository<StudentMedicalAllergy, Long> {
	Optional<StudentMedicalAllergy> findByIdAndMedicalRecord_Id(Long id, Long recordId);
}
