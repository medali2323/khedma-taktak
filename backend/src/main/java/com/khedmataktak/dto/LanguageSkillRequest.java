package com.khedmataktak.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record LanguageSkillRequest(
        @NotEmpty Map<@Size(max = 10) String, @Size(max = 500) String> language,
        @NotEmpty Map<@Size(max = 10) String, @Size(max = 500) String> level,
        int sortOrder
) {
}
