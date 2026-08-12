package com.khedmataktak.service;

import com.khedmataktak.dto.wizard.WizardDtos.CertificationDto;
import com.khedmataktak.dto.wizard.WizardDtos;
import com.khedmataktak.dto.wizard.WizardDtos.CvImportResultDto;
import com.khedmataktak.dto.wizard.WizardDtos.EducationDto;
import com.khedmataktak.dto.wizard.WizardDtos.ExperienceDto;
import com.khedmataktak.dto.wizard.WizardDtos.LanguageDto;
import com.khedmataktak.dto.wizard.WizardDtos.ProjectDto;
import com.khedmataktak.dto.wizard.WizardDtos.SkillDto;
import com.khedmataktak.dto.wizard.WizardDtos.UserProfileDto;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Service;

@Service
public class CvImportMergeService {

    private static final String STABLE_LISTS_NOTE =
            "Compétences et langues extraites sans IA (résultat identique à chaque import).";

    /**
     * Ollama is non-deterministic on list fields — skills/languages/certifications always come from
     * the heuristic parser. Ollama only enriches profile and narrative sections when validated.
     */
    public CvImportResultDto merge(CvImportResultDto ollama, CvImportResultDto heuristic, String cvText) {
        String haystack = cvText != null ? cvText.toLowerCase(Locale.ROOT) : "";
        return new CvImportResultDto(
                mergeProfile(ollama.profile(), heuristic.profile()),
                mergeExperiencesValidated(ollama.experiences(), heuristic.experiences(), haystack),
                mergeProjectsValidated(ollama.projects(), heuristic.projects(), haystack),
                mergeEducationValidated(ollama.education(), heuristic.education(), haystack),
                safeList(heuristic.skills()),
                safeList(heuristic.languages()),
                safeList(heuristic.certifications()),
                "hybrid",
                STABLE_LISTS_NOTE
        );
    }

    private UserProfileDto mergeProfile(UserProfileDto ollama, UserProfileDto heuristic) {
        if (heuristic == null) {
            return ollama;
        }
        if (ollama == null) {
            return heuristic;
        }
        return WizardDtos.parsedProfile(
                pick(heuristic.firstName(), ollama.firstName()),
                pick(heuristic.lastName(), ollama.lastName()),
                pick(heuristic.email(), ollama.email()),
                pickNonEmpty(heuristic.title(), ollama.title()),
                pickLonger(heuristic.summary(), ollama.summary()),
                pick(heuristic.phone(), ollama.phone()),
                pickNonEmpty(heuristic.location(), ollama.location()),
                pick(heuristic.website(), ollama.website()),
                pick(heuristic.linkedin(), ollama.linkedin()),
                pick(heuristic.github(), ollama.github())
        );
    }

    private List<ExperienceDto> mergeExperiencesValidated(
            List<ExperienceDto> ollama, List<ExperienceDto> heuristic, String haystack) {
        return mergeListValidated(heuristic, ollama, haystack,
                exp -> normalizeKey(exp.company(), exp.position()),
                exp -> containsInText(haystack, exp.company(), exp.position()));
    }

    private List<ProjectDto> mergeProjectsValidated(
            List<ProjectDto> ollama, List<ProjectDto> heuristic, String haystack) {
        return mergeListValidated(heuristic, ollama, haystack,
                proj -> normalizeKey(proj.name()),
                proj -> containsInText(haystack, proj.name()));
    }

    private List<EducationDto> mergeEducationValidated(
            List<EducationDto> ollama, List<EducationDto> heuristic, String haystack) {
        return mergeListValidated(heuristic, ollama, haystack,
                edu -> normalizeKey(edu.institution(), edu.degree()),
                edu -> containsInText(haystack, edu.institution(), edu.degree()));
    }

    private <T> List<T> mergeListValidated(
            List<T> base,
            List<T> ollamaExtra,
            String haystack,
            Function<T, String> keyFn,
            Function<T, Boolean> validInText) {
        Map<String, T> merged = new LinkedHashMap<>();
        if (base != null) {
            for (T item : base) {
                merged.put(keyFn.apply(item), item);
            }
        }
        if (ollamaExtra != null) {
            for (T item : ollamaExtra) {
                if (Boolean.TRUE.equals(validInText.apply(item))) {
                    merged.putIfAbsent(keyFn.apply(item), item);
                }
            }
        }
        return new ArrayList<>(merged.values());
    }

    private boolean containsInText(String haystack, String... parts) {
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            String needle = part.trim().toLowerCase(Locale.ROOT);
            if (needle.length() >= 3 && haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private <T> List<T> safeList(List<T> list) {
        return list != null ? list : List.of();
    }

    private String pick(String preferred, String alternate) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }
        return alternate != null ? alternate.trim() : "";
    }

    private String pickNonEmpty(String preferred, String alternate) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }
        if (alternate != null && !alternate.isBlank()) {
            return alternate.trim();
        }
        return "";
    }

    private String pickLonger(String a, String b) {
        String left = a != null ? a.trim() : "";
        String right = b != null ? b.trim() : "";
        return left.length() >= right.length() ? left : right;
    }

    private String normalizeKey(String... parts) {
        StringBuilder key = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            if (key.length() > 0) {
                key.append('|');
            }
            key.append(part.trim().toLowerCase(Locale.ROOT));
        }
        return key.toString();
    }
}
