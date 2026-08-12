package com.khedmataktak.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.khedmataktak.dto.wizard.WizardDtos.CvImportResultDto;
import com.khedmataktak.dto.wizard.WizardDtos.LanguageDto;
import com.khedmataktak.dto.wizard.WizardDtos.SkillDto;
import com.khedmataktak.dto.wizard.WizardDtos;
import com.khedmataktak.dto.wizard.WizardDtos.UserProfileDto;
import java.util.List;
import org.junit.jupiter.api.Test;

class CvImportMergeServiceTest {

    private final CvImportMergeService mergeService = new CvImportMergeService();

    @Test
    void mergeUsesHeuristicSkillsAndLanguagesNotOllama() {
        String cvText = "Java, Spring Boot, Anglais courant, Français natif";
        CvImportResultDto ollama = emptyResult(
                List.of(new SkillDto(null, "RandomSkill", "Expert", "")),
                List.of(new LanguageDto(null, "Klingon", "Fluent"))
        );
        CvImportResultDto heuristic = emptyResult(
                List.of(new SkillDto(null, "Java", "Advanced", "")),
                List.of(new LanguageDto(null, "Français", "Native"))
        );

        CvImportResultDto merged = mergeService.merge(ollama, heuristic, cvText);

        assertEquals(1, merged.skills().size());
        assertEquals("Java", merged.skills().getFirst().name());
        assertEquals(1, merged.languages().size());
        assertEquals("Français", merged.languages().getFirst().name());
        assertTrue(merged.parserNote().contains("sans IA"));
    }

    private CvImportResultDto emptyResult(List<SkillDto> skills, List<LanguageDto> languages) {
        UserProfileDto profile = WizardDtos.emptyProfile();
        return new CvImportResultDto(profile, List.of(), List.of(), List.of(), skills, languages, List.of(), "ollama", null);
    }
}
