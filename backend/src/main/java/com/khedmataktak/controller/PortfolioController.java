package com.khedmataktak.controller;

import com.khedmataktak.dto.ProfileResponse;
import com.khedmataktak.dto.wizard.WizardDtos.CertificationDto;
import com.khedmataktak.dto.wizard.WizardDtos.CvImportProgressEvent;
import com.khedmataktak.dto.wizard.WizardDtos.CvImportResultDto;
import com.khedmataktak.dto.wizard.WizardDtos.CvParserInfoDto;
import com.khedmataktak.dto.wizard.WizardDtos.EducationDto;
import com.khedmataktak.dto.wizard.WizardDtos.ExperienceDto;
import com.khedmataktak.dto.wizard.WizardDtos.LanguageDto;
import com.khedmataktak.dto.wizard.WizardDtos.PortfolioDto;
import com.khedmataktak.dto.wizard.WizardDtos.ProjectDto;
import com.khedmataktak.dto.wizard.WizardDtos.PublishStatusDto;
import com.khedmataktak.dto.wizard.WizardDtos.SkillDto;
import com.khedmataktak.dto.wizard.WizardDtos.UserProfileDto;
import com.khedmataktak.security.SecurityUtils;
import com.khedmataktak.service.CvImportService;
import com.khedmataktak.service.CvParserInfoService;
import com.khedmataktak.service.PortfolioWizardService;
import com.khedmataktak.service.ProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioWizardService portfolioWizardService;
    private final CvImportService cvImportService;
    private final CvParserInfoService cvParserInfoService;
    private final ProfileService profileService;
    private final ObjectMapper objectMapper;

    public PortfolioController(PortfolioWizardService portfolioWizardService,
                               CvImportService cvImportService,
                               CvParserInfoService cvParserInfoService,
                               ProfileService profileService,
                               ObjectMapper objectMapper) {
        this.portfolioWizardService = portfolioWizardService;
        this.cvImportService = cvImportService;
        this.cvParserInfoService = cvParserInfoService;
        this.profileService = profileService;
        this.objectMapper = objectMapper;
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

    @GetMapping("/cv/parser-info")
    public CvParserInfoDto getCvParserInfo() {
        return cvParserInfoService.getInfo();
    }

    @PostMapping(value = "/cv/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CvImportResultDto importCv(@RequestPart("file") MultipartFile file) {
        return cvImportService.importFromFile(file);
    }

    @PostMapping(value = "/cv/import-progress", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StreamingResponseBody> importCvWithProgress(@RequestPart("file") MultipartFile file) {
        StreamingResponseBody stream = outputStream -> {
            PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), false);
            writeProgressEvent(writer, new CvImportProgressEvent(
                    "extract", 0, "Démarrage de l'analyse...", null, null, null, null));
            try {
                cvImportService.importFromFileWithProgress(file, event -> writeProgressEvent(writer, event));
            } catch (IllegalArgumentException ex) {
                writeProgressEvent(writer, new CvImportProgressEvent(
                        "error", 0, ex.getMessage(), null, null, null, null));
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/x-ndjson"))
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header("X-Accel-Buffering", "no")
                .header("Connection", "keep-alive")
                .body(stream);
    }

    private void writeProgressEvent(PrintWriter writer, CvImportProgressEvent event) {
        try {
            writer.write(objectMapper.writeValueAsString(event));
            writer.write('\n');
            writer.flush();
        } catch (JsonProcessingException ex) {
            throw new UncheckedIOException(new IOException("Failed to serialize CV import progress", ex));
        }
    }

    @PostMapping(value = "/cv/import-and-save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PortfolioDto importCvAndSave(@RequestPart("file") MultipartFile file) {
        CvImportResultDto imported = cvImportService.importFromFile(file);
        return portfolioWizardService.importCvAndSave(SecurityUtils.currentUserId(), imported);
    }
}
