package com.khedmataktak.service;

import java.util.UUID;

import com.khedmataktak.config.AppProperties;
import com.khedmataktak.dto.ExtendedProfileFields;
import com.khedmataktak.dto.CertificationRequest;
import com.khedmataktak.dto.CertificationResponse;
import com.khedmataktak.dto.EducationRequest;
import com.khedmataktak.dto.EducationResponse;
import com.khedmataktak.dto.ExperienceRequest;
import com.khedmataktak.dto.ExperienceResponse;
import com.khedmataktak.dto.LanguageSkillRequest;
import com.khedmataktak.dto.LanguageSkillResponse;
import com.khedmataktak.dto.ProfileRequest;
import com.khedmataktak.dto.ProfileResponse;
import com.khedmataktak.dto.ProjectRequest;
import com.khedmataktak.dto.ProjectResponse;
import com.khedmataktak.dto.PublishRequest;
import com.khedmataktak.dto.SkillRequest;
import com.khedmataktak.dto.SkillResponse;
import com.khedmataktak.dto.wizard.WizardDtos.CertificationDto;
import com.khedmataktak.dto.wizard.WizardDtos.CvImportResultDto;
import com.khedmataktak.dto.wizard.WizardDtos.EducationDto;
import com.khedmataktak.dto.wizard.WizardDtos.ExperienceDto;
import com.khedmataktak.dto.wizard.WizardDtos.LanguageDto;
import com.khedmataktak.dto.wizard.WizardDtos.PortfolioDto;
import com.khedmataktak.dto.wizard.WizardDtos.ProjectDto;
import com.khedmataktak.dto.wizard.WizardDtos.PublishStatusDto;
import com.khedmataktak.dto.wizard.WizardDtos.SkillDto;
import com.khedmataktak.dto.wizard.WizardDtos.UserProfileDto;
import com.khedmataktak.entity.User;
import com.khedmataktak.entity.EducationType;
import com.khedmataktak.entity.CertificationType;
import com.khedmataktak.repository.UserRepository;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioWizardService {

    private static final Map<String, Integer> SKILL_LEVELS = Map.of(
            "Beginner", 1,
            "Intermediate", 2,
            "Advanced", 3,
            "Expert", 4,
            "Master", 5
    );

    private final ProfileService profileService;
    private final ExperienceService experienceService;
    private final ProjectService projectService;
    private final EducationService educationService;
    private final SkillService skillService;
    private final LanguageSkillService languageSkillService;
    private final CertificationService certificationService;
    private final UserRepository userRepository;
    private final AppProperties appProperties;

    public PortfolioWizardService(ProfileService profileService,
                                  ExperienceService experienceService,
                                  ProjectService projectService,
                                  EducationService educationService,
                                  SkillService skillService,
                                  LanguageSkillService languageSkillService,
                                  CertificationService certificationService,
                                  UserRepository userRepository,
                                  AppProperties appProperties) {
        this.profileService = profileService;
        this.experienceService = experienceService;
        this.projectService = projectService;
        this.educationService = educationService;
        this.skillService = skillService;
        this.languageSkillService = languageSkillService;
        this.certificationService = certificationService;
        this.userRepository = userRepository;
        this.appProperties = appProperties;
    }

    @Transactional(readOnly = true)
    public PortfolioDto getPortfolio(UUID userId) {
        profileService.ensureProfileExists(userId);
        ProfileResponse profile = profileService.getProfile(userId);
        return new PortfolioDto(
                profile.id(),
                profile.slug(),
                profile.published(),
                profile.updatedAt() != null ? profile.updatedAt().toString() : null,
                toUserProfile(userId, profile),
                listExperiences(userId),
                listProjects(userId),
                listEducation(userId),
                listSkills(userId),
                listLanguages(userId),
                listCertifications(userId)
        );
    }

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(UUID userId) {
        profileService.ensureProfileExists(userId);
        return toUserProfile(userId, profileService.getProfile(userId));
    }

    @Transactional
    public UserProfileDto saveProfile(UUID userId, UserProfileDto profile) {
        profileService.ensureProfileExists(userId);
        ProfileResponse current = profileService.getProfile(userId);
        User user = userRepository.findById(userId).orElseThrow();
        Map<String, String> socialLinks = new HashMap<>();
        putIfPresent(socialLinks, "website", profile.website());
        putIfPresent(socialLinks, "linkedin", profile.linkedin());
        putIfPresent(socialLinks, "github", profile.github());

        String fullName = joinName(profile.firstName(), profile.lastName());
        if (fullName.isBlank()) {
            fullName = localized(current.fullName());
        }
        if (fullName.isBlank()) {
            fullName = "My Portfolio";
        }

        String email = profile.email();
        if (email == null || email.isBlank()) {
            email = user.getEmail();
        }

        ProfileRequest request = new ProfileRequest(
                current.slug(),
                current.theme(),
                singleLangMap(fullName),
                singleLangMap(profile.title()),
                singleLangMap(profile.summary()),
                email,
                profile.phone(),
                singleLangMap(profile.location()),
                socialLinks
        );
        profileService.updateProfile(userId, request);
        profileService.updateExtendedFields(userId, toExtendedFields(profile));
        return toUserProfile(userId, profileService.getProfile(userId));
    }

    private ExtendedProfileFields toExtendedFields(UserProfileDto profile) {
        return new ExtendedProfileFields(
                profile.userType(),
                profile.primaryTrade(),
                splitList(profile.tradeSpecialties()),
                profile.drivingLicense(),
                profile.hasOwnVehicle(),
                profile.mobilityRadiusKm(),
                splitList(profile.toolsEquipment()),
                profile.studentInstitution(),
                profile.studentYear(),
                profile.internshipSought()
        );
    }

    private List<String> splitList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private String joinList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return String.join(", ", values);
    }

    @Transactional(readOnly = true)
    public List<ExperienceDto> listExperiences(UUID userId) {
        return experienceService.list(userId).stream().map(this::toExperience).toList();
    }

    @Transactional
    public List<ExperienceDto> saveExperiences(UUID userId, List<ExperienceDto> items) {
        List<ExperienceDto> filtered = items.stream().filter(item -> isNotBlank(item.company())).toList();
        return syncItems(
                experienceService.list(userId),
                filtered,
                ExperienceResponse::id,
                ExperienceDto::id,
                (item, index) -> toExperienceRequest(item, index),
                (id, request) -> experienceService.update(userId, id, request),
                request -> experienceService.create(userId, request),
                id -> experienceService.delete(userId, id),
                this::toExperience
        );
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> listProjects(UUID userId) {
        return projectService.list(userId).stream().map(this::toProject).toList();
    }

    @Transactional
    public List<ProjectDto> saveProjects(UUID userId, List<ProjectDto> items) {
        List<ProjectDto> filtered = items.stream().filter(item -> isNotBlank(item.name())).toList();
        return syncItems(
                projectService.list(userId),
                filtered,
                ProjectResponse::id,
                ProjectDto::id,
                this::toProjectRequest,
                (id, request) -> projectService.update(userId, id, request),
                request -> projectService.create(userId, request),
                id -> projectService.delete(userId, id),
                this::toProject
        );
    }

    @Transactional(readOnly = true)
    public List<EducationDto> listEducation(UUID userId) {
        return educationService.list(userId).stream().map(this::toEducation).toList();
    }

    @Transactional
    public List<EducationDto> saveEducation(UUID userId, List<EducationDto> items) {
        List<EducationDto> filtered = items.stream().filter(item -> isNotBlank(item.institution())).toList();
        return syncItems(
                educationService.list(userId),
                filtered,
                EducationResponse::id,
                EducationDto::id,
                this::toEducationRequest,
                (id, request) -> educationService.update(userId, id, request),
                request -> educationService.create(userId, request),
                id -> educationService.delete(userId, id),
                this::toEducation
        );
    }

    @Transactional(readOnly = true)
    public List<SkillDto> listSkills(UUID userId) {
        return skillService.list(userId).stream().map(this::toSkill).toList();
    }

    @Transactional
    public List<SkillDto> saveSkills(UUID userId, List<SkillDto> items) {
        List<SkillDto> filtered = items.stream().filter(item -> isNotBlank(item.name())).toList();
        return syncItems(
                skillService.list(userId),
                filtered,
                SkillResponse::id,
                SkillDto::id,
                this::toSkillRequest,
                (id, request) -> skillService.update(userId, id, request),
                request -> skillService.create(userId, request),
                id -> skillService.delete(userId, id),
                this::toSkill
        );
    }

    @Transactional(readOnly = true)
    public List<LanguageDto> listLanguages(UUID userId) {
        return languageSkillService.list(userId).stream().map(this::toLanguage).toList();
    }

    @Transactional
    public List<LanguageDto> saveLanguages(UUID userId, List<LanguageDto> items) {
        List<LanguageDto> filtered = items.stream().filter(item -> isNotBlank(item.name())).toList();
        return syncItems(
                languageSkillService.list(userId),
                filtered,
                LanguageSkillResponse::id,
                LanguageDto::id,
                this::toLanguageRequest,
                (id, request) -> languageSkillService.update(userId, id, request),
                request -> languageSkillService.create(userId, request),
                id -> languageSkillService.delete(userId, id),
                this::toLanguage
        );
    }

    @Transactional(readOnly = true)
    public List<CertificationDto> listCertifications(UUID userId) {
        return certificationService.list(userId).stream().map(this::toCertification).toList();
    }

    @Transactional
    public List<CertificationDto> saveCertifications(UUID userId, List<CertificationDto> items) {
        List<CertificationDto> filtered = items.stream().filter(item -> isNotBlank(item.name())).toList();
        return syncItems(
                certificationService.list(userId),
                filtered,
                CertificationResponse::id,
                CertificationDto::id,
                this::toCertificationRequest,
                (id, request) -> certificationService.update(userId, id, request),
                request -> certificationService.create(userId, request),
                id -> certificationService.delete(userId, id),
                this::toCertification
        );
    }

    @Transactional(readOnly = true)
    public PublishStatusDto getPublishStatus(UUID userId) {
        profileService.ensureProfileExists(userId);
        ProfileResponse profile = profileService.getProfile(userId);
        return toPublishStatus(profile);
    }

    @Transactional
    public PublishStatusDto publish(UUID userId) {
        ProfileResponse profile = profileService.togglePublish(userId, new PublishRequest(true));
        return toPublishStatus(profile);
    }

    @Transactional
    public PublishStatusDto unpublish(UUID userId) {
        ProfileResponse profile = profileService.togglePublish(userId, new PublishRequest(false));
        return toPublishStatus(profile);
    }

    @Transactional
    public PortfolioDto importCvAndSave(UUID userId, CvImportResultDto imported) {
        applyImportedCv(userId, imported);
        return getPortfolio(userId);
    }

    private void applyImportedCv(UUID userId, CvImportResultDto imported) {
        if (imported.profile() != null) {
            saveProfile(userId, imported.profile());
        }
        if (imported.experiences() != null && !imported.experiences().isEmpty()) {
            saveExperiences(userId, imported.experiences());
        }
        if (imported.projects() != null && !imported.projects().isEmpty()) {
            saveProjects(userId, imported.projects());
        }
        if (imported.education() != null && !imported.education().isEmpty()) {
            saveEducation(userId, imported.education());
        }
        if (imported.skills() != null && !imported.skills().isEmpty()) {
            saveSkills(userId, imported.skills());
        }
        if (imported.languages() != null && !imported.languages().isEmpty()) {
            saveLanguages(userId, imported.languages());
        }
        if (imported.certifications() != null && !imported.certifications().isEmpty()) {
            saveCertifications(userId, imported.certifications());
        }
    }

    private PublishStatusDto toPublishStatus(ProfileResponse profile) {
        String previewUrl = profile.published()
                ? appProperties.getPublicBaseUrl() + "/u/" + profile.slug()
                : appProperties.getPublicBaseUrl() + "/api/render/preview";
        return new PublishStatusDto(
                profile.published(),
                profile.slug(),
                profile.updatedAt() != null ? profile.updatedAt().toString() : null,
                previewUrl
        );
    }

    private UserProfileDto toUserProfile(UUID userId, ProfileResponse profile) {
        User user = userRepository.findById(userId).orElseThrow();
        String fullName = localized(profile.fullName());
        String firstName;
        String lastName;
        if (fullName.isBlank() || "My Portfolio".equals(fullName.trim())) {
            firstName = nullToEmpty(user.getFirstName());
            lastName = nullToEmpty(user.getLastName());
        } else {
            String[] nameParts = splitName(fullName);
            firstName = nameParts[0];
            lastName = nameParts[1];
        }
        Map<String, String> socialLinks = profile.socialLinks() != null ? profile.socialLinks() : Map.of();
        return new UserProfileDto(
                profile.id(),
                firstName,
                lastName,
                profile.contactEmail() != null ? profile.contactEmail() : user.getEmail(),
                localized(profile.title()),
                localized(profile.summary()),
                nullToEmpty(profile.contactPhone()),
                localized(profile.contactLocation()),
                socialLinks.getOrDefault("website", ""),
                socialLinks.getOrDefault("linkedin", ""),
                socialLinks.getOrDefault("github", ""),
                profile.userType() != null ? profile.userType() : user.getUserType().name(),
                nullToEmpty(profile.photoUrl()),
                nullToEmpty(profile.primaryTrade()),
                joinList(profile.tradeSpecialties()),
                nullToEmpty(profile.drivingLicense()),
                profile.hasOwnVehicle(),
                profile.mobilityRadiusKm(),
                joinList(profile.toolsEquipment()),
                nullToEmpty(profile.studentInstitution()),
                nullToEmpty(profile.studentYear()),
                nullToEmpty(profile.internshipSought())
        );
    }

    private ExperienceDto toExperience(ExperienceResponse response) {
        return new ExperienceDto(
                response.id(),
                localized(response.company()),
                localized(response.role()),
                localized(response.location()),
                formatDate(response.startDate()),
                formatDate(response.endDate()),
                response.currentPosition(),
                localized(response.description())
        );
    }

    private ProjectDto toProject(ProjectResponse response) {
        return new ProjectDto(
                response.id(),
                localized(response.title()),
                localized(response.description()),
                nullToEmpty(response.url()),
                technologiesToString(response.technologies()),
                "",
                ""
        );
    }

    private EducationDto toEducation(EducationResponse response) {
        return new EducationDto(
                response.id(),
                localized(response.institution()),
                localized(response.degree()),
                localized(response.field()),
                formatDate(response.startDate()),
                formatDate(response.endDate()),
                localized(response.description()),
                response.educationType() != null ? response.educationType().name() : EducationType.ACADEMIC.name()
        );
    }

    private SkillDto toSkill(SkillResponse response) {
        return new SkillDto(
                response.id(),
                localized(response.name()),
                skillLevelToString(response.level()),
                localized(response.category())
        );
    }

    private LanguageDto toLanguage(LanguageSkillResponse response) {
        return new LanguageDto(
                response.id(),
                localized(response.language()),
                localized(response.level())
        );
    }

    private CertificationDto toCertification(CertificationResponse response) {
        return new CertificationDto(
                response.id(),
                localized(response.name()),
                localized(response.issuer()),
                formatDate(response.issueDate()),
                nullToEmpty(response.credentialUrl()),
                response.certificationType() != null ? response.certificationType().name() : CertificationType.PROFESSIONAL.name()
        );
    }

    private ExperienceRequest toExperienceRequest(ExperienceDto item, int sortOrder) {
        return new ExperienceRequest(
                requiredLangMap(item.company()),
                requiredLangMap(item.position(), item.company()),
                singleLangMap(item.location()),
                parseDateRequired(item.startDate()),
                parseDate(item.endDate()),
                item.current(),
                singleLangMap(item.description()),
                Map.of(),
                sortOrder
        );
    }

    private ProjectRequest toProjectRequest(ProjectDto item, int sortOrder) {
        return new ProjectRequest(
                requiredLangMap(item.name()),
                singleLangMap(item.description()),
                nullToEmpty(item.url()),
                "",
                List.of(),
                technologiesFromString(item.technologies()),
                Map.of(),
                sortOrder
        );
    }

    private EducationRequest toEducationRequest(EducationDto item, int sortOrder) {
        return new EducationRequest(
                requiredLangMap(item.institution()),
                requiredLangMap(item.degree(), item.institution()),
                singleLangMap(item.field()),
                parseDate(item.startDate()),
                parseDate(item.endDate()),
                singleLangMap(item.description()),
                parseEducationType(item.educationType()),
                sortOrder
        );
    }

    private EducationType parseEducationType(String value) {
        if (value == null || value.isBlank()) {
            return EducationType.ACADEMIC;
        }
        try {
            return EducationType.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return EducationType.ACADEMIC;
        }
    }

    private SkillRequest toSkillRequest(SkillDto item, int sortOrder) {
        return new SkillRequest(
                requiredLangMap(item.name()),
                singleLangMap(item.category()),
                skillLevelToInt(item.level()),
                sortOrder
        );
    }

    private LanguageSkillRequest toLanguageRequest(LanguageDto item, int sortOrder) {
        return new LanguageSkillRequest(
                requiredLangMap(item.name()),
                requiredLangMap(item.proficiency(), "Conversational"),
                sortOrder
        );
    }

    private CertificationRequest toCertificationRequest(CertificationDto item, int sortOrder) {
        return new CertificationRequest(
                requiredLangMap(item.name()),
                singleLangMap(item.issuer()),
                parseDate(item.date()),
                null,
                nullToEmpty(item.url()),
                parseCertificationType(item.certificationType()),
                sortOrder
        );
    }

    private CertificationType parseCertificationType(String value) {
        if (value == null || value.isBlank()) {
            return CertificationType.PROFESSIONAL;
        }
        try {
            return CertificationType.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return CertificationType.PROFESSIONAL;
        }
    }

    private <E, W, R> List<W> syncItems(List<E> existing,
                                         List<W> incoming,
                                         Function<E, Long> existingId,
                                         Function<W, Long> incomingId,
                                         BiFunction<W, Integer, R> toRequest,
                                         BiFunction<Long, R, E> update,
                                         Function<R, E> create,
                                         Consumer<Long> delete,
                                         Function<E, W> toWizard) {
        Set<Long> keepIds = new HashSet<>();
        List<W> saved = new ArrayList<>();

        for (int index = 0; index < incoming.size(); index++) {
            W item = incoming.get(index);
            R request = toRequest.apply(item, index);
            Long id = incomingId.apply(item);
            E entity = id != null ? update.apply(id, request) : create.apply(request);
            keepIds.add(existingId.apply(entity));
            saved.add(toWizard.apply(entity));
        }

        for (E entity : existing) {
            Long id = existingId.apply(entity);
            if (!keepIds.contains(id)) {
                delete.accept(id);
            }
        }

        return saved;
    }

    private Map<String, String> singleLangMap(String value) {
        Map<String, String> map = new HashMap<>();
        if (value != null && !value.isBlank()) {
            map.put("en", value.trim());
            map.put("fr", value.trim());
        }
        return map;
    }

    private Map<String, String> requiredLangMap(String value) {
        Map<String, String> map = singleLangMap(value);
        if (map.isEmpty()) {
            map.put("en", "-");
            map.put("fr", "-");
        }
        return map;
    }

    private Map<String, String> requiredLangMap(String value, String fallback) {
        Map<String, String> map = singleLangMap(value);
        if (!map.isEmpty()) {
            return map;
        }
        return requiredLangMap(fallback);
    }

    private Map<String, List<String>> technologiesFromString(String tech) {
        if (tech == null || tech.isBlank()) {
            return new HashMap<>();
        }
        List<String> items = Arrays.stream(tech.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        Map<String, List<String>> map = new HashMap<>();
        map.put("en", items);
        map.put("fr", items);
        return map;
    }

    private String technologiesToString(Map<String, List<String>> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        List<String> list = map.containsKey("en") ? map.get("en") : map.values().iterator().next();
        return String.join(", ", list);
    }

    private String localized(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        if (map.containsKey("en")) {
            return nullToEmpty(map.get("en"));
        }
        return nullToEmpty(map.values().iterator().next());
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return new String[]{"", ""};
        }
        int space = fullName.trim().indexOf(' ');
        if (space < 0) {
            return new String[]{fullName.trim(), ""};
        }
        return new String[]{fullName.substring(0, space).trim(), fullName.substring(space + 1).trim()};
    }

    private String joinName(String firstName, String lastName) {
        String first = nullToEmpty(firstName).trim();
        String last = nullToEmpty(lastName).trim();
        if (first.isEmpty()) {
            return last;
        }
        if (last.isEmpty()) {
            return first;
        }
        return first + " " + last;
    }

    private Integer skillLevelToInt(String level) {
        return SKILL_LEVELS.getOrDefault(level, 2);
    }

    private String skillLevelToString(Integer level) {
        if (level == null) {
            return "Intermediate";
        }
        return switch (level) {
            case 1 -> "Beginner";
            case 3 -> "Advanced";
            case 4 -> "Expert";
            case 5 -> "Master";
            default -> "Intermediate";
        };
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private LocalDate parseDateRequired(String value) {
        LocalDate date = parseDate(value);
        return date != null ? date : LocalDate.now();
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.toString() : "";
    }

    private void putIfPresent(Map<String, String> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value.trim());
        }
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
