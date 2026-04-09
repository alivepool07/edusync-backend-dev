package com.project.edusync.em.model.repository;

import com.project.edusync.em.model.entity.AdmitCardEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdmitCardEntryRepository extends JpaRepository<AdmitCardEntry, Long> {

    @Query("""
            SELECT ace FROM AdmitCardEntry ace
            JOIN FETCH ace.admitCard ac
            JOIN FETCH ace.examSchedule es
            WHERE ac.id = :admitCardId
            ORDER BY ace.examDate ASC, ace.startTime ASC
            """)
    List<AdmitCardEntry> findByAdmitCardIdWithSchedule(@Param("admitCardId") Long admitCardId);

    @Modifying
    @Query("DELETE FROM AdmitCardEntry ace WHERE ace.examSchedule.id = :scheduleId")
    int deleteByExamScheduleId(@Param("scheduleId") Long scheduleId);
}
