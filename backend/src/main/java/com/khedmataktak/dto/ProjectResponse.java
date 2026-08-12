package com.khedmataktak.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ProjectResponse(
        Long id,
        Map<String, String> title,
        Map<String, String> description,
        String url,
        String githubUrl,
        List<String> imageUrls,
        Map<String, List<String>> technologies,
        Map<String, List<String>> highlights,
        int sortOrder,
        Instant createdAt,
        Instant updatedAt
) {
}
