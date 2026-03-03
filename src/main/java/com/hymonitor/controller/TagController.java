package com.hymonitor.controller;

import com.hymonitor.dto.TagRequest;
import com.hymonitor.dto.TagResponse;
import com.hymonitor.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for tag management
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * Get all tags
     * @return list of tags
     */
    @GetMapping
    public ResponseEntity<List<TagResponse>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    /**
     * Create a new tag
     * @param request tag creation request
     * @return created tag
     */
    @PostMapping
    public ResponseEntity<TagResponse> createTag(@Valid @RequestBody TagRequest request) {
        return ResponseEntity.ok(tagService.createTag(request));
    }

    /**
     * Update an existing tag
     * @param id tag ID
     * @param request tag update request
     * @return updated tag
     */
    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> updateTag(
            @PathVariable UUID id,
            @Valid @RequestBody TagRequest request) {
        return ResponseEntity.ok(tagService.updateTag(id, request));
    }

    /**
     * Delete a tag
     * @param id tag ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
