package com.project.edusync.iam.repository;

import com.project.edusync.iam.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Integer> {
    UserDetails findByUsername(String username);

    User findUserByUsername(String username);

    /**
     * Finds a user by their username and eagerly fetches their roles
     * and the permissions associated with those roles in a single query.
     * This is the most efficient way to load the full security context.
     *
     * @param username The username to search for.
     * @return An Optional containing the User if found.
     */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH r.permissions p " +
            "WHERE u.username = :username")
    Optional<User> findByUsernameWithAuthorities(@Param("username") String username);

    boolean existsByEmail(String email);

    @Query("SELECT u.email FROM User u WHERE u.email IN :emails")
    Set<String> findEmailsThatExist(@Param("emails") Set<String> emails);
}
