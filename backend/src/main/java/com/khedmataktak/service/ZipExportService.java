package com.khedmataktak.service;

import com.khedmataktak.dto.PortfolioViewModel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.stereotype.Service;

@Service
public class ZipExportService {

    private final PortfolioRenderService portfolioRenderService;
    private final LocalFileStorageService fileStorageService;

    public ZipExportService(PortfolioRenderService portfolioRenderService,
                            LocalFileStorageService fileStorageService) {
        this.portfolioRenderService = portfolioRenderService;
        this.fileStorageService = fileStorageService;
    }

    public byte[] exportZip(PortfolioViewModel model) {
        String html = portfolioRenderService.renderPortfolio(model);
        String css = defaultCss();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            addEntry(zos, "index.html", html.getBytes(StandardCharsets.UTF_8));
            addEntry(zos, "styles.css", css.getBytes(StandardCharsets.UTF_8));

            if (model.photoUrl() != null) {
                addImageIfExists(zos, "images/profile" + extensionOf(model.photoUrl()), model.photoUrl());
            }

            for (PortfolioViewModel.ProjectView project : model.projects()) {
                if (project.imageUrls() == null) {
                    continue;
                }
                int index = 0;
                for (String imageUrl : project.imageUrls()) {
                    addImageIfExists(zos, "images/project-" + index + extensionOf(imageUrl), imageUrl);
                    index++;
                }
            }

            zos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create export archive", e);
        }
    }

    private void addImageIfExists(ZipOutputStream zos, String entryName, String uploadUrl) throws IOException {
        try {
            Path path = fileStorageService.resolveUploadPath(uploadUrl);
            if (Files.exists(path)) {
                addEntry(zos, entryName, Files.readAllBytes(path));
            }
        } catch (IllegalArgumentException ignored) {
            // Skip invalid paths silently
        }
    }

    private void addEntry(ZipOutputStream zos, String name, byte[] content) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        zos.write(content);
        zos.closeEntry();
    }

    private String extensionOf(String url) {
        int dot = url.lastIndexOf('.');
        return dot >= 0 ? url.substring(dot) : ".jpg";
    }

    private String defaultCss() {
        return """
                body { font-family: system-ui, sans-serif; margin: 0; padding: 2rem; color: #1a1a1a; }
                header { margin-bottom: 2rem; }
                h1 { margin: 0 0 0.25rem; }
                h2 { border-bottom: 1px solid #ddd; padding-bottom: 0.25rem; margin-top: 2rem; }
                .section { margin-bottom: 1.5rem; }
                ul { padding-left: 1.25rem; }
                """;
    }
}
