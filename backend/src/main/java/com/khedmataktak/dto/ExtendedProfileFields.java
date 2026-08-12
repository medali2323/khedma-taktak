package com.khedmataktak.dto;

import java.util.List;

public record ExtendedProfileFields(
        String userType,
        String primaryTrade,
        List<String> tradeSpecialties,
        String drivingLicense,
        boolean hasOwnVehicle,
        Integer mobilityRadiusKm,
        List<String> toolsEquipment,
        String studentInstitution,
        String studentYear,
        String internshipSought
) {
}
