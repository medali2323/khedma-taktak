package com.khedmataktak.service;

import java.util.UUID;

import com.khedmataktak.dto.ExperienceRequest;
import com.khedmataktak.dto.ExperienceResponse;
import com.khedmataktak.entity.Experience;
import com.khedmataktak.entity.Profile;
import com.khedmataktak.exception.ResourceNotFoundException;
import com.khedmataktak.repository.ExperienceRepository;
import java.util.HashMap;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final ProfileService profileService;

    public ExperienceService(ExperienceRepository experienceRepository, ProfileService profileService) {
        this.experienceRepository = experienceRepository;
        this.profileService = profileService;
    }

    @Transactional(readOnly = true)
    public List<ExperienceResponse> list(UUID userId) {
        Profile profile = profileService.findByUserId(userId);
        return experienceRepository.findByProfileIdOrderBySortOrderAscStartDateDesc(profile.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExperienceResponse get(UUID userId, Long id) {
        return toResponse(findOwned(userId, id));
    }

    @Transactional
    public ExperienceResponse create(UUID userId, ExperienceRequest request) {
        Profile profile = profileService.findByUserId(userId);
        Experience experience = mapToEntity(new Experience(), request);
        experience.setProfile(profile);
        return toResponse(experienceRepository.save(experience));
    }

    @Transactional
    public ExperienceResponse update(UUID userId, Long id, ExperienceRequest request) {
        Experience experience = findOwned(userId, id);
        mapToEntity(experience, request);
        return toResponse(experienceRepository.save(experience));
    }

    @Transactional
    public void delete(UUID userId, Long id) {
        Experience experience = findOwned(userId, id);
        experienceRepository.delete(experience);
    }

    @Transactional(readOnly = true)
    public List<Experience> findByProfileId(Long profileId) {
        return experienceRepository.findByProfileIdOrderBySortOrderAscStartDateDesc(profileId);
    }

    private Experience findOwned(UUID userId, Long id) {
        Profile profile = profileService.findByUserId(userId);
        return experienceRepository.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found"));
    }

    private Experience mapToEntity(Experience experience, ExperienceRequest request) {
        experience.setCompany(request.company());
        experience.setRole(request.role());
        experience.setLocation(request.location() != null ? request.location() : new HashMap<>());
        experience.setStartDate(request.startDate());
        experience.setEndDate(request.endDate());
        experience.setCurrentPosition(request.currentPosition());
        experience.setDescription(request.description() != null ? request.description() : new HashMap<>());
        experience.setHighlights(request.highlights() != null ? request.highlights() : new HashMap<>());
        experience.setSortOrder(request.sortOrder());
        return experience;
    }

    ExperienceResponse toResponse(Experience experience) {
        return new ExperienceResponse(
                experience.getId(),
                experience.getCompany(),
                experience.getRole(),
                experience.getLocation(),
                experience.getStartDate(),
                experience.getEndDate(),
                experience.isCurrentPosition(),
                experience.getDescription(),
                experience.getHighlights(),
                experience.getSortOrder(),
                experience.getCreatedAt(),
                experience.getUpdatedAt()
        );
    }
}
