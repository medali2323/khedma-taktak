package com.khedmataktak.dto;

import com.khedmataktak.validation.ValidSlug;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record ProfileRequest(
        @ValidSlug String slug,
        @Size(max = 50) String theme,
        @NotEmpty Map<@Size(max = 10) String, @Size(max = 500) String> fullName,
        Map<@Size(max = 10) String, @Size(max = 500) String> title,
        Map<@Size(max = 10) String, @Size(max = 5000) String> summary,
        @Size(max = 255) String contactEmail,
        @Size(max = 50) String contactPhone,
        Map<@Size(max = 50) String, @Size(max = 500) String> contactLocation,
        Map<@Size(max = 50) String, @Size(max = 500) String> socialLinks
) {
}
