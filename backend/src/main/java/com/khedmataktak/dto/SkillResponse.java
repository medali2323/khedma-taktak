package com.khedmataktak.dto;

import java.time.Instant;
import java.util.Map;

public record SkillResponse(
        Long id,
        Map<String, String> name,
        Map<String, String> category,
        Integer level,
        int sortOrder,
        Instant createdAt,
        Instant updatedAt
) {
}
