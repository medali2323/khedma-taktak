package com.khedmataktak.service;

import com.khedmataktak.dto.wizard.WizardDtos.CvImportProgressEvent;
import com.khedmataktak.dto.wizard.WizardDtos.CvImportResultDto;
import java.util.Set;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Gateway only: validates the upload then forwards it to the external CV API.
 * Extraction and understanding live outside this Spring app (Python service).
 */
@Service
public class CvImportService {

    private static final Set<String> PDF_TYPES = Set.of("application/pdf");
    private static final Set<String> DOCX_TYPES = Set.of(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final ExternalCvApiClient externalCvApiClient;

    public CvImportService(ExternalCvApiClient externalCvApiClient) {
        this.externalCvApiClient = externalCvApiClient;
    }

    public CvImportResultDto importFromPdf(MultipartFile file) {
        return importFromFile(file);
    }

    public CvImportResultDto importFromFile(MultipartFile file) {
        return importFromFileWithProgress(file, null);
    }

    public CvImportResultDto importFromPdfWithProgress(MultipartFile file, Consumer<CvImportProgressEvent> progress) {
        return importFromFileWithProgress(file, progress);
    }

    public CvImportResultDto importFromFileWithProgress(MultipartFile file, Consumer<CvImportProgressEvent> progress) {
        validateFile(file);
        emit(progress, "extract", 10, "Envoi du CV vers le service d'extraction...", null, null, null, null);
        emit(progress, "parse", 40, "Traitement par l'API CV...", null, null, null, null);

        CvImportResultDto result = externalCvApiClient.importCv(file);

        emit(progress, "parse", 90, "Réponse reçue du service CV", null, null, null, null);
        emit(progress, "complete", 100, "Analyse terminée (API CV externe)", null, null, null, result);
        return result;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("CV file is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File exceeds maximum size of 10MB");
        }
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        boolean pdf = (contentType != null && PDF_TYPES.contains(contentType)) || filename.endsWith(".pdf");
        boolean docx = (contentType != null && DOCX_TYPES.contains(contentType)) || filename.endsWith(".docx");
        if (!pdf && !docx) {
            throw new IllegalArgumentException("Only PDF and Word (.docx) files are supported");
        }
    }

    private void emit(Consumer<CvImportProgressEvent> progress,
                      String phase,
                      int pct,
                      String message,
                      String section,
                      Boolean found,
                      Integer count,
                      CvImportResultDto result) {
        if (progress != null) {
            progress.accept(new CvImportProgressEvent(phase, pct, message, section, found, count, result));
        }
    }
}
