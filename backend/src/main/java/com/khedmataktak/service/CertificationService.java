package com.khedmataktak.service;

import java.util.UUID;

import com.khedmataktak.dto.CertificationRequest;
import com.khedmataktak.dto.CertificationResponse;
import com.khedmataktak.entity.Certification;
import com.khedmataktak.entity.CertificationType;
import com.khedmataktak.entity.Profile;
import com.khedmataktak.exception.ResourceNotFoundException;
import com.khedmataktak.repository.CertificationRepository;
import java.util.HashMap;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CertificationService {

    private final CertificationRepository certificationRepository;
    private final ProfileService profileService;

    public CertificationService(CertificationRepository certificationRepository,
                                ProfileService profileService) {
        this.certificationRepository = certificationRepository;
        this.profileService = profileService;
    }

    @Transactional(readOnly = true)
    public List<CertificationResponse> list(UUID userId) {
        Profile profile = profileService.findByUserId(userId);
        return certificationRepository.findByProfileIdOrderBySortOrderAscIssueDateDesc(profile.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CertificationResponse get(UUID userId, Long id) {
        return toResponse(findOwned(userId, id));
    }

    @Transactional
    public CertificationResponse create(UUID userId, CertificationRequest request) {
        Profile profile = profileService.findByUserId(userId);
        Certification certification = mapToEntity(new Certification(), request);
        certification.setProfile(profile);
        return toResponse(certificationRepository.save(certification));
    }

    @Transactional
    public CertificationResponse update(UUID userId, Long id, CertificationRequest request) {
        Certification certification = findOwned(userId, id);
        mapToEntity(certification, request);
        return toResponse(certificationRepository.save(certification));
    }

    @Transactional
    public void delete(UUID userId, Long id) {
        Certification certification = findOwned(userId, id);
        certificationRepository.delete(certification);
    }

    @Transactional(readOnly = true)
    public List<Certification> findByProfileId(Long profileId) {
        return certificationRepository.findByProfileIdOrderBySortOrderAscIssueDateDesc(profileId);
    }

    private Certification findOwned(UUID userId, Long id) {
        Profile profile = profileService.findByUserId(userId);
        return certificationRepository.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found"));
    }

    private Certification mapToEntity(Certification certification, CertificationRequest request) {
        certification.setName(request.name());
        certification.setIssuer(request.issuer() != null ? request.issuer() : new HashMap<>());
        certification.setIssueDate(request.issueDate());
        certification.setExpiryDate(request.expiryDate());
        certification.setCredentialUrl(request.credentialUrl());
        certification.setCertificationType(
                request.certificationType() != null ? request.certificationType() : CertificationType.PROFESSIONAL);
        certification.setSortOrder(request.sortOrder());
        return certification;
    }

    CertificationResponse toResponse(Certification certification) {
        return new CertificationResponse(
                certification.getId(),
                certification.getName(),
                certification.getIssuer(),
                certification.getIssueDate(),
                certification.getExpiryDate(),
                certification.getCredentialUrl(),
                certification.getCertificationType(),
                certification.getSortOrder(),
                certification.getCreatedAt(),
                certification.getUpdatedAt()
        );
    }
}
