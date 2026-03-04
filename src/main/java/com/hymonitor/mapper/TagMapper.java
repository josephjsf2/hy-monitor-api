package com.hymonitor.mapper;

import com.hymonitor.dto.TagResponse;
import com.hymonitor.entity.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public TagResponse toResponse(Tag tag) {
        return new TagResponse(
                tag.getId().toString(),
                tag.getName(),
                tag.getColor(),
                tag.getCreatedAt()
        );
    }
}
