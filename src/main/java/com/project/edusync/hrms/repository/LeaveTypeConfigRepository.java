package com.project.edusync.hrms.repository;

import com.project.edusync.hrms.model.entity.LeaveTypeConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveTypeConfigRepository extends JpaRepository<LeaveTypeConfig, Long> {

    List<LeaveTypeConfig> findByActiveTrueOrderBySortOrderAscLeaveCodeAsc();

    Optional<LeaveTypeConfig> findByLeaveCodeIgnoreCaseAndActiveTrue(String leaveCode);

    Optional<LeaveTypeConfig> findByLeaveCodeIgnoreCase(String leaveCode);

    boolean existsByLeaveCodeIgnoreCaseAndActiveTrue(String leaveCode);

    boolean existsByLeaveCodeIgnoreCaseAndActiveTrueAndIdNot(String leaveCode, Long id);
}

