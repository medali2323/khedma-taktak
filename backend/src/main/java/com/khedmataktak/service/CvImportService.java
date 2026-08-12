package com.khedmataktak.service;

import com.khedmataktak.dto.wizard.WizardDtos.CvImportProgressEvent;
import com.khedmataktak.dto.wizard.WizardDtos.CvImportResultDto;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CvImportService {

    private static final Logger log = LoggerFactory.getLogger(CvImportService.class);

    private static final Set<String> PDF_TYPES = Set.of("application/pdf");
    private static final Set<String> DOCX_TYPES = Set.of(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final CvParserService cvParserService;
    private final OllamaCvParserService ollamaCvParserService;
    private final CvImportMergeService cvImportMergeService;

    public CvImportService(CvParserService cvParserService,
                           OllamaCvParserService ollamaCvParserService,
                           CvImportMergeService cvImportMergeService) {
        this.cvParserService = cvParserService;
        this.ollamaCvParserService = ollamaCvParserService;
        this.cvImportMergeService = cvImportMergeService;
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
        FileKind kind = detectKind(file);
        try {
            emit(progress, "extract", 5,
                    kind == FileKind.PDF ? "Lecture du PDF..." : "Lecture du document Word...",
                    null, null, null, null);
            String text = extractText(file.getBytes(), kind);
            emit(progress, "extract", 15, "Texte extrait du document", null, null, null, null);
            if (text.isBlank()) {
                throw new IllegalArgumentException(
                        "No readable text found. Try a text-based CV instead of a scanned image.");
            }
            return parseExtractedText(text, progress);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read uploaded CV file", e);
        }
    }

    private CvImportResultDto parseExtractedText(String text, Consumer<CvImportProgressEvent> progress) {
        emit(progress, "parse", 20, "Analyse structurelle...", null, null, null, null);
        CvImportResultDto heuristic = cvParserService.parse(text);

        Optional<CvImportResultDto> ollamaResult = parseWithOllama(text, progress);
        if (ollamaResult.isPresent()) {
            CvImportResultDto merged = cvImportMergeService.merge(ollamaResult.get(), heuristic, text);
            emitMergedProgress(progress, merged);
            emit(progress, "complete", 100, "Analyse terminée (IA + structure)", null, null, null, merged);
            return merged;
        }
        String ollamaNote = ollamaCvParserService.getLastFailureReason();
        emitHeuristicProgress(progress, heuristic);
        emit(progress, "complete", 100, "Analyse terminée (structure)", null, null, null, heuristic);
        if (ollamaNote != null && !ollamaNote.isBlank()) {
            return withParserNote(heuristic, ollamaNote);
        }
        return heuristic;
    }

    private Optional<CvImportResultDto> parseWithOllama(String text, Consumer<CvImportProgressEvent> progress) {
        if (!ollamaCvParserService.isEnabled()) {
            return Optional.empty();
        }
        emit(progress, "parse", 30, "Analyse IA (Ollama)...", null, null, null, null);
        if (!ollamaCvParserService.isReachable()) {
            log.info("Ollama unavailable, using heuristic parser");
            emit(progress, "parse", 35, "Ollama indisponible, analyse classique...", null, null, null, null);
            return Optional.empty();
        }
        if (!ollamaCvParserService.isModelAvailable()) {
            String reason = ollamaCvParserService.getLastFailureReason();
            log.info("Ollama model missing, using heuristic parser");
            emit(progress, "parse", 35, reason != null ? reason : "Modèle Ollama absent, analyse classique...", null, null, null, null);
            return Optional.empty();
        }
        Optional<CvImportResultDto> result = ollamaCvParserService.parse(text);
        if (result.isPresent()) {
            emitOllamaProgress(progress, result.get());
        }
        return result;
    }

    private void emitMergedProgress(Consumer<CvImportProgressEvent> progress, CvImportResultDto result) {
        emit(progress, "parse", 95, "Finalisation (listes stables sans IA)...", null, null, null, null);
        if (!result.skills().isEmpty()) {
            emit(progress, "parse", 97, "Compétences (extraction stable)", "skills", true, result.skills().size(), null);
        }
        if (!result.languages().isEmpty()) {
            emit(progress, "parse", 98, "Langues (extraction stable)", "languages", true, result.languages().size(), null);
        }
    }

    private void emitHeuristicProgress(Consumer<CvImportProgressEvent> progress, CvImportResultDto result) {
        emit(progress, "parse", 70, "Analyse structurelle terminée", null, null, null, null);
        if (hasProfile(result)) {
            emit(progress, "parse", 75, "Profil détecté", "profile", true, null, null);
        }
        if (!result.experiences().isEmpty()) {
            emit(progress, "parse", 80, "Expériences détectées", "experiences", true, result.experiences().size(), null);
        }
        if (!result.skills().isEmpty()) {
            emit(progress, "parse", 88, "Compétences détectées", "skills", true, result.skills().size(), null);
        }
        if (!result.languages().isEmpty()) {
            emit(progress, "parse", 90, "Langues détectées", "languages", true, result.languages().size(), null);
        }
        if (!result.education().isEmpty()) {
            emit(progress, "parse", 92, "Formation détectée", "education", true, result.education().size(), null);
        }
    }

    private void emitOllamaProgress(Consumer<CvImportProgressEvent> progress, CvImportResultDto result) {
        emit(progress, "parse", 70, "Profil analysé", "profile",
                hasProfile(result), null, null);
        if (!result.experiences().isEmpty()) {
            emit(progress, "parse", 80, "Expériences analysées", "experiences", true, result.experiences().size(), null);
        }
        if (!result.skills().isEmpty()) {
            emit(progress, "parse", 88, "Compétences analysées", "skills", true, result.skills().size(), null);
        }
        if (!result.education().isEmpty()) {
            emit(progress, "parse", 92, "Formation analysée", "education", true, result.education().size(), null);
        }
    }

    private boolean hasProfile(CvImportResultDto result) {
        if (result.profile() == null) {
            return false;
        }
        var p = result.profile();
        return !p.firstName().isBlank() || !p.lastName().isBlank() || !p.email().isBlank()
                || !p.linkedin().isBlank() || !p.github().isBlank();
    }

    private CvImportResultDto withParserNote(CvImportResultDto result, String note) {
        return new CvImportResultDto(
                result.profile(),
                result.experiences(),
                result.projects(),
                result.education(),
                result.skills(),
                result.languages(),
                result.certifications(),
                result.parserEngine(),
                note
        );
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

    private String extractText(byte[] bytes, FileKind kind) throws IOException {
        if (kind == FileKind.DOCX) {
            return extractDocxText(bytes);
        }
        return extractPdfText(bytes);
    }

    private String extractPdfText(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setLineSeparator("\n");
            stripper.setWordSeparator(" ");
            stripper.setSuppressDuplicateOverlappingText(true);
            stripper.setAddMoreFormatting(true);
            return stripper.getText(document);
        }
    }

    private String extractDocxText(byte[] docxBytes) throws IOException {
        try (InputStream input = new java.io.ByteArrayInputStream(docxBytes);
             XWPFDocument document = new XWPFDocument(input);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("CV file is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File exceeds maximum size of 10MB");
        }
        detectKind(file);
    }

    private FileKind detectKind(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        boolean pdf = (contentType != null && PDF_TYPES.contains(contentType)) || filename.endsWith(".pdf");
        boolean docx = (contentType != null && DOCX_TYPES.contains(contentType))
                || filename.endsWith(".docx");
        if (pdf) {
            return FileKind.PDF;
        }
        if (docx) {
            return FileKind.DOCX;
        }
        throw new IllegalArgumentException("Only PDF and Word (.docx) files are supported");
    }

    private enum FileKind {
        PDF, DOCX
    }
}
