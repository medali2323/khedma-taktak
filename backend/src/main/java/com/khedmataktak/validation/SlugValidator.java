package com.khedmataktak.validation;

import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SlugValidator {

    public static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9-]{3,30}$");

    private static final Set<String> RESERVED_SLUGS = Set.of(
            "admin", "api", "login", "register", "logout", "auth", "refresh",
            "www", "static", "assets", "uploads", "u", "profile", "profiles",
            "settings", "dashboard", "help", "support", "about", "terms",
            "privacy", "export", "cv", "preview", "health", "actuator"
    );

    public boolean isValidFormat(String slug) {
        return slug != null && SLUG_PATTERN.matcher(slug).matches();
    }

    public boolean isReserved(String slug) {
        return slug != null && RESERVED_SLUGS.contains(slug.toLowerCase());
    }

    public void validate(String slug) {
        if (!isValidFormat(slug)) {
            throw new IllegalArgumentException(
                    "Slug must match pattern ^[a-z0-9-]{3,30}$");
        }
        if (isReserved(slug)) {
            throw new IllegalArgumentException("Slug is reserved");
        }
    }
}
