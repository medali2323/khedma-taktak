package com.khedmataktak.service;

import java.util.UUID;

import com.khedmataktak.config.AppProperties;
import com.khedmataktak.dto.PortfolioViewModel;
import com.khedmataktak.entity.Certification;
import com.khedmataktak.entity.Education;
import com.khedmataktak.entity.Experience;
import com.khedmataktak.entity.LanguageSkill;
import com.khedmataktak.entity.Profile;
import com.khedmataktak.entity.Project;
import com.khedmataktak.entity.Skill;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioDataService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ProfileService profileService;
    private final ExperienceService experienceService;
    private final ProjectService projectService;
    private final EducationService educationService;
    private final SkillService skillService;
    private final LanguageSkillService languageSkillService;
    private final CertificationService certificationService;
    private final AppProperties appProperties;

    public PortfolioDataService(ProfileService profileService,
                                ExperienceService experienceService,
                                ProjectService projectService,
                                EducationService educationService,
                                SkillService skillService,
                                LanguageSkillService languageSkillService,
                                CertificationService certificationService,
                                AppProperties appProperties) {
        this.profileService = profileService;
        this.experienceService = experienceService;
        this.projectService = projectService;
        this.educationService = educationService;
        this.skillService = skillService;
        this.languageSkillService = languageSkillService;
        this.certificationService = certificationService;
        this.appProperties = appProperties;
    }

    @Transactional(readOnly = true)
    public PortfolioViewModel buildViewModel(Profile profile, String lang) {
        String resolvedLang = resolveLang(lang);
        Long profileId = profile.getId();

        String fullName = localized(profile.getFullName(), resolvedLang);
        String title = localized(profile.getTitle(), resolvedLang);
        String summary = localized(profile.getSummary(), resolvedLang);
        String location = localized(profile.getContactLocation(), resolvedLang);
        String canonicalUrl = appProperties.getPublicBaseUrl() + "/u/" + profile.getSlug();
        String metaDescription = truncate(summary, 160);

        return new PortfolioViewModel(
                resolvedLang,
                profile.getSlug(),
                profile.getTheme(),
                profile.getPhotoUrl(),
                fullName,
                title,
                summary,
                profile.getContactEmail(),
                profile.getContactPhone(),
                location,
                profile.getSocialLinks(),
                canonicalUrl,
                metaDescription,
                fullName + " | " + title,
                metaDescription,
                profile.getPhotoUrl() != null ? appProperties.getPublicBaseUrl() + profile.getPhotoUrl() : null,
                mapExperiences(experienceService.findByProfileId(profileId), resolvedLang),
                mapProjects(projectService.findByProfileId(profileId), resolvedLang),
                mapEducations(educationService.findByProfileId(profileId), resolvedLang),
                mapSkills(skillService.findByProfileId(profileId), resolvedLang),
                mapLanguages(languageSkillService.findByProfileId(profileId), resolvedLang),
                mapCertifications(certificationService.findByProfileId(profileId), resolvedLang)
        );
    }

    @Transactional(readOnly = true)
    public PortfolioViewModel buildViewModelForUser(UUID userId, String lang) {
        Profile profile = profileService.findByUserId(userId);
        return buildViewModel(profile, lang);
    }

    @Transactional(readOnly = true)
    public PortfolioViewModel buildPublishedViewModel(String slug, String lang) {
        Profile profile = profileService.findPublishedBySlug(slug);
        return buildViewModel(profile, lang);
    }

    private List<PortfolioViewModel.ExperienceView> mapExperiences(List<Experience> items, String lang) {
        return items.stream().map(e -> new PortfolioViewModel.ExperienceView(
                localized(e.getCompany(), lang),
                localized(e.getRole(), lang),
                localized(e.getLocation(), lang),
                formatDate(e.getStartDate()),
                e.isCurrentPosition() ? "Present" : formatDate(e.getEndDate()),
                e.isCurrentPosition(),
                localized(e.getDescription(), lang),
                localizedList(e.getHighlights(), lang)
        )).toList();
    }

    private List<PortfolioViewModel.ProjectView> mapProjects(List<Project> items, String lang) {
        return items.stream().map(p -> new PortfolioViewModel.ProjectView(
                localized(p.getTitle(), lang),
                localized(p.getDescription(), lang),
                p.getUrl(),
                p.getGithubUrl(),
                p.getImageUrls(),
                localizedList(p.getTechnologies(), lang),
                localizedList(p.getHighlights(), lang)
        )).toList();
    }

    private List<PortfolioViewModel.EducationView> mapEducations(List<Education> items, String lang) {
        return items.stream().map(e -> new PortfolioViewModel.EducationView(
                localized(e.getInstitution(), lang),
                localized(e.getDegree(), lang),
                localized(e.getField(), lang),
                formatDate(e.getStartDate()),
                formatDate(e.getEndDate()),
                localized(e.getDescription(), lang)
        )).toList();
    }

    private List<PortfolioViewModel.SkillView> mapSkills(List<Skill> items, String lang) {
        return items.stream().map(s -> new PortfolioViewModel.SkillView(
                localized(s.getName(), lang),
                localized(s.getCategory(), lang),
                s.getLevel()
        )).toList();
    }

    private List<PortfolioViewModel.LanguageView> mapLanguages(List<LanguageSkill> items, String lang) {
        return items.stream().map(l -> new PortfolioViewModel.LanguageView(
                localized(l.getLanguage(), lang),
                localized(l.getLevel(), lang)
        )).toList();
    }

    private List<PortfolioViewModel.CertificationView> mapCertifications(List<Certification> items, String lang) {
        return items.stream().map(c -> new PortfolioViewModel.CertificationView(
                localized(c.getName(), lang),
                localized(c.getIssuer(), lang),
                formatDate(c.getIssueDate()),
                formatDate(c.getExpiryDate()),
                c.getCredentialUrl()
        )).toList();
    }

    private String resolveLang(String lang) {
        if (lang == null || lang.isBlank()) {
            return "en";
        }
        return lang.toLowerCase().trim();
    }

    private String localized(Map<String, String> map, String lang) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        if (map.containsKey(lang)) {
            return map.get(lang);
        }
        return map.values().iterator().next();
    }

    private List<String> localizedList(Map<String, List<String>> map, String lang) {
        if (map == null || map.isEmpty()) {
            return List.of();
        }
        if (map.containsKey(lang)) {
            return map.get(lang);
        }
        return map.values().iterator().next();
    }

    private String formatDate(java.time.LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "";
    }

    private String truncate(String text, int max) {
        if (text == null || text.length() <= max) {
            return text != null ? text : "";
        }
        return text.substring(0, max - 3) + "...";
    }
}
