package com.khedmataktak.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khedmataktak.config.CvParserProperties;
import com.khedmataktak.dto.wizard.WizardDtos;
import com.khedmataktak.dto.wizard.WizardDtos.CertificationDto;
import com.khedmataktak.dto.wizard.WizardDtos.CvImportResultDto;
import com.khedmataktak.dto.wizard.WizardDtos.EducationDto;
import com.khedmataktak.dto.wizard.WizardDtos.ExperienceDto;
import com.khedmataktak.dto.wizard.WizardDtos.LanguageDto;
import com.khedmataktak.dto.wizard.WizardDtos.ProjectDto;
import com.khedmataktak.dto.wizard.WizardDtos.SkillDto;
import com.khedmataktak.dto.wizard.WizardDtos.UserProfileDto;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class OllamaCvParserService {

    private static final Logger log = LoggerFactory.getLogger(OllamaCvParserService.class);

    private static final String SYSTEM_PROMPT = """
            You extract structured data from CV/resume text.
            Return ONLY valid JSON matching this schema (no markdown, no explanation):
            {
              "profile": {
                "firstName": "string", "lastName": "string", "email": "string", "title": "string",
                "summary": "string", "phone": "string", "location": "string", "website": "string",
                "linkedin": "string", "github": "string"
              },
              "experiences": [{
                "company": "string", "position": "string", "location": "string",
                "startDate": "YYYY-MM-DD or empty", "endDate": "YYYY-MM-DD or empty",
                "current": false, "description": "string"
              }],
              "projects": [{
                "name": "string", "description": "string", "url": "string",
                "technologies": "string", "startDate": "string", "endDate": "string"
              }],
              "education": [{
                "institution": "string", "degree": "string", "field": "string",
                "startDate": "string", "endDate": "string", "description": "string"
              }],
              "skills": [{ "name": "string", "level": "Beginner|Intermediate|Advanced|Expert", "category": "string" }],
              "languages": [{ "name": "string", "proficiency": "string" }],
              "certifications": [{ "name": "string", "issuer": "string", "date": "YYYY-MM-DD", "url": "string" }]
            }
            Rules:
            - Use empty strings for unknown fields. Dates as YYYY-MM-DD when possible.
            - Extract EVERY skill listed (comma lists, bullet lists, skills section, technologies).
            - Extract EVERY language with its proficiency level (native, fluent, B2, etc.).
            - Recognize vocational training (CAP, BEP, Bac Pro, BP, CQP, titre professionnel) and trades (plomberie, menuiserie, maçonnerie, électricité, peinture, chauffage).
            - Recognize regulatory certifications (CACES, habilitation électrique, SST, permis).
            - Do not invent data not present in the CV text.
            - Be exhaustive and consistent: same input must yield the same output.
            """;

    private final CvParserProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private String lastFailureReason;

    public OllamaCvParserService(CvParserProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(Math.max(30, properties.getOllama().getTimeoutSeconds())));
        this.restClient = RestClient.builder()
                .baseUrl(trimTrailingSlash(properties.getOllama().getBaseUrl()))
                .requestFactory(factory)
                .build();
    }

    public boolean isEnabled() {
        return properties.getOllama().isEnabled();
    }

    public String getLastFailureReason() {
        return lastFailureReason;
    }

    public boolean isModelAvailable() {
        if (!isEnabled() || !isReachable()) {
            return false;
        }
        try {
            String response = restClient.get().uri("/api/tags").retrieve().body(String.class);
            if (response == null) {
                return false;
            }
            JsonNode models = objectMapper.readTree(response).path("models");
            String wanted = properties.getOllama().getModel();
            for (JsonNode model : models) {
                String name = model.path("name").asText("");
                if (name.equals(wanted) || name.startsWith(wanted + ":")) {
                    return true;
                }
            }
            lastFailureReason = "Modèle Ollama « " + wanted + " » non installé. Exécutez : .\\scripts\\setup-ollama.ps1 (ou .\\scripts\\setup-portfolio-cv-model.ps1)";
            return false;
        } catch (Exception ex) {
            lastFailureReason = "Impossible de vérifier les modèles Ollama : " + ex.getMessage();
            return false;
        }
    }

    public boolean isReachable() {
        if (!isEnabled()) {
            return false;
        }
        try {
            restClient.get().uri("/api/tags").retrieve().toBodilessEntity();
            return true;
        } catch (RestClientException ex) {
            log.debug("Ollama not reachable at {}: {}", properties.getOllama().getBaseUrl(), ex.getMessage());
            return false;
        }
    }

    public Optional<CvImportResultDto> parse(String cvText) {
        if (!isEnabled()) {
            return Optional.empty();
        }
        lastFailureReason = null;
        if (!isModelAvailable()) {
            log.warn("Ollama model not available: {}", lastFailureReason);
            return Optional.empty();
        }
        String truncated = truncate(cvText, properties.getOllama().getMaxTextLength());
        try {
            String jsonContent = callOllama(truncated);
            CvImportResultDto result = mapToResult(jsonContent);
            if (hasUsefulData(result)) {
                log.info("CV parsed successfully with Ollama model {}", properties.getOllama().getModel());
                return Optional.of(result);
            }
            lastFailureReason = "Ollama n'a pas extrait de données exploitables du CV.";
            log.warn("Ollama returned JSON but no useful CV data detected");
            return Optional.empty();
        } catch (Exception ex) {
            lastFailureReason = describeFailure(ex);
            log.warn("Ollama CV parsing failed: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private String describeFailure(Exception ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        if (message.contains("not found")) {
            return "Modèle Ollama « " + properties.getOllama().getModel()
                    + " » absent. Exécutez : .\\scripts\\setup-ollama.ps1 (ou .\\scripts\\setup-portfolio-cv-model.ps1)";
        }
        return "Erreur Ollama : " + message;
    }

    private String callOllama(String cvText) throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("temperature", properties.getOllama().getTemperature());
        options.put("top_p", 0.1);
        options.put("seed", properties.getOllama().getSeed());

        Map<String, Object> body = new HashMap<>();
        body.put("model", properties.getOllama().getModel());
        body.put("stream", false);
        body.put("format", "json");
        body.put("options", options);
        body.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", "Extract CV data from this text:\n\n" + cvText)
        ));

        String response = restClient.post()
                .uri("/api/chat")
                .body(body)
                .retrieve()
                .body(String.class);

        if (response == null || response.isBlank()) {
            throw new IllegalStateException("Empty response from Ollama");
        }

        JsonNode root = objectMapper.readTree(response);
        JsonNode content = root.path("message").path("content");
        if (content.isMissingNode() || content.asText().isBlank()) {
            throw new IllegalStateException("No message content in Ollama response");
        }
        return extractJson(content.asText());
    }

    private CvImportResultDto mapToResult(String jsonContent) throws Exception {
        JsonNode root = objectMapper.readTree(extractJson(jsonContent));
        return new CvImportResultDto(
                mapProfile(root.path("profile")),
                mapExperiences(root.path("experiences")),
                mapProjects(root.path("projects")),
                mapEducation(root.path("education")),
                mapSkills(root.path("skills")),
                mapLanguages(root.path("languages")),
                mapCertifications(root.path("certifications")),
                "ollama",
                null
        );
    }

    private UserProfileDto mapProfile(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return emptyProfile();
        }
        return WizardDtos.parsedProfile(
                text(node, "firstName"),
                text(node, "lastName"),
                text(node, "email"),
                text(node, "title"),
                text(node, "summary"),
                text(node, "phone"),
                text(node, "location"),
                text(node, "website"),
                text(node, "linkedin"),
                text(node, "github")
        );
    }

    private List<ExperienceDto> mapExperiences(JsonNode node) {
        List<ExperienceDto> list = new ArrayList<>();
        if (!node.isArray()) {
            return list;
        }
        for (JsonNode item : node) {
            String company = text(item, "company");
            String position = text(item, "position");
            if (company.isBlank() && position.isBlank()) {
                continue;
            }
            list.add(new ExperienceDto(
                    null,
                    company,
                    position,
                    text(item, "location"),
                    text(item, "startDate"),
                    text(item, "endDate"),
                    item.path("current").asBoolean(false),
                    text(item, "description")
            ));
        }
        return list;
    }

    private List<ProjectDto> mapProjects(JsonNode node) {
        List<ProjectDto> list = new ArrayList<>();
        if (!node.isArray()) {
            return list;
        }
        for (JsonNode item : node) {
            if (text(item, "name").isBlank()) {
                continue;
            }
            list.add(new ProjectDto(
                    null,
                    text(item, "name"),
                    text(item, "description"),
                    text(item, "url"),
                    text(item, "technologies"),
                    text(item, "startDate"),
                    text(item, "endDate")
            ));
        }
        return list;
    }

    private List<EducationDto> mapEducation(JsonNode node) {
        List<EducationDto> list = new ArrayList<>();
        if (!node.isArray()) {
            return list;
        }
        for (JsonNode item : node) {
            if (text(item, "institution").isBlank() && text(item, "degree").isBlank()) {
                continue;
            }
            list.add(new EducationDto(
                    null,
                    text(item, "institution"),
                    text(item, "degree"),
                    text(item, "field"),
                    text(item, "startDate"),
                    text(item, "endDate"),
                    text(item, "description"),
                    inferEducationType(text(item, "degree"))
            ));
        }
        return list;
    }

    private List<SkillDto> mapSkills(JsonNode node) {
        List<SkillDto> list = new ArrayList<>();
        if (!node.isArray()) {
            return list;
        }
        for (JsonNode item : node) {
            String name = text(item, "name");
            if (name.isBlank()) {
                continue;
            }
            String level = text(item, "level");
            if (level.isBlank()) {
                level = "Intermediate";
            }
            list.add(new SkillDto(null, name, level, text(item, "category")));
        }
        return list;
    }

    private List<LanguageDto> mapLanguages(JsonNode node) {
        List<LanguageDto> list = new ArrayList<>();
        if (!node.isArray()) {
            return list;
        }
        for (JsonNode item : node) {
            String name = text(item, "name");
            if (name.isBlank()) {
                continue;
            }
            String proficiency = text(item, "proficiency");
            if (proficiency.isBlank()) {
                proficiency = "Conversational";
            }
            list.add(new LanguageDto(null, name, proficiency));
        }
        return list;
    }

    private List<CertificationDto> mapCertifications(JsonNode node) {
        List<CertificationDto> list = new ArrayList<>();
        if (!node.isArray()) {
            return list;
        }
        for (JsonNode item : node) {
            if (text(item, "name").isBlank()) {
                continue;
            }
            list.add(new CertificationDto(
                    null,
                    text(item, "name"),
                    text(item, "issuer"),
                    text(item, "date"),
                    text(item, "url"),
                    inferCertificationType(text(item, "name"))
            ));
        }
        return list;
    }

    private UserProfileDto emptyProfile() {
        return WizardDtos.emptyProfile();
    }

    private String inferEducationType(String degree) {
        String lower = degree.toLowerCase();
        if (lower.contains("cap") || lower.contains("bep") || lower.contains("bac pro")
                || lower.contains("bp ") || lower.contains("cqp") || lower.contains("titre pro")) {
            return "VOCATIONAL";
        }
        return "ACADEMIC";
    }

    private String inferCertificationType(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("caces") || lower.contains("habilitation") || lower.contains("sst")
                || lower.contains("electrique") || lower.contains("amiante")) {
            return "REGULATORY";
        }
        return "PROFESSIONAL";
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isNull() || value.isMissingNode() ? "" : value.asText("").trim();
    }

    private String extractJson(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("{")) {
            return trimmed;
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    private String trimTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private boolean hasUsefulData(CvImportResultDto result) {
        if (result == null) {
            return false;
        }
        UserProfileDto profile = result.profile();
        boolean hasProfile = profile != null && (
                !profile.email().isBlank() || !profile.firstName().isBlank() || !profile.lastName().isBlank()
                        || !profile.linkedin().isBlank() || !profile.github().isBlank()
        );
        return hasProfile
                || !result.experiences().isEmpty()
                || !result.education().isEmpty()
                || !result.skills().isEmpty();
    }
}
