package com.khedmataktak.service;

import java.util.UUID;

import com.khedmataktak.dto.SkillRequest;
import com.khedmataktak.dto.SkillResponse;
import com.khedmataktak.entity.Profile;
import com.khedmataktak.entity.Skill;
import com.khedmataktak.exception.ResourceNotFoundException;
import com.khedmataktak.repository.SkillRepository;
import java.util.HashMap;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final ProfileService profileService;

    public SkillService(SkillRepository skillRepository, ProfileService profileService) {
        this.skillRepository = skillRepository;
        this.profileService = profileService;
    }

    @Transactional(readOnly = true)
    public List<SkillResponse> list(UUID userId) {
        Profile profile = profileService.findByUserId(userId);
        return skillRepository.findByProfileIdOrderBySortOrderAsc(profile.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SkillResponse get(UUID userId, Long id) {
        return toResponse(findOwned(userId, id));
    }

    @Transactional
    public SkillResponse create(UUID userId, SkillRequest request) {
        Profile profile = profileService.findByUserId(userId);
        Skill skill = mapToEntity(new Skill(), request);
        skill.setProfile(profile);
        return toResponse(skillRepository.save(skill));
    }

    @Transactional
    public SkillResponse update(UUID userId, Long id, SkillRequest request) {
        Skill skill = findOwned(userId, id);
        mapToEntity(skill, request);
        return toResponse(skillRepository.save(skill));
    }

    @Transactional
    public void delete(UUID userId, Long id) {
        Skill skill = findOwned(userId, id);
        skillRepository.delete(skill);
    }

    @Transactional(readOnly = true)
    public List<Skill> findByProfileId(Long profileId) {
        return skillRepository.findByProfileIdOrderBySortOrderAsc(profileId);
    }

    private Skill findOwned(UUID userId, Long id) {
        Profile profile = profileService.findByUserId(userId);
        return skillRepository.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
    }

    private Skill mapToEntity(Skill skill, SkillRequest request) {
        skill.setName(request.name());
        skill.setCategory(request.category() != null ? request.category() : new HashMap<>());
        skill.setLevel(request.level());
        skill.setSortOrder(request.sortOrder());
        return skill;
    }

    SkillResponse toResponse(Skill skill) {
        return new SkillResponse(
                skill.getId(),
                skill.getName(),
                skill.getCategory(),
                skill.getLevel(),
                skill.getSortOrder(),
                skill.getCreatedAt(),
                skill.getUpdatedAt()
        );
    }
}
