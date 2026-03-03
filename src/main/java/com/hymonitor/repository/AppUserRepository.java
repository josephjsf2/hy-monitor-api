package com.hymonitor.repository;

import com.hymonitor.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AppUser entity
 */
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    /**
     * Find user by username
     * @param username the username
     * @return Optional of AppUser
     */
    Optional<AppUser> findByUsername(String username);

    /**
     * Check if username already exists
     * @param username the username
     * @return true if exists
     */
    boolean existsByUsername(String username);
}
