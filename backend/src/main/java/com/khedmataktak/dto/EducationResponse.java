package com.khedmataktak.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import com.khedmataktak.entity.EducationType;

public record EducationResponse(
        Long id,
        Map<String, String> institution,
        Map<String, String> degree,
        Map<String, String> field,
        LocalDate startDate,
        LocalDate endDate,
        Map<String, String> description,
        EducationType educationType,
        int sortOrder,
        Instant createdAt,
        Instant updatedAt
) {
}
