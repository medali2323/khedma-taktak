package com.khedmataktak.service;

import java.util.UUID;

import com.khedmataktak.dto.ProjectRequest;
import com.khedmataktak.dto.ProjectResponse;
import com.khedmataktak.entity.Profile;
import com.khedmataktak.entity.Project;
import com.khedmataktak.exception.ResourceNotFoundException;
import com.khedmataktak.repository.ProjectRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProfileService profileService;
    private final LocalFileStorageService fileStorageService;

    public ProjectService(ProjectRepository projectRepository,
                          ProfileService profileService,
                          LocalFileStorageService fileStorageService) {
        this.projectRepository = projectRepository;
        this.profileService = profileService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> list(UUID userId) {
        Profile profile = profileService.findByUserId(userId);
        return projectRepository.findByProfileIdOrderBySortOrderAsc(profile.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(UUID userId, Long id) {
        return toResponse(findOwned(userId, id));
    }

    @Transactional
    public ProjectResponse create(UUID userId, ProjectRequest request) {
        Profile profile = profileService.findByUserId(userId);
        Project project = mapToEntity(new Project(), request);
        project.setProfile(profile);
        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse update(UUID userId, Long id, ProjectRequest request) {
        Project project = findOwned(userId, id);
        mapToEntity(project, request);
        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public void delete(UUID userId, Long id) {
        Project project = findOwned(userId, id);
        projectRepository.delete(project);
    }

    @Transactional
    public ProjectResponse uploadImage(UUID userId, Long id, MultipartFile file) {
        Project project = findOwned(userId, id);
        String url = fileStorageService.storeProjectImage(userId, file);
        if (project.getImageUrls() == null) {
            project.setImageUrls(new ArrayList<>());
        }
        project.getImageUrls().add(url);
        return toResponse(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public List<Project> findByProfileId(Long profileId) {
        return projectRepository.findByProfileIdOrderBySortOrderAsc(profileId);
    }

    private Project findOwned(UUID userId, Long id) {
        Profile profile = profileService.findByUserId(userId);
        return projectRepository.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private Project mapToEntity(Project project, ProjectRequest request) {
        project.setTitle(request.title());
        project.setDescription(request.description() != null ? request.description() : new HashMap<>());
        project.setUrl(request.url());
        project.setGithubUrl(request.githubUrl());
        project.setImageUrls(request.imageUrls() != null ? new ArrayList<>(request.imageUrls()) : new ArrayList<>());
        project.setTechnologies(request.technologies() != null ? request.technologies() : new HashMap<>());
        project.setHighlights(request.highlights() != null ? request.highlights() : new HashMap<>());
        project.setSortOrder(request.sortOrder());
        return project;
    }

    ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getUrl(),
                project.getGithubUrl(),
                project.getImageUrls(),
                project.getTechnologies(),
                project.getHighlights(),
                project.getSortOrder(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
