package com.khedmataktak.controller;

import com.khedmataktak.dto.ProfileResponse;
import com.khedmataktak.dto.wizard.WizardDtos.CertificationDto;
import com.khedmataktak.dto.wizard.WizardDtos.EducationDto;
import com.khedmataktak.dto.wizard.WizardDtos.ExperienceDto;
import com.khedmataktak.dto.wizard.WizardDtos.LanguageDto;
import com.khedmataktak.dto.wizard.WizardDtos.PortfolioDto;
import com.khedmataktak.dto.wizard.WizardDtos.ProjectDto;
import com.khedmataktak.dto.wizard.WizardDtos.PublishStatusDto;
import com.khedmataktak.dto.wizard.WizardDtos.SkillDto;
import com.khedmataktak.dto.wizard.WizardDtos.UserProfileDto;
import com.khedmataktak.security.SecurityUtils;
import com.khedmataktak.service.PortfolioWizardService;
import com.khedmataktak.service.ProfileService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioWizardService portfolioWizardService;
    private final ProfileService profileService;

    public PortfolioController(PortfolioWizardService portfolioWizardService,
                               ProfileService profileService) {
        this.portfolioWizardService = portfolioWizardService;
        this.profileService = profileService;
    }

    @GetMapping
    public PortfolioDto getPortfolio() {
        return portfolioWizardService.getPortfolio(SecurityUtils.currentUserId());
    }

    @GetMapping("/profile")
    public UserProfileDto getProfile() {
        return portfolioWizardService.getProfile(SecurityUtils.currentUserId());
    }

    @PutMapping("/profile")
    public UserProfileDto saveProfile(@RequestBody UserProfileDto profile) {
        return portfolioWizardService.saveProfile(SecurityUtils.currentUserId(), profile);
    }

    @PostMapping(value = "/profile/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileResponse uploadPhoto(@RequestPart("file") MultipartFile file) {
        return profileService.uploadPhoto(SecurityUtils.currentUserId(), file);
    }

    @GetMapping("/experiences")
    public List<ExperienceDto> getExperiences() {
        return portfolioWizardService.listExperiences(SecurityUtils.currentUserId());
    }

    @PutMapping("/experiences")
    public List<ExperienceDto> saveExperiences(@RequestBody List<ExperienceDto> experiences) {
        return portfolioWizardService.saveExperiences(SecurityUtils.currentUserId(), experiences);
    }

    @GetMapping("/projects")
    public List<ProjectDto> getProjects() {
        return portfolioWizardService.listProjects(SecurityUtils.currentUserId());
    }

    @PutMapping("/projects")
    public List<ProjectDto> saveProjects(@RequestBody List<ProjectDto> projects) {
        return portfolioWizardService.saveProjects(SecurityUtils.currentUserId(), projects);
    }

    @GetMapping("/education")
    public List<EducationDto> getEducation() {
        return portfolioWizardService.listEducation(SecurityUtils.currentUserId());
    }

    @PutMapping("/education")
    public List<EducationDto> saveEducation(@RequestBody List<EducationDto> education) {
        return portfolioWizardService.saveEducation(SecurityUtils.currentUserId(), education);
    }

    @GetMapping("/skills")
    public List<SkillDto> getSkills() {
        return portfolioWizardService.listSkills(SecurityUtils.currentUserId());
    }

    @PutMapping("/skills")
    public List<SkillDto> saveSkills(@RequestBody List<SkillDto> skills) {
        return portfolioWizardService.saveSkills(SecurityUtils.currentUserId(), skills);
    }

    @GetMapping("/languages")
    public List<LanguageDto> getLanguages() {
        return portfolioWizardService.listLanguages(SecurityUtils.currentUserId());
    }

    @PutMapping("/languages")
    public List<LanguageDto> saveLanguages(@RequestBody List<LanguageDto> languages) {
        return portfolioWizardService.saveLanguages(SecurityUtils.currentUserId(), languages);
    }

    @GetMapping("/certifications")
    public List<CertificationDto> getCertifications() {
        return portfolioWizardService.listCertifications(SecurityUtils.currentUserId());
    }

    @PutMapping("/certifications")
    public List<CertificationDto> saveCertifications(@RequestBody List<CertificationDto> certifications) {
        return portfolioWizardService.saveCertifications(SecurityUtils.currentUserId(), certifications);
    }

    @GetMapping("/publish/status")
    public PublishStatusDto getPublishStatus() {
        return portfolioWizardService.getPublishStatus(SecurityUtils.currentUserId());
    }

    @PostMapping("/publish")
    public PublishStatusDto publish() {
        return portfolioWizardService.publish(SecurityUtils.currentUserId());
    }

    @PostMapping("/unpublish")
    public PublishStatusDto unpublish() {
        return portfolioWizardService.unpublish(SecurityUtils.currentUserId());
    }
}
