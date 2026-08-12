package com.khedmataktak.dto;

import java.util.List;
import java.util.Map;

public record PortfolioViewModel(
        String lang,
        String slug,
        String theme,
        String photoUrl,
        String fullName,
        String title,
        String summary,
        String contactEmail,
        String contactPhone,
        String contactLocation,
        Map<String, String> socialLinks,
        String canonicalUrl,
        String metaDescription,
        String ogTitle,
        String ogDescription,
        String ogImage,
        List<ExperienceView> experiences,
        List<ProjectView> projects,
        List<EducationView> educations,
        List<SkillView> skills,
        List<LanguageView> languages,
        List<CertificationView> certifications
) {

    public record ExperienceView(
            String company,
            String role,
            String location,
            String startDate,
            String endDate,
            boolean currentPosition,
            String description,
            List<String> highlights
    ) {
    }

    public record ProjectView(
            String title,
            String description,
            String url,
            String githubUrl,
            List<String> imageUrls,
            List<String> technologies,
            List<String> highlights
    ) {
    }

    public record EducationView(
            String institution,
            String degree,
            String field,
            String startDate,
            String endDate,
            String description
    ) {
    }

    public record SkillView(String name, String category, Integer level) {
    }

    public record LanguageView(String language, String level) {
    }

    public record CertificationView(
            String name,
            String issuer,
            String issueDate,
            String expiryDate,
            String credentialUrl
    ) {
    }
}
