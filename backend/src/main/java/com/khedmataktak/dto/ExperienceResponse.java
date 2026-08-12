package com.khedmataktak.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record ExperienceResponse(
        Long id,
        Map<String, String> company,
        Map<String, String> role,
        Map<String, String> location,
        LocalDate startDate,
        LocalDate endDate,
        boolean currentPosition,
        Map<String, String> description,
        Map<String, List<String>> highlights,
        int sortOrder,
        Instant createdAt,
        Instant updatedAt
) {
}
