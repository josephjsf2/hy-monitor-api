package com.hymonitor.repository;

import com.hymonitor.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Tag entity
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    /**
     * Find tag by name
     * @param name the tag name
     * @return Optional of Tag
     */
    Optional<Tag> findByName(String name);

    /**
     * Check if tag name already exists
     * @param name the tag name
     * @return true if exists
     */
    boolean existsByName(String name);
}
