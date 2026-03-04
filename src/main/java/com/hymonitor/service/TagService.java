package com.hymonitor.service;

import com.hymonitor.dto.TagRequest;
import com.hymonitor.dto.TagResponse;
import com.hymonitor.entity.Tag;
import com.hymonitor.exception.DuplicateResourceException;
import com.hymonitor.exception.ResourceNotFoundException;
import com.hymonitor.mapper.TagMapper;
import com.hymonitor.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Cacheable(value = "tags")
    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tagMapper::toResponse)
                .toList();
    }

    @CacheEvict(value = "tags", allEntries = true)
    public TagResponse createTag(TagRequest request) {
        if (tagRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Tag name already exists");
        }
        Tag tag = Tag.builder()
                .name(request.name())
                .color(request.color())
                .build();
        tagRepository.save(tag);
        return tagMapper.toResponse(tag);
    }

    @CacheEvict(value = "tags", allEntries = true)
    public TagResponse updateTag(UUID id, TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));

        if (!tag.getName().equals(request.name()) && tagRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Tag name already exists");
        }

        tag.setName(request.name());
        tag.setColor(request.color());
        tagRepository.save(tag);
        return tagMapper.toResponse(tag);
    }

    @CacheEvict(value = "tags", allEntries = true)
    public void deleteTag(UUID id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        tagRepository.delete(tag);
    }
}
