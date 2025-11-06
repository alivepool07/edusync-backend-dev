package com.project.edusync.enrollment.service.impl;

import com.opencsv.exceptions.CsvException;
import com.project.edusync.adm.model.entity.Section;
import com.project.edusync.adm.repository.SectionRepository;
import com.project.edusync.enrollment.model.dto.BulkImportReportDTO;
import com.project.edusync.enrollment.service.BulkImportService;
import com.project.edusync.enrollment.util.CsvValidationHelper;
import com.project.edusync.enrollment.util.RegisterUserByRole;
import com.project.edusync.iam.model.entity.Role;
import com.project.edusync.iam.repository.RoleRepository;
import com.project.edusync.iam.repository.UserRepository;
import com.project.edusync.uis.model.entity.Staff;
import com.project.edusync.uis.model.entity.Student;
import com.project.edusync.uis.model.entity.UserProfile;
import com.project.edusync.uis.model.enums.Department;
import com.project.edusync.uis.model.enums.Gender;
import com.project.edusync.uis.model.enums.StaffType;
import com.project.edusync.uis.repository.StaffRepository;
import com.project.edusync.uis.repository.StudentRepository;
import com.project.edusync.uis.repository.UserProfileRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementation for bulk user import via CSV.
 * This implementation is **resilient** (processing one row at a time) and
 * **optimized** (pre-caches static data like Roles and Sections).
 *
 * It orchestrates the import by:
 * 1. Pre-fetching and caching static data (Roles, Sections) for performance.
 * 2. Looping through the CSV file one row at a time.
 * 3. Calling a separate, transactional method for each row.
 * 4. Wrapping each row's processing in a try-catch block to ensure that
 * one bad row does not stop the entire import.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BulkImportServiceImpl implements BulkImportService {

    // --- Constants ---
    private static final String USER_TYPE_STUDENTS = "students";
    private static final String USER_TYPE_STAFF = "staff";
    private static final String ROLE_STUDENT = "ROLE_STUDENT";

    @Value("${edusync.bulk-import.default-password:Welcome@123}")
    private String DEFAULT_PASSWORD;

    // --- Repositories & Services (all final) ---
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserProfileRepository userProfileRepository;
    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;
    private final SectionRepository sectionRepository;
    private final PasswordEncoder passwordEncoder;
    private final CsvValidationHelper validationHelper;
    private final RegisterUserByRole registerUserByRole; // Helper for saving

    /**
     * Orchestrates the import process.
     * This method is **NOT** transactional itself.
     * It builds the performance caches, then loops through the CSV,
     * delegating the *actual* transactional work to other methods.
     *
     * @param file The multipart CSV file uploaded by the user.
     * @param userType A string ("students" or "staff") indicating the import type.
     * @return A DTO report summarizing successes and failures.
     * @throws IOException if the file cannot be read.
     */
    @Override
    public BulkImportReportDTO importUsers(MultipartFile file, String userType) throws IOException {

        // --- 1. PRE-FETCH CACHES (The Optimization) ---
        // We fetch static data (that won't change mid-import) *once*
        // to avoid N+1 queries inside the loop.
        log.info("Building caches for roles and sections...");

        // Cache all Roles by their name (e.g., "ROLE_STUDENT")
        final Map<String, Role> roleCache = roleRepository.findAll().stream()
                .collect(Collectors.toMap(Role::getName, role -> role));

        // Cache all Sections by a composite key (e.g., "Class 10:A")
        // We use findAllWithClass() for an efficient "JOIN FETCH"
        final Map<String, Section> sectionCache = sectionRepository.findAllWithClass().stream()
                .collect(Collectors.toMap(
                        s -> s.getAcademicClass().getName() + ":" + s.getSectionName(),
                        s -> s
                ));
        log.info("Caches built with {} roles and {} sections. Starting row processing...", roleCache.size(), sectionCache.size());

        // --- 2. Resilient Row-by-Row Loop ---
        BulkImportReportDTO report = new BulkImportReportDTO();
        report.setStatus("PROCESSING");
        int rowNumber = 1, successCount = 0, failureCount = 0;

        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            String[] header = csvReader.readNext(); // Read and skip header
            if (header == null) {
                throw new IllegalArgumentException("File is empty or header is missing.");
            }

            String[] row;
            // This is the heart of the "Resilient" model.
            // We loop, and each row's success or failure is independent.
            while ((row = csvReader.readNext()) != null) {
                rowNumber++;
                try {
                    // This is the "Orchestrator" try-catch block.
                    // It will catch any exception bubbled up from
                    // processStudentRow, processStaffRow, or registerUserByRole.
                    if (USER_TYPE_STUDENTS.equalsIgnoreCase(userType)) {
                        processStudentRow(row, roleCache, sectionCache);
                        successCount++;
                    } else if (USER_TYPE_STAFF.equalsIgnoreCase(userType)) {
                        processStaffRow(row, roleCache);
                        successCount++;
                    } else {
                        throw new IllegalArgumentException("Invalid userType: " + userType);
                    }
                } catch (Exception e) {
                    // If one row fails, we log it, add it to the report,
                    // and continue to the next row.
                    failureCount++;
                    String errorMessage = e.getMessage(); // Get the specific error
                    String error = String.format("Row %d: %s", rowNumber, errorMessage);
                    report.getErrorMessages().add(error);
                    log.warn("Failed to process row {}: {}", rowNumber, errorMessage);
                }
            }
        } catch (CsvValidationException e) { // Catches a badly formatted CSV file
            report.setStatus("FAILED");
            report.getErrorMessages().add("File is not a valid CSV: " + e.getMessage());
            return report;
        }

        // --- 3. Final Report Generation ---
        report.setStatus("COMPLETED");
        report.setTotalRows(rowNumber - 1); // -1 for the header
        report.setSuccessCount(successCount);
        report.setFailureCount(failureCount);
        return report;
    }


    /**
     * Processes and validates a single student row.
     * This method is marked @Transactional, so all DB checks and
     * the final save (inside the helper) are part of *one* transaction.
     * If this fails, the transaction is rolled back for this row *only*.
     *
     * @param row The raw String[] from the CSV.
     * @param roleCache The pre-fetched Role map.
     * @param sectionCache The pre-fetched Section map.
     * @throws Exception if any validation or database constraint fails.
     */
    @Transactional(rollbackFor = Exception.class)
    public void processStudentRow(String[] row, Map<String, Role> roleCache, Map<String, Section> sectionCache) throws Exception {
        // 1. --- Parse & Validate Data (per students.csv spec) ---
        String firstName = validationHelper.validateString(row[0], "firstName");
        String lastName = validationHelper.validateString(row[1], "lastName");
        String middleName = row[2]; // Optional field
        String email = validationHelper.validateEmail(row[3]);
        LocalDate dob = validationHelper.parseDate(row[4], "dateOfBirth");
        Integer rollNo = Integer.parseInt(row[5]);
        Gender gender = validationHelper.parseEnum(Gender.class, row[6], "gender");
        String enrollmentNumber = validationHelper.validateString(row[7], "enrollmentNumber");
        LocalDate enrollmentDate = validationHelper.parseDate(row[8], "enrollmentDate");
        String className = validationHelper.validateString(row[9], "className");
        String sectionName = validationHelper.validateString(row[10], "sectionName");

        // 2. --- Validate Business Logic & Foreign Keys ---

        // (These two DB calls MUST stay in the transactional loop
        // to prevent race conditions and ensure row-level integrity)
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email '" + email + "' already exists.");
        }
        if (studentRepository.existsByEnrollmentNumber(enrollmentNumber)) {
            throw new IllegalArgumentException("Student with enrollment number '" + enrollmentNumber + "' already exists.");
        }

        // (These two calls are now fast, in-memory lookups from the cache)
        Section section = sectionCache.get(className + ":" + sectionName);
        if (section == null) {
            throw new IllegalArgumentException("Section not found for class '" + className + "' and section '" + sectionName + "'.");
        }

        Role studentRole = roleCache.get(ROLE_STUDENT);
        if(studentRole == null) {
            throw new RuntimeException("CRITICAL: " + ROLE_STUDENT + " not found in database.");
        }

        // 3. --- Delegate creation to the helper ---
        // The helper class will now perform the save operations,
        // all within the transaction started by this method.
        registerUserByRole.RegisterStudent(
                email, enrollmentNumber, DEFAULT_PASSWORD, studentRole,
                firstName, lastName, middleName, dob, gender,
                enrollmentDate, section, rollNo
        );
    }


    /**
     * Processes and validates a single staff row.
     * This method is marked @Transactional.
     *
     * @param row The raw String[] from the CSV.
     * @param roleCache The pre-fetched Role map.
     * @throws Exception if any validation or database constraint fails.
     */
    @Transactional(rollbackFor = Exception.class)
    public void processStaffRow(String[] row, Map<String, Role> roleCache) throws Exception {
        // 1. --- Parse & Validate Data (per staff.csv spec) ---
        String firstName = validationHelper.validateString(row[0], "firstName");
        String lastName = validationHelper.validateString(row[1], "lastName");
        String middleName = row[2]; // Optional
        String email = validationHelper.validateEmail(row[3]);
        LocalDate dob = validationHelper.parseDate(row[4], "dateOfBirth");
        Gender gender = validationHelper.parseEnum(Gender.class, row[5], "gender");
        String employeeId = validationHelper.validateString(row[6], "employeeId");
        LocalDate joiningDate = validationHelper.parseDate(row[7], "joiningDate");
        String jobTitle = validationHelper.validateString(row[8], "jobTitle");
        Department department = validationHelper.parseEnum(Department.class, row[9], "department");
        StaffType staffType = validationHelper.parseEnum(StaffType.class, row[10], "staffType");

        // 2. --- Validate Business Logic & Foreign Keys ---

        // (DB calls that must remain in the transaction)
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email '" + email + "' already exists.");
        }
        if (staffRepository.existsByEmployeeId(employeeId)) {
            throw new IllegalArgumentException("Staff with employee ID '" + employeeId + "' already exists.");
        }

        // (Fast, in-memory lookup from the cache)
        String roleName = "ROLE_" + staffType.name();
        Role staffRole = roleCache.get(roleName);
        if(staffRole == null) {
            throw new RuntimeException("CRITICAL: Role '" + roleName + "' not found in database.");
        }

        // 3. --- Delegate creation to the helper ---
        // The helper will parse the *rest* of the row (row[11], row[12], etc.)
        // and save all entities within this transaction.
        registerUserByRole.RegisterStaff(
                email, employeeId, DEFAULT_PASSWORD, staffRole,
                firstName, lastName, middleName, dob, gender,
                joiningDate, jobTitle, department, staffType,
                row
        );
    }
}