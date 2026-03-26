package com.project.edusync.common.config;

import com.project.edusync.adm.model.entity.AcademicClass;
import com.project.edusync.adm.model.entity.Section;
import com.project.edusync.adm.repository.AcademicClassRepository;
import com.project.edusync.iam.model.entity.Permission;
import com.project.edusync.iam.model.entity.Role;
import com.project.edusync.iam.repository.PermissionRepository;
import com.project.edusync.iam.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment; // Import the Environment class
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This component runs once on application startup.
 * It checks if the spring.jpa.hibernate.ddl-auto property is set to 'create'.
 * If it is, it populates the database with foundational data (Roles, Classes).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AcademicClassRepository classRepository;
    private final Environment environment; // 1. Inject the Spring Environment

    private static final Map<String, List<String>> ROLE_PERMISSION_BLUEPRINT = buildRolePermissionBlueprint();

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {

        // 2. Get the ddl-auto property value from the environment
        String ddlAuto = environment.getProperty("spring.jpa.hibernate.ddl-auto");

        log.info("Running RBAC role/permission bootstrap in idempotent mode...");
        Map<String, Role> rolesByName = seedRoles();
        seedPermissionsAndRoleMappings(rolesByName);

        // Class/section fixture data should be inserted only on schema-create mode.
        if ("create".equalsIgnoreCase(ddlAuto)) {
            log.info("DDL-AUTO mode is 'create'. Running class/section seeder...");
            seedClassesAndSections();
        } else {
            log.info("DDL-AUTO mode is '{}' - skipping class/section fixture seeding.", ddlAuto);
        }

        log.info("Data seeding complete.");
    }

    private Map<String, Role> seedRoles() {
        log.info("Seeding foundational Roles (idempotent)...");

        List<String> roleNames = Arrays.asList(
                "ROLE_STUDENT",
                "ROLE_TEACHER",
                "ROLE_PRINCIPAL",
                "ROLE_LIBRARIAN",
                "ROLE_GUARDIAN",
                "ROLE_ADMIN",
                "ROLE_SUPER_ADMIN",
                "ROLE_SCHOOL_ADMIN"
        );

        Map<String, Role> rolesByName = new LinkedHashMap<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName).orElse(null);
            if (role == null) {
                Role created = createRole(roleName);
                Role saved = roleRepository.save(created);
                rolesByName.put(saved.getName(), saved);
                continue;
            }

            if (!role.isActive()) {
                role.setActive(true);
                role = roleRepository.save(role);
            }
            rolesByName.put(role.getName(), role);
        }

        log.info("Ensured {} foundational roles.", rolesByName.size());
        return rolesByName;
    }

    private Role createRole(String name) {
        Role role = new Role();
        role.setName(name);
        role.setActive(true);
        // Note: uuid, createdAt, etc., are set automatically
        // by AuditableEntity's @PrePersist and @CreatedDate
        return role;
    }

    private void seedPermissionsAndRoleMappings(Map<String, Role> rolesByName) {
        log.info("Seeding RBAC permissions and role-permission mappings...");

        List<String> permissionNames = ROLE_PERMISSION_BLUEPRINT.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();

        Map<String, Permission> permissionByName = new LinkedHashMap<>();
        for (String permissionName : permissionNames) {
            Permission permission = permissionRepository.findByName(permissionName).orElse(null);
            if (permission == null) {
                Permission created = createPermission(permissionName);
                Permission saved = permissionRepository.save(created);
                permissionByName.put(saved.getName(), saved);
                continue;
            }

            if (!permission.isActive()) {
                permission.setActive(true);
                permission = permissionRepository.save(permission);
            }
            permissionByName.put(permission.getName(), permission);
        }

        for (Map.Entry<String, List<String>> entry : ROLE_PERMISSION_BLUEPRINT.entrySet()) {
            String roleName = entry.getKey();
            Role role = rolesByName.get(roleName);
            if (role == null) {
                log.warn("Role '{}' was not found while assigning permissions. Skipping mapping.", roleName);
                continue;
            }

            Set<Permission> mappedPermissions = entry.getValue().stream()
                    .map(permissionByName::get)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toCollection(HashSet::new));

            Set<Permission> existingPermissions = role.getPermissions();
            if (existingPermissions == null) {
                existingPermissions = new HashSet<>();
                role.setPermissions(existingPermissions);
            }

            int beforeCount = existingPermissions.size();
            boolean changed = existingPermissions.addAll(mappedPermissions);
            int afterCount = existingPermissions.size();

            if (changed) {
                roleRepository.save(role);
            }
            log.info("Role {} now has {} permissions (added {})", roleName, afterCount, (afterCount - beforeCount));
        }

        log.info("Ensured {} permissions and completed role-permission mapping.", permissionByName.size());
    }

    private Permission createPermission(String name) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setActive(true);
        return permission;
    }

    private static Map<String, List<String>> buildRolePermissionBlueprint() {
        Map<String, List<String>> blueprint = new LinkedHashMap<>();

        blueprint.put("ROLE_STUDENT", List.of(
                "profile:read:own",
                "profile:update:own",
                "dashboard:read:own",
                "attendance:read:own",
                "finance:read:own",
                "timetable:read:own",
                "exam:read:own"
        ));

        blueprint.put("ROLE_GUARDIAN", List.of(
                "profile:read:own",
                "profile:update:own",
                "dashboard:read:linked",
                "attendance:read:linked",
                "finance:read:linked",
                "timetable:read:linked",
                "exam:read:linked",
                "student:read:linked"
        ));

        blueprint.put("ROLE_TEACHER", List.of(
                "profile:read:own",
                "profile:update:own",
                "dashboard:read:own",
                "attendance:mark:section",
                "attendance:update:section",
                "attendance:read:section",
                "marks:read:section",
                "marks:update:section",
                "timetable:read:own",
                "student:read:section"
        ));

        blueprint.put("ROLE_LIBRARIAN", List.of(
                "profile:read:own",
                "profile:update:own",
                "library:book:issue",
                "library:book:return",
                "library:inventory:read",
                "library:inventory:update",
                "library:member:read"
        ));

        blueprint.put("ROLE_PRINCIPAL", List.of(
                "profile:read:own",
                "dashboard:read:school",
                "users:read:all",
                "attendance:read:all",
                "finance:read:all",
                "timetable:read:all",
                "exam:read:all",
                "reports:read:school"
        ));

        blueprint.put("ROLE_SCHOOL_ADMIN", List.of(
                "profile:read:own",
                "profile:update:own",
                "profile:read:all",
                "profile:update:all",
                "dashboard:read:school",
                "users:create",
                "users:read:all",
                "users:update",
                "users:deactivate",
                "adm:class:manage",
                "adm:section:manage",
                "adm:schedule:manage",
                "attendance:config:manage",
                "finance:manage",
                "reports:read:school",
                "rbac:permission:create",
                "rbac:permission:read",
                "rbac:role-permission:assign",
                "rbac:role-permission:revoke",
                "rbac:role-permission:read"
        ));

        blueprint.put("ROLE_ADMIN", List.of(
                "profile:read:all",
                "profile:update:all",
                "dashboard:read:school",
                "users:create",
                "users:read:all",
                "users:update",
                "users:deactivate",
                "attendance:config:manage",
                "finance:manage",
                "reports:read:school",
                "rbac:permission:create",
                "rbac:permission:read",
                "rbac:role-permission:assign",
                "rbac:role-permission:revoke",
                "rbac:role-permission:read"
        ));

        blueprint.put("ROLE_SUPER_ADMIN", List.of(
                "profile:read:all",
                "profile:update:all",
                "dashboard:read:school",
                "users:create",
                "users:read:all",
                "users:update",
                "users:deactivate",
                "adm:class:manage",
                "adm:section:manage",
                "adm:schedule:manage",
                "attendance:config:manage",
                "finance:manage",
                "reports:read:school",
                "rbac:permission:create",
                "rbac:permission:read",
                "rbac:role-permission:assign",
                "rbac:role-permission:revoke",
                "rbac:role-permission:read",
                "system:settings:manage"
        ));

        return blueprint;
    }

    private void seedClassesAndSections() {
        // No .count() check needed.
        log.info("Seeding foundational Academic Classes and Sections...");

        // Use the helper to create classes and their sections in one go
        createClassWithSections("Nursery", Arrays.asList("A", "B"));
        createClassWithSections("LKG", Arrays.asList("A", "B"));
        createClassWithSections("UKG", Arrays.asList("A", "B"));

        List<String> standardSections = Arrays.asList("A", "B", "C");
        for (int i = 1; i <= 12; i++) {
            createClassWithSections("Class " + i, standardSections);
        }

        log.info("Saved {} classes and their corresponding sections.", classRepository.count());
    }

    /**
     * Helper method to create an AcademicClass and its child Sections.
     * This leverages CascadeType.ALL on the 'sections' relationship
     * in the AcademicClass entity.
     */
    private void createClassWithSections(String className, List<String> sectionNames) {
        // 1. Create the parent (AcademicClass)
        AcademicClass ac = new AcademicClass();
        ac.setName(className);
        ac.setIsActive(true);

        // 2. Create the children (Sections) and link them to the parent
        Set<Section> sections = new HashSet<>();
        for (String sectionName : sectionNames) {
            Section section = new Section();
            section.setSectionName(sectionName);
            section.setAcademicClass(ac); // Link child to parent
            sections.add(section);
        }

        // 3. Set the relationship on the parent side
        ac.setSections(sections);

        // 4. Save the parent.
        // Because of @OneToMany(mappedBy = "academicClass", cascade = CascadeType.ALL...)
        // saving the parent (AcademicClass) will automatically save all the
        // linked Section entities in the same transaction.
        classRepository.save(ac);
        log.debug("Created class: {} with sections: {}", className, sectionNames);
    }
}