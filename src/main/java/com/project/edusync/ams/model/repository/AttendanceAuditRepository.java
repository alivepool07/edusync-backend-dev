package com.project.edusync.ams.model.repository;

import com.project.edusync.ams.model.entity.AttendanceAudit;
import com.project.edusync.ams.model.entity.AttendanceAudit.ActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceAuditRepository extends JpaRepository<AttendanceAudit, Long> {

    /**
     * Retrieves the audit history for a specific StudentDailyAttendance record.
     * Crucial for investigating disputes or errors.
     * The ID used here is the primary key of the StudentDailyAttendance record.
     */
    List<AttendanceAudit> findByDailyAttendance_IdOrderByCreatedAtDesc(Long dailyAttendanceId);

    /**
     * Finds all changes made by a specific user (UIS FK) across all audited tables.
     */
    Page<AttendanceAudit> findByChangedByUserIdOrderByCreatedAtDesc(
            Long changedByUserId,
            Pageable pageable);

    /**
     * Finds audit entries based on the type of action performed (INSERT, UPDATE, DELETE).
     */
    Page<AttendanceAudit> findByActionTypeOrderByCreatedAtDesc(
            ActionType actionType,
            Pageable pageable);

    /**
     * Retrieves the recent activity for a specific student, useful for dashboard feed rendering.
     */
    List<AttendanceAudit> findByDailyAttendance_StudentIdOrderByCreatedAtDesc(
            Long studentId,
            Pageable pageable);
}