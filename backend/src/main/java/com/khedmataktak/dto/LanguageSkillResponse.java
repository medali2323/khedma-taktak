package com.khedmataktak.dto;

import java.time.Instant;
import java.util.Map;

public record LanguageSkillResponse(
        Long id,
        Map<String, String> language,
        Map<String, String> level,
        int sortOrder,
        Instant createdAt,
        Instant updatedAt
) {
}
