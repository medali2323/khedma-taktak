package com.khedmataktak.dto.wizard;

import java.util.List;

public final class WizardDtos {

    private WizardDtos() {
    }

    public record UserProfileDto(
            Long id,
            String firstName,
            String lastName,
            String email,
            String title,
            String summary,
            String phone,
            String location,
            String website,
            String linkedin,
            String github,
            String userType,
            String photoUrl,
            String primaryTrade,
            String tradeSpecialties,
            String drivingLicense,
            boolean hasOwnVehicle,
            Integer mobilityRadiusKm,
            String toolsEquipment,
            String studentInstitution,
            String studentYear,
            String internshipSought
    ) {
    }

    public record ExperienceDto(
            Long id,
            String company,
            String position,
            String location,
            String startDate,
            String endDate,
            boolean current,
            String description
    ) {
    }

    public record ProjectDto(
            Long id,
            String name,
            String description,
            String url,
            String technologies,
            String startDate,
            String endDate
    ) {
    }

    public record EducationDto(
            Long id,
            String institution,
            String degree,
            String field,
            String startDate,
            String endDate,
            String description,
            String educationType
    ) {
    }

    public record SkillDto(
            Long id,
            String name,
            String level,
            String category
    ) {
    }

    public record LanguageDto(
            Long id,
            String name,
            String proficiency
    ) {
    }

    public record CertificationDto(
            Long id,
            String name,
            String issuer,
            String date,
            String url,
            String certificationType
    ) {
    }

    public record PortfolioDto(
            Long id,
            String slug,
            boolean published,
            String publishedAt,
            UserProfileDto profile,
            List<ExperienceDto> experiences,
            List<ProjectDto> projects,
            List<EducationDto> education,
            List<SkillDto> skills,
            List<LanguageDto> languages,
            List<CertificationDto> certifications
    ) {
    }

    public record PublishStatusDto(
            boolean published,
            String slug,
            String publishedAt,
            String previewUrl
    ) {
    }

    public record CvImportResultDto(
            UserProfileDto profile,
            List<ExperienceDto> experiences,
            List<ProjectDto> projects,
            List<EducationDto> education,
            List<SkillDto> skills,
            List<LanguageDto> languages,
            List<CertificationDto> certifications,
            String parserEngine,
            String parserNote
    ) {
    }

    public record CvImportProgressEvent(
            String phase,
            int progress,
            String message,
            String section,
            Boolean found,
            Integer count,
            CvImportResultDto result
    ) {
    }

    public record CvParserInfoDto(
            boolean ollamaEnabled,
            boolean ollamaReachable,
            boolean modelAvailable,
            String configuredModel,
            String parserMode,
            String skillsLanguagesSource,
            String statusMessage
    ) {
    }

    public static UserProfileDto emptyProfile() {
        return new UserProfileDto(
                null, "", "", "", "", "", "", "", "", "", "",
                "CANDIDATE", null, null, null, null, false, null, null, null, null, null
        );
    }

    public static UserProfileDto parsedProfile(String firstName,
                                               String lastName,
                                               String email,
                                               String title,
                                               String summary,
                                               String phone,
                                               String location,
                                               String website,
                                               String linkedin,
                                               String github) {
        return new UserProfileDto(
                null,
                firstName != null ? firstName : "",
                lastName != null ? lastName : "",
                email != null ? email : "",
                title != null ? title : "",
                summary != null ? summary : "",
                phone != null ? phone : "",
                location != null ? location : "",
                website != null ? website : "",
                linkedin != null ? linkedin : "",
                github != null ? github : "",
                "CANDIDATE", null, null, null, null, false, null, null, null, null, null
        );
    }
}
