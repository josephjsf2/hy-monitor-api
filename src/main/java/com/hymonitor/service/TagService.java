package com.hymonitor.service;

import com.hymonitor.dto.TagRequest;
import com.hymonitor.dto.TagResponse;
import com.hymonitor.entity.Tag;
import com.hymonitor.exception.DuplicateResourceException;
import com.hymonitor.exception.ResourceNotFoundException;
import com.hymonitor.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for tag management operations
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TagService {

    private final TagRepository tagRepository;

    /**
     * Get all tags
     * @return list of tag responses
     */
    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Create a new tag
     * @param request tag creation request
     * @return created tag response
     */
    public TagResponse createTag(TagRequest request) {
        if (tagRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Tag name already exists");
        }
        Tag tag = Tag.builder()
                .name(request.getName())
                .color(request.getColor())
                .build();
        tagRepository.save(tag);
        return toResponse(tag);
    }

    /**
     * Update an existing tag
     * @param id tag ID
     * @param request tag update request
     * @return updated tag response
     */
    public TagResponse updateTag(UUID id, TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));

        // Check uniqueness if name changed
        if (!tag.getName().equals(request.getName()) && tagRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Tag name already exists");
        }

        tag.setName(request.getName());
        tag.setColor(request.getColor());
        tagRepository.save(tag);
        return toResponse(tag);
    }

    /**
     * Delete a tag
     * @param id tag ID
     */
    public void deleteTag(UUID id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));

        // Clear associations from all websites before deleting
        tag.getWebsites().forEach(w -> w.getTags().remove(tag));
        tagRepository.delete(tag);
    }

    /**
     * Convert Tag entity to TagResponse DTO
     * @param tag the tag entity
     * @return tag response DTO
     */
    private TagResponse toResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId().toString())
                .name(tag.getName())
                .color(tag.getColor())
                .createdAt(tag.getCreatedAt())
                .build();
    }
}
