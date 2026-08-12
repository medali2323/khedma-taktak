package com.khedmataktak.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import com.khedmataktak.entity.CertificationType;

public record CertificationResponse(
        Long id,
        Map<String, String> name,
        Map<String, String> issuer,
        LocalDate issueDate,
        LocalDate expiryDate,
        String credentialUrl,
        CertificationType certificationType,
        int sortOrder,
        Instant createdAt,
        Instant updatedAt
) {
}
