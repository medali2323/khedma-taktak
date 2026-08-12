package com.khedmataktak.dto.ats;

import com.khedmataktak.dto.wizard.WizardDtos.UserProfileDto;
import java.util.List;

public final class AtsDtos {

    private AtsDtos() {
    }

    public record AtsAnalyzeRequest(
            String mode,
            String targetDomain
    ) {
    }

    public record AtsAnalysisResult(
            int score,
            String mode,
            String targetDomain,
            List<String> strengths,
            List<String> weaknesses,
            List<String> recommendations,
            List<String> suggestedKeywords
    ) {
    }

    public record AtsOptimizeResult(
            AtsAnalysisResult analysis,
            UserProfileDto optimizedProfile
    ) {
    }
}
