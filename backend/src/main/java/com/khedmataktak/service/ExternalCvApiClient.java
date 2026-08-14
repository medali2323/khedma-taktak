package com.khedmataktak.service;

import com.khedmataktak.config.CvParserProperties;
import com.khedmataktak.dto.wizard.WizardDtos.CvImportResultDto;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * Thin HTTP client: forwards the CV file to the external extraction API and returns its JSON as-is.
 * This backend does not parse or correct the payload.
 */
@Service
public class ExternalCvApiClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalCvApiClient.class);

    private final CvParserProperties properties;
    private final RestTemplate restTemplate;
    private String lastFailureReason;

    public ExternalCvApiClient(CvParserProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(Math.max(30, properties.getTimeoutSeconds())));
        this.restTemplate = new RestTemplate(factory);
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public String configuredBaseUrl() {
        return properties.getBaseUrl();
    }

    public String getLastFailureReason() {
        return lastFailureReason;
    }

    public boolean isReachable() {
        if (!isEnabled()) {
            lastFailureReason = "API CV désactivée (app.cv-parser.enabled=false).";
            return false;
        }
        try {
            restTemplate.getForEntity(properties.healthUrl(), String.class);
            lastFailureReason = null;
            return true;
        } catch (RestClientException ex) {
            lastFailureReason = "API CV injoignable (" + properties.getBaseUrl() + ") : " + ex.getMessage();
            log.debug("CV API health check failed: {}", ex.getMessage());
            return false;
        }
    }

    public CvImportResultDto importCv(MultipartFile file) {
        if (!isEnabled()) {
            throw new IllegalStateException("API CV désactivée. Configurez app.cv-parser.enabled=true.");
        }
        lastFailureReason = null;
        try {
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(
                    buildMultipart(file),
                    multipartHeaders());

            ResponseEntity<CvImportResultDto> response = restTemplate.postForEntity(
                    properties.importUrl(),
                    request,
                    CvImportResultDto.class);

            CvImportResultDto result = response.getBody();
            if (result == null) {
                lastFailureReason = "API CV a renvoyé une réponse vide.";
                throw new IllegalStateException(lastFailureReason);
            }
            log.info("CV import delegated to external API ({})", properties.importUrl());
            return result;
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            String message = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            if (message.toLowerCase().contains("timed out") || message.toLowerCase().contains("timeout")) {
                lastFailureReason = "API CV trop lente (timeout "
                        + properties.getTimeoutSeconds()
                        + "s). Vérifiez les logs Python / Ollama, ou augmentez CV_API_TIMEOUT_SECONDS.";
            } else {
                lastFailureReason = "Erreur API CV : " + message;
            }
            throw new IllegalArgumentException(lastFailureReason, ex);
        }
    }

    private HttpHeaders multipartHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }

    private MultiValueMap<String, Object> buildMultipart(MultipartFile file) throws java.io.IOException {
        byte[] bytes = file.getBytes();
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "cv.pdf";

        ByteArrayResource resource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        return body;
    }
}
