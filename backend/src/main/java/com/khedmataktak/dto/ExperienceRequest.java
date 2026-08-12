package com.khedmataktak.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record ExperienceRequest(
        @NotEmpty Map<@Size(max = 10) String, @Size(max = 500) String> company,
        @NotEmpty Map<@Size(max = 10) String, @Size(max = 500) String> role,
        Map<@Size(max = 10) String, @Size(max = 500) String> location,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        boolean currentPosition,
        Map<@Size(max = 10) String, @Size(max = 5000) String> description,
        Map<@Size(max = 10) String, List<@Size(max = 500) String>> highlights,
        int sortOrder
) {
}
