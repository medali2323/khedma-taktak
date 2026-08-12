package com.khedmataktak.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

public record ProjectRequest(
        @NotEmpty Map<@Size(max = 10) String, @Size(max = 500) String> title,
        Map<@Size(max = 10) String, @Size(max = 5000) String> description,
        @Size(max = 500) String url,
        @Size(max = 500) String githubUrl,
        List<@Size(max = 500) String> imageUrls,
        Map<@Size(max = 10) String, List<@Size(max = 100) String>> technologies,
        Map<@Size(max = 10) String, List<@Size(max = 500) String>> highlights,
        int sortOrder
) {
}
