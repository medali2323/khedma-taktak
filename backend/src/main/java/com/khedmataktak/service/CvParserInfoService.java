package com.khedmataktak.service;

import com.khedmataktak.config.CvParserProperties;
import com.khedmataktak.dto.wizard.WizardDtos.CvParserInfoDto;
import org.springframework.stereotype.Service;

@Service
public class CvParserInfoService {

    private final CvParserProperties properties;
    private final ExternalCvApiClient externalCvApiClient;

    public CvParserInfoService(CvParserProperties properties, ExternalCvApiClient externalCvApiClient) {
        this.properties = properties;
        this.externalCvApiClient = externalCvApiClient;
    }

    public CvParserInfoDto getInfo() {
        boolean enabled = externalCvApiClient.isEnabled();
        boolean reachable = enabled && externalCvApiClient.isReachable();
        return new CvParserInfoDto(
                enabled,
                reachable,
                reachable,
                properties.getBaseUrl(),
                "external-api",
                "cv-api",
                externalCvApiClient.getLastFailureReason()
        );
    }
}
