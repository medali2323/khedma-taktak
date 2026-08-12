package com.khedmataktak.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Map;
import com.khedmataktak.entity.CertificationType;

public record CertificationRequest(
        @NotEmpty Map<@Size(max = 10) String, @Size(max = 500) String> name,
        Map<@Size(max = 10) String, @Size(max = 500) String> issuer,
        LocalDate issueDate,
        LocalDate expiryDate,
        @Size(max = 500) String credentialUrl,
        CertificationType certificationType,
        int sortOrder
) {
}
