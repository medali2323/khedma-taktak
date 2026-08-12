package com.khedmataktak.service;

import java.util.UUID;

import com.khedmataktak.dto.LanguageSkillRequest;
import com.khedmataktak.dto.LanguageSkillResponse;
import com.khedmataktak.entity.LanguageSkill;
import com.khedmataktak.entity.Profile;
import com.khedmataktak.exception.ResourceNotFoundException;
import com.khedmataktak.repository.LanguageSkillRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LanguageSkillService {

    private final LanguageSkillRepository languageSkillRepository;
    private final ProfileService profileService;

    public LanguageSkillService(LanguageSkillRepository languageSkillRepository,
                                ProfileService profileService) {
        this.languageSkillRepository = languageSkillRepository;
        this.profileService = profileService;
    }

    @Transactional(readOnly = true)
    public List<LanguageSkillResponse> list(UUID userId) {
        Profile profile = profileService.findByUserId(userId);
        return languageSkillRepository.findByProfileIdOrderBySortOrderAsc(profile.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public LanguageSkillResponse get(UUID userId, Long id) {
        return toResponse(findOwned(userId, id));
    }

    @Transactional
    public LanguageSkillResponse create(UUID userId, LanguageSkillRequest request) {
        Profile profile = profileService.findByUserId(userId);
        LanguageSkill languageSkill = mapToEntity(new LanguageSkill(), request);
        languageSkill.setProfile(profile);
        return toResponse(languageSkillRepository.save(languageSkill));
    }

    @Transactional
    public LanguageSkillResponse update(UUID userId, Long id, LanguageSkillRequest request) {
        LanguageSkill languageSkill = findOwned(userId, id);
        mapToEntity(languageSkill, request);
        return toResponse(languageSkillRepository.save(languageSkill));
    }

    @Transactional
    public void delete(UUID userId, Long id) {
        LanguageSkill languageSkill = findOwned(userId, id);
        languageSkillRepository.delete(languageSkill);
    }

    @Transactional(readOnly = true)
    public List<LanguageSkill> findByProfileId(Long profileId) {
        return languageSkillRepository.findByProfileIdOrderBySortOrderAsc(profileId);
    }

    private LanguageSkill findOwned(UUID userId, Long id) {
        Profile profile = profileService.findByUserId(userId);
        return languageSkillRepository.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Language skill not found"));
    }

    private LanguageSkill mapToEntity(LanguageSkill languageSkill, LanguageSkillRequest request) {
        languageSkill.setLanguage(request.language());
        languageSkill.setLevel(request.level());
        languageSkill.setSortOrder(request.sortOrder());
        return languageSkill;
    }

    LanguageSkillResponse toResponse(LanguageSkill languageSkill) {
        return new LanguageSkillResponse(
                languageSkill.getId(),
                languageSkill.getLanguage(),
                languageSkill.getLevel(),
                languageSkill.getSortOrder(),
                languageSkill.getCreatedAt(),
                languageSkill.getUpdatedAt()
        );
    }
}
