package com.khedmataktak.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ProfileResponse(
        Long id,
        String slug,
        boolean published,
        String theme,
        String photoUrl,
        Map<String, String> fullName,
        Map<String, String> title,
        Map<String, String> summary,
        String contactEmail,
        String contactPhone,
        Map<String, String> contactLocation,
        Map<String, String> socialLinks,
        String userType,
        String primaryTrade,
        List<String> tradeSpecialties,
        String drivingLicense,
        boolean hasOwnVehicle,
        Integer mobilityRadiusKm,
        List<String> toolsEquipment,
        String studentInstitution,
        String studentYear,
        String internshipSought,
        Instant createdAt,
        Instant updatedAt
) {
}
