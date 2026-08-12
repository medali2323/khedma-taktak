package com.khedmataktak.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Map;
import com.khedmataktak.entity.EducationType;

public record EducationRequest(
        @NotEmpty Map<@Size(max = 10) String, @Size(max = 500) String> institution,
        @NotEmpty Map<@Size(max = 10) String, @Size(max = 500) String> degree,
        Map<@Size(max = 10) String, @Size(max = 500) String> field,
        LocalDate startDate,
        LocalDate endDate,
        Map<@Size(max = 10) String, @Size(max = 5000) String> description,
        EducationType educationType,
        int sortOrder
) {
}
