package com.project.edusync.iam.repository;

import com.project.edusync.iam.model.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Integer> {

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    List<Permission> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    List<Permission> findAllByOrderByNameAsc();
}
