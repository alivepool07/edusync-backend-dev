package com.project.edusync.ams.model.repository;

import com.project.edusync.ams.model.entity.AttendanceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceTypeRepository extends JpaRepository<AttendanceType, Long> {

    /**
     * Finds an attendance type by its unique short code (e.g., 'P', 'A', 'UA').
     * Essential for system logic and quick data retrieval.
     */
    Optional<AttendanceType> findByShortCode(String shortCode);

    /**
     * Finds an attendance type by its unique short code (e.g., 'P', 'A', 'UA').
     * Case-insensitive lookup is convenient for API clients that may send 'p' or 'P'.
     */
    Optional<AttendanceType> findByShortCodeIgnoreCase(String shortCode);

    /**
     * Finds an attendance type by its unique full name (e.g., 'Present', 'Excused Absence').
     */
    Optional<AttendanceType> findByTypeName(String typeName);

    /**
     * Case-insensitive contains search on typeName (e.g., "exc" -> "Excused").
     * This is used as a fallback when searching for an 'Excused' attendance type.
     */
    Optional<AttendanceType> findByTypeNameContainingIgnoreCase(String namePart);

    /**
     * Retrieves all attendance types that mark a student/staff as present.
     */
    List<AttendanceType> findByIsPresentMarkTrue();

    /**
     * Retrieves all attendance types that mark a student/staff as absent (for reporting/metrics).
     */
    List<AttendanceType> findByIsAbsenceMarkTrue();

    /**
     * Retrieves all attendance types that mark a student/staff as late.
     */
    List<AttendanceType> findByIsLateMarkTrue();

    /**
     * Finds a single active attendance type by its primary key.
     * @param id The internal ID of the Attendance Type.
     * @return Optional containing the active Attendance Type.
     */
    Optional<AttendanceType> findByIdAndIsActiveTrue(Long id);

    Optional<AttendanceType> findByUuidAndIsActiveTrue(UUID uuid);

    /**
     * Finds an active attendance type by its unique short code (e.g., 'P', 'A').
     */
    Optional<AttendanceType> findByShortCodeAndIsActiveTrue(String shortCode);

    /**
     * Finds an active attendance type by its unique full name.
     */
    Optional<AttendanceType> findByTypeNameAndIsActiveTrue(String typeName);

    /**
     * Retrieves all active attendance types for use in UI/logic.
     */
    List<AttendanceType> findByIsActiveTrue();
}
