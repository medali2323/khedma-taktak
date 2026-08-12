package com.khedmataktak.service;

import java.util.UUID;

import com.khedmataktak.dto.EducationRequest;
import com.khedmataktak.dto.EducationResponse;
import com.khedmataktak.entity.Education;
import com.khedmataktak.entity.EducationType;
import com.khedmataktak.entity.Profile;
import com.khedmataktak.exception.ResourceNotFoundException;
import com.khedmataktak.repository.EducationRepository;
import java.util.HashMap;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EducationService {

    private final EducationRepository educationRepository;
    private final ProfileService profileService;

    public EducationService(EducationRepository educationRepository, ProfileService profileService) {
        this.educationRepository = educationRepository;
        this.profileService = profileService;
    }

    @Transactional(readOnly = true)
    public List<EducationResponse> list(UUID userId) {
        Profile profile = profileService.findByUserId(userId);
        return educationRepository.findByProfileIdOrderBySortOrderAscStartDateDesc(profile.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EducationResponse get(UUID userId, Long id) {
        return toResponse(findOwned(userId, id));
    }

    @Transactional
    public EducationResponse create(UUID userId, EducationRequest request) {
        Profile profile = profileService.findByUserId(userId);
        Education education = mapToEntity(new Education(), request);
        education.setProfile(profile);
        return toResponse(educationRepository.save(education));
    }

    @Transactional
    public EducationResponse update(UUID userId, Long id, EducationRequest request) {
        Education education = findOwned(userId, id);
        mapToEntity(education, request);
        return toResponse(educationRepository.save(education));
    }

    @Transactional
    public void delete(UUID userId, Long id) {
        Education education = findOwned(userId, id);
        educationRepository.delete(education);
    }

    @Transactional(readOnly = true)
    public List<Education> findByProfileId(Long profileId) {
        return educationRepository.findByProfileIdOrderBySortOrderAscStartDateDesc(profileId);
    }

    private Education findOwned(UUID userId, Long id) {
        Profile profile = profileService.findByUserId(userId);
        return educationRepository.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Education not found"));
    }

    private Education mapToEntity(Education education, EducationRequest request) {
        education.setInstitution(request.institution());
        education.setDegree(request.degree());
        education.setField(request.field() != null ? request.field() : new HashMap<>());
        education.setStartDate(request.startDate());
        education.setEndDate(request.endDate());
        education.setDescription(request.description() != null ? request.description() : new HashMap<>());
        education.setEducationType(request.educationType() != null ? request.educationType() : EducationType.ACADEMIC);
        education.setSortOrder(request.sortOrder());
        return education;
    }

    EducationResponse toResponse(Education education) {
        return new EducationResponse(
                education.getId(),
                education.getInstitution(),
                education.getDegree(),
                education.getField(),
                education.getStartDate(),
                education.getEndDate(),
                education.getDescription(),
                education.getEducationType(),
                education.getSortOrder(),
                education.getCreatedAt(),
                education.getUpdatedAt()
        );
    }
}
