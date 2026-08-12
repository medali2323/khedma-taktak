package com.khedmataktak.service;

import com.khedmataktak.config.StorageProperties;
import com.khedmataktak.dto.ExtendedProfileFields;
import com.khedmataktak.dto.ProfileRequest;
import com.khedmataktak.dto.ProfileResponse;
import com.khedmataktak.dto.PublishRequest;
import com.khedmataktak.entity.Profile;
import com.khedmataktak.entity.User;
import com.khedmataktak.entity.UserType;
import com.khedmataktak.exception.ConflictException;
import com.khedmataktak.exception.ResourceNotFoundException;
import com.khedmataktak.repository.ProfileRepository;
import com.khedmataktak.repository.UserRepository;
import com.khedmataktak.validation.SlugValidator;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final SlugValidator slugValidator;
    private final LocalFileStorageService fileStorageService;
    private final StorageProperties storageProperties;

    public ProfileService(ProfileRepository profileRepository,
                          UserRepository userRepository,
                          SlugValidator slugValidator,
                          LocalFileStorageService fileStorageService,
                          StorageProperties storageProperties) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.slugValidator = slugValidator;
        this.fileStorageService = fileStorageService;
        this.storageProperties = storageProperties;
    }

    @PostConstruct
    void ensureUploadDir() throws IOException {
        Files.createDirectories(Path.of(storageProperties.getUploadDir()));
    }

    @Transactional
    public Profile createDefaultProfile(User user, String firstName, String lastName, UserType userType) {
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setSlug(generateDefaultSlug(user));
        Map<String, String> fullName = new HashMap<>();
        String name = joinName(firstName, lastName);
        if (name.isBlank()) {
            name = "My Portfolio";
        }
        fullName.put("en", name);
        fullName.put("fr", name);
        profile.setFullName(fullName);
        profile.setContactEmail(user.getEmail());
        Profile saved = profileRepository.save(profile);

        if (userType != null && userType != user.getUserType()) {
            user.setUserType(userType);
            userRepository.save(user);
        }
        return saved;
    }

    @Transactional
    public Profile createDefaultProfile(User user) {
        return createDefaultProfile(user, null, null, user.getUserType());
    }

    @Transactional
    public ProfileResponse updateExtendedFields(UUID userId, ExtendedProfileFields fields) {
        Profile profile = findByUserId(userId);
        User user = profile.getUser();

        if (fields.userType() != null && !fields.userType().isBlank()) {
            try {
                user.setUserType(UserType.valueOf(fields.userType()));
                userRepository.save(user);
            } catch (IllegalArgumentException ignored) {
                // keep existing type
            }
        }

        profile.setPrimaryTrade(trimToNull(fields.primaryTrade()));
        profile.setTradeSpecialties(fields.tradeSpecialties());
        profile.setDrivingLicense(trimToNull(fields.drivingLicense()));
        profile.setHasOwnVehicle(fields.hasOwnVehicle());
        profile.setMobilityRadiusKm(fields.mobilityRadiusKm());
        profile.setToolsEquipment(fields.toolsEquipment());
        profile.setStudentInstitution(trimToNull(fields.studentInstitution()));
        profile.setStudentYear(trimToNull(fields.studentYear()));
        profile.setInternshipSought(trimToNull(fields.internshipSought()));

        return toResponse(profileRepository.save(profile));
    }

    private String joinName(String firstName, String lastName) {
        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";
        if (first.isEmpty()) {
            return last;
        }
        if (last.isEmpty()) {
            return first;
        }
        return first + " " + last;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String generateDefaultSlug(User user) {
        String compact = user.getId().toString().replace("-", "");
        String base = "u-" + compact.substring(0, 8);
        if (!profileRepository.existsBySlug(base)) {
            return base;
        }
        return base + "-" + compact.substring(8, 14);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        Profile profile = findByUserId(userId);
        return toResponse(profile);
    }

    @Transactional
    public ProfileResponse updateProfile(UUID userId, ProfileRequest request) {
        Profile profile = findByUserId(userId);
        slugValidator.validate(request.slug());

        if (profileRepository.existsBySlugAndIdNot(request.slug(), profile.getId())) {
            throw new ConflictException("Slug already taken");
        }

        profile.setSlug(request.slug());
        if (request.theme() != null) {
            profile.setTheme(request.theme());
        }
        profile.setFullName(request.fullName());
        profile.setTitle(nullToEmpty(request.title()));
        profile.setSummary(nullToEmpty(request.summary()));
        profile.setContactEmail(request.contactEmail());
        profile.setContactPhone(request.contactPhone());
        profile.setContactLocation(nullToEmpty(request.contactLocation()));
        profile.setSocialLinks(nullToEmpty(request.socialLinks()));

        return toResponse(profileRepository.save(profile));
    }

    @Transactional
    public ProfileResponse togglePublish(UUID userId, PublishRequest request) {
        Profile profile = findByUserId(userId);
        profile.setPublished(request.published());
        return toResponse(profileRepository.save(profile));
    }

    @Transactional
    public ProfileResponse uploadPhoto(UUID userId, MultipartFile file) {
        Profile profile = findByUserId(userId);
        String url = fileStorageService.storeProfilePhoto(userId, file);
        profile.setPhotoUrl(url);
        return toResponse(profileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public Profile findByUserId(UUID userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Profile ensureProfileExists(UUID userId) {
        Optional<Profile> existing = profileRepository.findByUserId(userId);
        if (existing.isPresent()) {
            return existing.get();
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        try {
            return createDefaultProfile(user, user.getFirstName(), user.getLastName(), user.getUserType());
        } catch (DataIntegrityViolationException ex) {
            return profileRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        }
    }

    @Transactional(readOnly = true)
    public Profile findPublishedBySlug(String slug) {
        return profileRepository.findBySlugAndPublishedTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found"));
    }

    ProfileResponse toResponse(Profile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getSlug(),
                profile.isPublished(),
                profile.getTheme(),
                profile.getPhotoUrl(),
                profile.getFullName(),
                profile.getTitle(),
                profile.getSummary(),
                profile.getContactEmail(),
                profile.getContactPhone(),
                profile.getContactLocation(),
                profile.getSocialLinks(),
                profile.getUser().getUserType().name(),
                profile.getPrimaryTrade(),
                profile.getTradeSpecialties(),
                profile.getDrivingLicense(),
                profile.isHasOwnVehicle(),
                profile.getMobilityRadiusKm(),
                profile.getToolsEquipment(),
                profile.getStudentInstitution(),
                profile.getStudentYear(),
                profile.getInternshipSought(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    private Map<String, String> nullToEmpty(Map<String, String> map) {
        return map != null ? map : new HashMap<>();
    }
}
