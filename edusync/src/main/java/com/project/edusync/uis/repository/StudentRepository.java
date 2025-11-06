package com.project.edusync.uis.repository;

import com.project.edusync.uis.model.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface StudentRepository extends JpaRepository<Student,Long> {
    boolean existsByEnrollmentNumber(String enrollmentNumber);

    @Query("SELECT s.enrollmentNumber FROM Student s WHERE s.enrollmentNumber IN :numbers")
    Set<String> findNumbersThatExist(@Param("numbers") Set<String> numbers);
}
