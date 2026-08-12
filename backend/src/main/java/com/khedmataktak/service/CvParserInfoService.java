package com.khedmataktak.service;

import com.khedmataktak.config.CvParserProperties;
import com.khedmataktak.dto.wizard.WizardDtos.CvParserInfoDto;
import org.springframework.stereotype.Service;

@Service
public class CvParserInfoService {

    private final CvParserProperties properties;
    private final OllamaCvParserService ollamaCvParserService;

    public CvParserInfoService(CvParserProperties properties, OllamaCvParserService ollamaCvParserService) {
        this.properties = properties;
        this.ollamaCvParserService = ollamaCvParserService;
    }

    public CvParserInfoDto getInfo() {
        boolean enabled = ollamaCvParserService.isEnabled();
        boolean reachable = enabled && ollamaCvParserService.isReachable();
        boolean modelAvailable = reachable && ollamaCvParserService.isModelAvailable();
        return new CvParserInfoDto(
                enabled,
                reachable,
                modelAvailable,
                properties.getOllama().getModel(),
                "hybrid",
                "heuristic-stable",
                ollamaCvParserService.getLastFailureReason()
        );
    }
}
