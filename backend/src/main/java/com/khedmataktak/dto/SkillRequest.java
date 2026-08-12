package com.khedmataktak.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record SkillRequest(
        @NotEmpty Map<@Size(max = 10) String, @Size(max = 500) String> name,
        Map<@Size(max = 10) String, @Size(max = 500) String> category,
        @Min(1) @Max(5) Integer level,
        int sortOrder
) {
}
