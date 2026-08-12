package com.khedmataktak.service;

import com.khedmataktak.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalFileStorageService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final StorageProperties storageProperties;
    private Path uploadRoot;

    public LocalFileStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @PostConstruct
    void init() throws IOException {
        uploadRoot = Path.of(storageProperties.getUploadDir()).toAbsolutePath().normalize();
        Files.createDirectories(uploadRoot);
    }

    public String storeProfilePhoto(UUID userId, MultipartFile file) {
        validateImage(file);
        String filename = "profile-" + userId + "-" + UUID.randomUUID() + extensionOf(file);
        Path target = uploadRoot.resolve("photos").resolve(filename);
        store(file, target);
        return "/uploads/photos/" + filename;
    }

    public String storeProjectImage(UUID userId, MultipartFile file) {
        validateImage(file);
        String filename = "project-" + userId + "-" + UUID.randomUUID() + extensionOf(file);
        Path target = uploadRoot.resolve("projects").resolve(filename);
        store(file, target);
        return "/uploads/projects/" + filename;
    }

    public Path resolveUploadPath(String relativeUrl) {
        if (relativeUrl == null || !relativeUrl.startsWith("/uploads/")) {
            throw new IllegalArgumentException("Invalid upload path");
        }
        String relative = relativeUrl.substring("/uploads/".length());
        Path resolved = uploadRoot.resolve(relative).normalize();
        if (!resolved.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Path traversal detected");
        }
        return resolved;
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File exceeds maximum size of 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported file type");
        }
    }

    private void store(MultipartFile file, Path target) {
        try {
            Files.createDirectories(target.getParent());
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file", e);
        }
    }

    private String extensionOf(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            return original.substring(original.lastIndexOf('.')).toLowerCase();
        }
        return switch (file.getContentType()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }
}
