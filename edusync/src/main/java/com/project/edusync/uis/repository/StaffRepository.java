package com.project.edusync.uis.repository;

import com.project.edusync.uis.model.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface StaffRepository extends JpaRepository<Staff,Long> {
    boolean existsByEmployeeId(String employeeId);

    @Query("SELECT s.employeeId FROM Staff s WHERE s.employeeId IN :employeeIds")
    Set<String> findEmployeeIdsThatExist(@Param("employeeIds") Set<String> employeeIds);
}
