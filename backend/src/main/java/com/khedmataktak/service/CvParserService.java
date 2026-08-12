package com.khedmataktak.service;

import com.khedmataktak.dto.wizard.WizardDtos;
import com.khedmataktak.dto.wizard.WizardDtos.CertificationDto;
import com.khedmataktak.dto.wizard.WizardDtos.CvImportProgressEvent;
import com.khedmataktak.dto.wizard.WizardDtos.CvImportResultDto;
import com.khedmataktak.dto.wizard.WizardDtos.EducationDto;
import com.khedmataktak.dto.wizard.WizardDtos.ExperienceDto;
import com.khedmataktak.dto.wizard.WizardDtos.LanguageDto;
import com.khedmataktak.dto.wizard.WizardDtos.ProjectDto;
import com.khedmataktak.dto.wizard.WizardDtos.SkillDto;
import com.khedmataktak.dto.wizard.WizardDtos.UserProfileDto;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class CvParserService {

    private static final Pattern EMAIL = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", Pattern.CASE_INSENSITIVE);
    private static final Pattern LABELED_EMAIL = Pattern.compile(
            "(?i)(?:e-?mail|courriel|mail)\\s*[:.]?\\s*([A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,})");
    private static final Pattern PHONE = Pattern.compile(
            "(?:\\+\\d{1,3}[\\s.-]?)?(?:\\(?\\d{2,4}\\)?[\\s.-]?)?\\d{2,3}[\\s.-]?\\d{2,3}[\\s.-]?\\d{2,4}");
    private static final Pattern LABELED_PHONE = Pattern.compile(
            "(?i)(?:tel(?:éphone)?|phone|mobile|portable|gsm)\\s*[:.]?\\s*((?:\\+\\d[\\d\\s.-]{7,}\\d))");
    private static final Pattern LINKEDIN = Pattern.compile(
            "(?i)(?:https?://)?(?:www\\.)?linkedin\\.com/(?:in/)?[\\w%-]+");
    private static final Pattern LABELED_LINKEDIN = Pattern.compile(
            "(?i)linkedin\\s*[:.]?\\s*((?:https?://)?(?:www\\.)?linkedin\\.com/\\S+)");
    private static final Pattern GITHUB = Pattern.compile(
            "(?i)(?:https?://)?(?:www\\.)?github\\.com/[\\w-]+");
    private static final Pattern LABELED_GITHUB = Pattern.compile(
            "(?i)github\\s*[:.]?\\s*((?:https?://)?(?:www\\.)?github\\.com/\\S+)");
    private static final Pattern WEBSITE = Pattern.compile(
            "(?i)https?://(?!www\\.linkedin\\.com|linkedin\\.com|www\\.github\\.com|github\\.com)[\\w./?=&%-]+");
    private static final Pattern YEAR_RANGE = Pattern.compile(
            "(\\d{4})\\s*[-–—/|àa]+\\s*(\\d{4}|present|current|now|aujourd'?hui|présent|actuel|en cours)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern FRENCH_YEAR_RANGE = Pattern.compile(
            "(?:de\\s+)?(\\d{4})\\s*(?:à|a|jusqu'?à?|to|-–—)\\s*(\\d{4}|present|current|now|aujourd'?hui|présent|actuel|en cours)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_RANGE = Pattern.compile(
            "((?:\\d{1,2}/)?\\d{4}|(?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec|janvier|fevrier|février|mars|avril|mai|juin|juillet|aout|août|septembre|octobre|novembre|decembre|décembre)[a-zéû]*\\.?\\s+\\d{4})"
                    + "\\s*[-–—/|àtoaujusqu'\\s]+\\s*"
                    + "((?:\\d{1,2}/)?\\d{4}|(?:present|current|now|aujourd'?hui|présent|actuel|en cours)|"
                    + "(?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec|janvier|fevrier|février|mars|avril|mai|juin|juillet|aout|août|septembre|octobre|novembre|decembre|décembre)[a-zéû]*\\.?\\s+\\d{4})",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern TITLE_HINT = Pattern.compile(
            "(?i)\\b(développeur|developer|engineer|ingénieur|consultant|manager|architect|designer|analyst|technicien|lead|full[- ]stack|devops|data|product|chef de projet|administrateur)\\b");

    private static final Set<String> SKILL_STOPWORDS = Set.of(
            "et", "ou", "de", "des", "du", "la", "le", "les", "the", "and", "with", "for", "en", "sur", "dans",
            "ans", "années", "annees", "niveau", "bon", "bonne", "maitrise", "maîtrise", "connaissance", "connaissances"
    );

    private static final List<SectionRule> SECTION_RULES = List.of(
            new SectionRule("experience",
                    "experience", "experiences", "work experience", "professional experience", "employment history",
                    "employment", "work history", "expérience", "expériences", "experience professionnelle",
                    "experiences professionnelles", "expériences professionnelles", "parcours professionnel",
                    "parcours pro", "carriere", "carrière", "historique professionnel", "emplois", "postes occupés",
                    "postes occupes", "activité professionnelle", "activite professionnelle"),
            new SectionRule("education",
                    "education", "formation", "formations", "études", "etudes", "academic background", "academic",
                    "diplômes", "diplomes", "scolarite", "scolarité", "diplomes et formations", "formations et diplomes"),
            new SectionRule("skills",
                    "skills", "compétences", "competences", "technical skills", "core skills", "expertise",
                    "technologies", "outils", "stack technique", "tech stack", "savoir faire", "savoir-faire",
                    "compétences techniques", "competences techniques", "it skills", "hard skills"),
            new SectionRule("languages",
                    "languages", "langues", "language skills", "langues parlées", "langues parlees", "idiomes"),
            new SectionRule("certifications",
                    "certifications", "certificats", "certificates", "licenses", "licences", "certificats et formations",
                    "certifications et formations"),
            new SectionRule("projects",
                    "projects", "projets", "personal projects", "portfolio projects", "realisations", "réalisations",
                    "projets personnels", "projets professionnels")
    );

    private static final int MAX_TEXT_LENGTH = 80_000;

    public CvImportResultDto parse(String rawText) {
        return parseWithProgress(rawText, null);
    }

    public CvImportResultDto parseWithProgress(String rawText, Consumer<CvImportProgressEvent> progress) {
        emit(progress, "parse", 20, "Préparation du texte...", null, null, null, null);
        String normalized = preProcessText(normalize(rawText));
        List<String> lines = splitLines(normalized);
        Map<String, List<String>> sections = splitSections(lines);

        emit(progress, "parse", 25, "Analyse du profil...", "profile", null, null, null);
        List<String> headerLines = sections.getOrDefault("header", List.of());
        UserProfileDto profile = parseProfile(lines, headerLines, normalized);
        emit(progress, "parse", 30, "Profil analysé", "profile", hasProfileData(profile), null, null);

        emit(progress, "parse", 35, "Analyse des expériences...", "experiences", null, null, null);
        List<ExperienceDto> experiences = parseExperiences(sections.get("experience"));
        if (experiences.isEmpty()) {
            experiences = parseExperiencesFallback(lines);
        }
        emit(progress, "parse", 45, "Expériences analysées", "experiences", !experiences.isEmpty(), experiences.size(), null);

        emit(progress, "parse", 50, "Analyse des projets...", "projects", null, null, null);
        List<ProjectDto> projects = parseProjects(sections.get("projects"));
        emit(progress, "parse", 55, "Projets analysés", "projects", !projects.isEmpty(), projects.size(), null);

        emit(progress, "parse", 60, "Analyse de la formation...", "education", null, null, null);
        List<EducationDto> education = parseEducation(sections.get("education"));
        emit(progress, "parse", 65, "Formation analysée", "education", !education.isEmpty(), education.size(), null);

        emit(progress, "parse", 70, "Analyse des compétences...", "skills", null, null, null);
        List<SkillDto> skills = parseSkills(sections.get("skills"));
        emit(progress, "parse", 75, "Compétences analysées", "skills", !skills.isEmpty(), skills.size(), null);

        emit(progress, "parse", 80, "Analyse des langues...", "languages", null, null, null);
        List<LanguageDto> languages = parseLanguages(sections.get("languages"));
        emit(progress, "parse", 85, "Langues analysées", "languages", !languages.isEmpty(), languages.size(), null);

        emit(progress, "parse", 88, "Analyse des certifications...", "certifications", null, null, null);
        List<CertificationDto> certifications = parseCertifications(sections.get("certifications"));
        emit(progress, "parse", 92, "Certifications analysées", "certifications", !certifications.isEmpty(), certifications.size(), null);

        CvImportResultDto result = new CvImportResultDto(
                profile, experiences, projects, education, skills, languages, certifications, "heuristic", null
        );
        emit(progress, "complete", 100, "Analyse terminée", null, null, null, result);
        return result;
    }

    private boolean hasProfileData(UserProfileDto profile) {
        return profile != null && (
                isNotBlank(profile.firstName()) || isNotBlank(profile.lastName()) || isNotBlank(profile.email())
                        || isNotBlank(profile.title()) || isNotBlank(profile.summary()) || isNotBlank(profile.phone())
                        || isNotBlank(profile.location()) || isNotBlank(profile.website()) || isNotBlank(profile.linkedin())
                        || isNotBlank(profile.github())
        );
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private void emit(Consumer<CvImportProgressEvent> progress, String phase, int pct, String message,
                      String section, Boolean found, Integer count, CvImportResultDto result) {
        if (progress != null) {
            progress.accept(new CvImportProgressEvent(phase, pct, message, section, found, count, result));
        }
    }

    private String preProcessText(String text) {
        if (text.length() > MAX_TEXT_LENGTH) {
            text = text.substring(0, MAX_TEXT_LENGTH);
        }
        List<String> output = new ArrayList<>();
        for (String rawLine : text.split("\n")) {
            String line = rawLine.trim().replace('\t', ' ');
            if (line.isBlank()) {
                continue;
            }
            if (line.contains("  ") && line.length() > 35) {
                for (String part : line.split("\\s{2,}")) {
                    addPreprocessedLine(output, part.trim());
                }
                continue;
            }
            addPreprocessedLine(output, line);
        }
        return String.join("\n", output)
                .replace('|', '\n')
                .replace('•', '\n')
                .replace('▪', '\n')
                .replace('●', '\n')
                .replace('◦', '\n');
    }

    private void addPreprocessedLine(List<String> output, String line) {
        if (line.isBlank()) {
            return;
        }
        if (detectSection(line) != null) {
            output.add("");
            output.add(line.replaceAll("[:：·]+$", "").trim());
            output.add("");
        } else {
            output.add(line);
        }
    }

    private UserProfileDto parseProfile(List<String> allLines, List<String> headerLines, String fullText) {
        String contactText = fullText.length() > 15000 ? fullText.substring(0, 15000) : fullText;

        String email = labeledFirst(LABELED_EMAIL, contactText, 1);
        if (email == null) {
            email = firstMatch(EMAIL, contactText);
        }
        String phone = labeledFirst(LABELED_PHONE, contactText, 1);
        if (phone == null) {
            phone = firstMatch(PHONE, contactText);
        }
        String linkedin = normalizeUrl(labeledFirst(LABELED_LINKEDIN, contactText, 1));
        if (linkedin == null) {
            linkedin = normalizeUrl(firstMatch(LINKEDIN, contactText));
        }
        String github = normalizeUrl(labeledFirst(LABELED_GITHUB, contactText, 1));
        if (github == null) {
            github = normalizeUrl(firstMatch(GITHUB, contactText));
        }
        String website = firstMatch(WEBSITE, contactText);

        List<String> nameCandidates = headerLines.isEmpty()
                ? allLines.subList(0, Math.min(allLines.size(), 15))
                : headerLines;

        String nameLine = "";
        String titleLine = "";
        for (String line : nameCandidates) {
            if (containsContactMarker(line) || isSectionHeader(line)) {
                continue;
            }
            if (nameLine.isBlank() && looksLikeName(line)) {
                nameLine = line;
                continue;
            }
            if (titleLine.isBlank() && looksLikeTitle(line)) {
                titleLine = line;
                break;
            }
        }
        if (titleLine.isBlank()) {
            for (String line : nameCandidates) {
                if (!line.equals(nameLine) && !containsContactMarker(line) && !isSectionHeader(line)
                        && line.length() <= 120 && TITLE_HINT.matcher(line).find()) {
                    titleLine = line;
                    break;
                }
            }
        }

        String[] nameParts = splitName(nameLine);
        String summary = joinNonEmpty(sectionsSummaryLines(allLines), "\n");

        return WizardDtos.parsedProfile(
                nameParts[0], nameParts[1],
                email != null ? email : "",
                titleLine,
                summary,
                phone != null ? phone.trim() : "",
                extractLocation(contactText),
                website != null ? website : "",
                linkedin != null ? linkedin : "",
                github != null ? github : ""
        );
    }

    private boolean looksLikeTitle(String line) {
        if (line.length() > 120 || line.length() < 3) {
            return false;
        }
        return TITLE_HINT.matcher(line).find()
                || (line.length() <= 80 && !looksLikeName(line) && !containsDateRange(line));
    }

    private String labeledFirst(Pattern pattern, String text, int group) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(group).trim() : null;
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        String cleaned = url.replaceAll("[),.;]+$", "");
        return cleaned.startsWith("http") ? cleaned : "https://" + cleaned;
    }

    private List<String> sectionsSummaryLines(List<String> lines) {
        int summaryStart = -1;
        for (int i = 0; i < lines.size(); i++) {
            String lower = stripAccents(lines.get(i).toLowerCase(Locale.ROOT));
            if (lower.matches("^(summary|profil|profile|about|a propos|à propos|resume|résumé|profil professionnel).*")) {
                summaryStart = i + 1;
                break;
            }
        }
        if (summaryStart < 0) {
            return List.of();
        }
        List<String> summaryLines = new ArrayList<>();
        for (int i = summaryStart; i < lines.size(); i++) {
            if (isSectionHeader(lines.get(i))) {
                break;
            }
            summaryLines.add(lines.get(i));
        }
        return summaryLines;
    }

    private List<ExperienceDto> parseExperiences(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return List.of();
        }
        List<ExperienceDto> results = new ArrayList<>();
        for (List<String> block : splitBlocks(lines)) {
            ExperienceDto experience = parseExperienceBlock(block);
            if (experience != null) {
                results.add(experience);
            }
        }
        return dedupeExperiences(results);
    }

    private List<ExperienceDto> parseExperiencesFallback(List<String> lines) {
        List<ExperienceDto> results = new ArrayList<>();
        boolean inExperienceZone = false;

        for (String line : lines) {
            if (isSectionHeader(line)) {
                inExperienceZone = "experience".equals(detectSection(line));
                continue;
            }
            if (!inExperienceZone && !containsDateRange(line)) {
                continue;
            }
            if (containsDateRange(line)) {
                List<String> block = new ArrayList<>();
                block.add(line);
                ExperienceDto parsed = parseExperienceBlock(block);
                if (parsed != null) {
                    results.add(parsed);
                }
            }
        }
        return dedupeExperiences(results).stream().limit(15).toList();
    }

    private List<ExperienceDto> dedupeExperiences(List<ExperienceDto> experiences) {
        Set<String> seen = new LinkedHashSet<>();
        List<ExperienceDto> unique = new ArrayList<>();
        for (ExperienceDto exp : experiences) {
            String key = (exp.company() + "|" + exp.position() + "|" + exp.startDate()).toLowerCase(Locale.ROOT);
            if (seen.add(key)) {
                unique.add(exp);
            }
        }
        return unique;
    }

    private ExperienceDto parseExperienceBlock(List<String> block) {
        if (block.isEmpty()) {
            return null;
        }

        String company = "";
        String position = "";
        String startDate = "";
        String endDate = "";
        boolean current = false;
        List<String> descriptionLines = new ArrayList<>();
        int index = 0;

        // First line may combine dates + role + company
        String firstLine = block.get(0);
        if (containsDateRange(firstLine)) {
            DateRange range = extractDateRange(firstLine);
            startDate = range.start();
            endDate = range.end();
            current = range.current();
            String remainder = stripDateRanges(firstLine);
            if (!remainder.isBlank()) {
                String[] roleCompany = splitRoleCompany(remainder);
                position = roleCompany[0];
                company = roleCompany[1];
            }
            index = 1;
        }

        while (index < block.size() && containsDateRange(block.get(index))) {
            DateRange range = extractDateRange(block.get(index));
            startDate = range.start();
            endDate = range.end();
            current = range.current();
            index++;
        }

        if (index < block.size() && (company.isBlank() || position.isBlank())) {
            String[] roleCompany = splitRoleCompany(block.get(index));
            if (position.isBlank()) {
                position = roleCompany[0];
            }
            if (company.isBlank()) {
                company = roleCompany[1];
            }
            if (!roleCompany[0].isBlank() || !roleCompany[1].isBlank()) {
                index++;
            }
        }

        if (index < block.size() && company.isBlank() && !containsDateRange(block.get(index))) {
            company = block.get(index);
            index++;
        }

        if (index < block.size() && position.isBlank() && !containsDateRange(block.get(index))) {
            position = block.get(index);
            index++;
        }

        if (index < block.size() && containsDateRange(block.get(index))) {
            DateRange range = extractDateRange(block.get(index));
            startDate = range.start();
            endDate = range.end();
            current = range.current();
            index++;
        }

        for (; index < block.size(); index++) {
            descriptionLines.add(block.get(index));
        }

        if (company.isBlank() && position.isBlank()) {
            return null;
        }
        if (company.isBlank()) {
            company = position;
        }
        if (position.isBlank()) {
            position = company;
        }

        return new ExperienceDto(
                null, company.trim(), position.trim(), "",
                startDate, current ? "" : endDate, current,
                joinNonEmpty(descriptionLines, "\n")
        );
    }

    private String stripDateRanges(String line) {
        String result = DATE_RANGE.matcher(line).replaceAll(" ");
        result = YEAR_RANGE.matcher(result).replaceAll(" ");
        result = FRENCH_YEAR_RANGE.matcher(result).replaceAll(" ");
        return result.replaceAll("\\s{2,}", " ").trim();
    }

    private List<EducationDto> parseEducation(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return List.of();
        }
        List<EducationDto> results = new ArrayList<>();
        for (List<String> block : splitBlocks(lines)) {
            if (block.isEmpty()) {
                continue;
            }
            String firstLine = block.get(0);
            String institution = firstLine;
            String degree = "";
            String startDate = "";
            String endDate = "";
            List<String> description = new ArrayList<>();
            int index = 1;

            if (firstLine.contains(" - ") && !containsDateRange(firstLine)) {
                String[] parts = firstLine.split("\\s+-\\s+", 2);
                degree = parts[0].trim();
                institution = parts[1].trim();
            } else if (containsDateRange(firstLine)) {
                DateRange range = extractDateRange(firstLine);
                startDate = range.start();
                endDate = range.end();
                String remainder = stripDateRanges(firstLine);
                if (remainder.contains(" - ")) {
                    String[] parts = remainder.split("\\s+-\\s+", 2);
                    degree = parts[0].trim();
                    institution = parts[1].trim();
                } else {
                    institution = remainder;
                }
                index = 1;
            }

            for (; index < block.size(); index++) {
                if (containsDateRange(block.get(index))) {
                    DateRange range = extractDateRange(block.get(index));
                    startDate = range.start();
                    endDate = range.end();
                    index++;
                    break;
                }
            }
            if (degree.isBlank() && index < block.size() && !containsDateRange(block.get(index))) {
                degree = block.get(index);
                index++;
            }
            for (; index < block.size(); index++) {
                description.add(block.get(index));
            }

            if (institution.isBlank()) {
                continue;
            }
            results.add(new EducationDto(
                    null, institution, degree, "", startDate, endDate, joinNonEmpty(description, "\n"),
                    inferEducationType(degree)
            ));
        }
        return results;
    }

    private List<SkillDto> parseSkills(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return List.of();
        }
        Set<String> seen = new LinkedHashSet<>();
        List<SkillDto> skills = new ArrayList<>();
        for (String token : splitTokens(lines)) {
            String cleaned = token.trim();
            if (cleaned.length() < 2 || cleaned.length() > 60 || isSectionHeader(cleaned)) {
                continue;
            }
            String lower = stripAccents(cleaned.toLowerCase(Locale.ROOT));
            if (SKILL_STOPWORDS.contains(lower)) {
                continue;
            }
            if (lower.contains(":")) {
                // category line like "Langages: Java, Python"
                String afterColon = cleaned.substring(cleaned.indexOf(':') + 1).trim();
                for (String sub : splitInlineList(afterColon)) {
                    addSkill(skills, seen, sub);
                }
                continue;
            }
            addSkill(skills, seen, cleaned);
        }
        return skills.stream().limit(50).toList();
    }

    private void addSkill(List<SkillDto> skills, Set<String> seen, String name) {
        String trimmed = name.trim();
        if (trimmed.length() < 2 || trimmed.length() > 60) {
            return;
        }
        String key = stripAccents(trimmed.toLowerCase(Locale.ROOT));
        if (SKILL_STOPWORDS.contains(key) || !seen.add(key)) {
            return;
        }
        skills.add(new SkillDto(null, trimmed, "Intermediate", ""));
    }

    private List<LanguageDto> parseLanguages(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return List.of();
        }
        List<LanguageDto> languages = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split("[:\\-–—|]", 2);
            if (parts.length == 2 && parts[0].trim().length() <= 40) {
                languages.add(new LanguageDto(null, parts[0].trim(), parts[1].trim()));
            } else {
                for (String token : splitInlineList(line)) {
                    if (token.length() <= 40 && !isSectionHeader(token)) {
                        languages.add(new LanguageDto(null, token, "Conversational"));
                    }
                }
            }
        }
        return languages.stream().limit(15).toList();
    }

    private List<CertificationDto> parseCertifications(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return List.of();
        }
        List<CertificationDto> certifications = new ArrayList<>();
        for (List<String> block : splitBlocks(lines)) {
            if (block.isEmpty()) {
                continue;
            }
            String name = block.get(0);
            String issuer = block.size() > 1 ? block.get(1) : "";
            String date = "";
            for (String line : block) {
                Matcher year = Pattern.compile("(\\d{4})").matcher(line);
                if (year.find()) {
                    date = year.group(1) + "-01-01";
                }
            }
            certifications.add(new CertificationDto(null, name, issuer, date, "", inferCertificationType(name)));
        }
        return certifications;
    }

    private List<ProjectDto> parseProjects(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return List.of();
        }
        List<ProjectDto> projects = new ArrayList<>();
        for (List<String> block : splitBlocks(lines)) {
            if (block.isEmpty()) {
                continue;
            }
            String name = block.get(0);
            String description = block.size() > 1 ? joinNonEmpty(block.subList(1, block.size()), "\n") : "";
            projects.add(new ProjectDto(null, name, description, "", "", "", ""));
        }
        return projects;
    }

    private Map<String, List<String>> splitSections(List<String> lines) {
        Map<String, List<String>> sections = new LinkedHashMap<>();
        sections.put("header", new ArrayList<>());

        String current = "header";
        for (String line : lines) {
            String section = detectSection(line);
            if (section != null) {
                current = section;
                sections.computeIfAbsent(current, key -> new ArrayList<>());
                continue;
            }
            sections.computeIfAbsent(current, key -> new ArrayList<>()).add(line);
        }
        return sections;
    }

    private String detectSection(String line) {
        if (line.length() > 100) {
            return null;
        }
        String cleaned = line.trim().replaceAll("[:：·]+$", "").trim();
        String normalized = stripAccents(cleaned.toLowerCase(Locale.ROOT))
                .replaceAll("[^a-z0-9\\s']", " ")
                .replaceAll("\\s+", " ")
                .trim();
        normalized = normalized.replaceFirst("^\\d+[.)\\s]+", "");
        if (normalized.length() < 3) {
            return null;
        }
        for (SectionRule rule : SECTION_RULES) {
            if (rule.matches(normalized)) {
                return rule.key();
            }
        }
        return null;
    }

    private boolean isSectionHeader(String line) {
        return detectSection(line) != null;
    }

    private List<List<String>> splitBlocks(List<String> lines) {
        List<List<String>> blocks = new ArrayList<>();
        List<String> current = new ArrayList<>();
        for (String line : lines) {
            if (line.isBlank()) {
                if (!current.isEmpty()) {
                    blocks.add(current);
                    current = new ArrayList<>();
                }
                continue;
            }
            if (!current.isEmpty() && looksLikeExperienceHeader(line)) {
                blocks.add(current);
                current = new ArrayList<>();
            }
            current.add(line);
        }
        if (!current.isEmpty()) {
            blocks.add(current);
        }
        return blocks;
    }

    private boolean looksLikeExperienceHeader(String line) {
        return containsDateRange(line) && line.length() < 160;
    }

    private List<String> splitTokens(List<String> lines) {
        List<String> tokens = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith("•") || line.startsWith("-") || line.startsWith("*") || line.startsWith("▪")) {
                tokens.add(cleanBullet(line));
                continue;
            }
            tokens.addAll(splitInlineList(line));
        }
        return tokens;
    }

    private List<String> splitInlineList(String line) {
        String[] parts = line.split("[,;|/•]+");
        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            String cleaned = part.trim();
            if (!cleaned.isBlank()) {
                tokens.add(cleaned);
            }
        }
        return tokens;
    }

    private String[] splitRoleCompany(String line) {
        String trimmed = line.trim();
        String[] separators = {
                " chez ", " at ", " @ ", " pour ", " - ", " – ", " — ", " | ", " / ", ", "
        };
        String lower = " " + trimmed.toLowerCase(Locale.ROOT) + " ";
        for (String separator : separators) {
            int index = lower.indexOf(separator);
            if (index >= 0) {
                String left = trimmed.substring(0, index).trim();
                String right = trimmed.substring(index + separator.length()).trim();
                if (looksLikeCompany(right)) {
                    return new String[]{left, right};
                }
                if (looksLikeCompany(left)) {
                    return new String[]{right, left};
                }
                return new String[]{left, right};
            }
        }
        if (trimmed.contains(" - ")) {
            String[] parts = trimmed.split("\\s+-\\s+", 2);
            if (parts.length == 2) {
                if (looksLikeCompany(parts[1])) {
                    return new String[]{parts[0].trim(), parts[1].trim()};
                }
                if (looksLikeCompany(parts[0])) {
                    return new String[]{parts[1].trim(), parts[0].trim()};
                }
                return new String[]{parts[0].trim(), parts[1].trim()};
            }
        }
        if (looksLikeCompany(trimmed)) {
            return new String[]{"", trimmed};
        }
        return new String[]{trimmed, ""};
    }

    private boolean looksLikeCompany(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String lower = stripAccents(value.toLowerCase(Locale.ROOT));
        return lower.matches(".*\\b(sarl|sas|sa|gmbh|ltd|inc|corp|group|groupe|bank|banque|université|universite|ecole|école|school|consulting|services|technologies|tech)\\b.*")
                || (value.equals(value.toUpperCase(Locale.ROOT)) && value.length() > 3 && value.length() < 60);
    }

    private String normalize(String text) {
        return text.replace('\r', '\n')
                .replace('\u00A0', ' ')
                .replace('\f', '\n')
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }

    private List<String> splitLines(String text) {
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\n")) {
            String trimmed = line.trim();
            if (!trimmed.isBlank()) {
                if (trimmed.length() > 200) {
                    for (String chunk : trimmed.split("(?i)\\s{2,}|\\s*[|•]\\s*")) {
                        if (!chunk.isBlank()) {
                            lines.add(chunk.trim());
                        }
                    }
                } else {
                    lines.add(trimmed);
                }
            }
        }
        return lines;
    }

    private String firstMatch(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group().trim() : null;
    }

    private boolean containsContactMarker(String line) {
        String lower = line.toLowerCase(Locale.ROOT);
        return EMAIL.matcher(line).find()
                || PHONE.matcher(line).find()
                || lower.contains("linkedin")
                || lower.contains("github")
                || lower.contains("http")
                || lower.contains("@")
                || lower.contains("www.");
    }

    private boolean looksLikeName(String line) {
        if (line.length() > 70 || line.length() < 3) {
            return false;
        }
        if (containsContactMarker(line) || containsDateRange(line) || isSectionHeader(line)) {
            return false;
        }
        if (TITLE_HINT.matcher(line).find() && line.split("\\s+").length > 2) {
            return false;
        }
        return line.matches("^[\\p{L} .'-]+$") && line.split("\\s+").length <= 5;
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return new String[]{"", ""};
        }
        String cleaned = fullName.trim();
        if (cleaned.equals(cleaned.toUpperCase(Locale.ROOT)) && cleaned.contains(" ")) {
            String[] parts = cleaned.split("\\s+", 2);
            return parts.length == 1 ? new String[]{parts[0], ""} : new String[]{parts[1], parts[0]};
        }
        String[] parts = cleaned.split("\\s+", 2);
        return parts.length == 1 ? new String[]{parts[0], ""} : new String[]{parts[0], parts[1]};
    }

    private String extractLocation(String text) {
        Matcher matcher = Pattern.compile(
                "(?i)(?:location|localisation|address|adresse|ville|based in|résidence|residence)\\s*[:\\-]\\s*(.+?)(?:\\n|$)"
        ).matcher(text);
        return matcher.find() ? matcher.group(1).trim() : "";
    }

    private boolean containsDateRange(String line) {
        return DATE_RANGE.matcher(line).find()
                || YEAR_RANGE.matcher(line).find()
                || FRENCH_YEAR_RANGE.matcher(line).find()
                || line.matches(".*\\b(19|20)\\d{2}\\s*[-–—/]\\s*((19|20)\\d{2}|present|présent|actuel|en cours).*");
    }

    private DateRange extractDateRange(String line) {
        Matcher matcher = DATE_RANGE.matcher(line);
        if (matcher.find()) {
            String end = normalizeDateToken(matcher.group(2));
            return new DateRange(normalizeDateToken(matcher.group(1)), end, isCurrent(matcher.group(2)));
        }
        Matcher frenchMatcher = FRENCH_YEAR_RANGE.matcher(line);
        if (frenchMatcher.find()) {
            return new DateRange(
                    frenchMatcher.group(1) + "-01-01",
                    isCurrent(frenchMatcher.group(2)) ? "" : frenchMatcher.group(2) + "-01-01",
                    isCurrent(frenchMatcher.group(2))
            );
        }
        Matcher yearMatcher = YEAR_RANGE.matcher(line);
        if (yearMatcher.find()) {
            return new DateRange(
                    yearMatcher.group(1) + "-01-01",
                    isCurrent(yearMatcher.group(2)) ? "" : yearMatcher.group(2) + "-01-01",
                    isCurrent(yearMatcher.group(2))
            );
        }
        Matcher looseYear = Pattern.compile("((?:19|20)\\d{2})\\s*[-–—/]\\s*((?:19|20)\\d{2}|present|présent|actuel|en cours)", Pattern.CASE_INSENSITIVE).matcher(line);
        if (looseYear.find()) {
            return new DateRange(
                    looseYear.group(1) + "-01-01",
                    isCurrent(looseYear.group(2)) ? "" : looseYear.group(2) + "-01-01",
                    isCurrent(looseYear.group(2))
            );
        }
        return new DateRange("", "", false);
    }

    private String normalizeDateToken(String token) {
        if (token == null || token.isBlank() || isCurrent(token)) {
            return "";
        }
        Matcher monthYear = Pattern.compile("(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec|janvier|fevrier|février|mars|avril|mai|juin|juillet|aout|août|septembre|octobre|novembre|decembre|décembre)[a-zéû]*\\.?\\s+(\\d{4})").matcher(token);
        if (monthYear.find()) {
            return monthYear.group(2) + "-01-01";
        }
        if (token.matches("\\d{4}")) {
            return token + "-01-01";
        }
        if (token.matches("\\d{1,2}/\\d{4}")) {
            String[] parts = token.split("/");
            String month = parts[0].length() == 1 ? "0" + parts[0] : parts[0];
            return parts[1] + "-" + month + "-01";
        }
        return token;
    }

    private boolean isCurrent(String value) {
        if (value == null) {
            return false;
        }
        String lower = stripAccents(value.toLowerCase(Locale.ROOT));
        return lower.contains("present") || lower.contains("current") || lower.contains("aujourd")
                || lower.contains("actuel") || lower.contains("en cours") || lower.equals("now");
    }

    private String stripAccents(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private String cleanBullet(String line) {
        return line.replaceFirst("^[-*•▪●◦\\s]+", "").trim();
    }

    private String joinNonEmpty(List<String> lines, String delimiter) {
        return lines.stream().filter(line -> !line.isBlank()).reduce((a, b) -> a + delimiter + b).orElse("");
    }

    private record DateRange(String start, String end, boolean current) {
    }

    private record SectionRule(String key, String... labels) {
        boolean matches(String normalizedLine) {
            String padded = " " + normalizedLine + " ";
            for (String label : labels) {
                String normalizedLabel = Normalizer.normalize(label, Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "")
                        .toLowerCase(Locale.ROOT);
                if (normalizedLine.equals(normalizedLabel)
                        || normalizedLine.startsWith(normalizedLabel + " ")
                        || normalizedLine.endsWith(" " + normalizedLabel)
                        || padded.contains(" " + normalizedLabel + " ")) {
                    return true;
                }
            }
            return false;
        }
    }

    private String inferEducationType(String degree) {
        String lower = degree != null ? degree.toLowerCase(Locale.ROOT) : "";
        if (lower.contains("cap") || lower.contains("bep") || lower.contains("bac pro")
                || lower.contains("bp ") || lower.contains("cqp") || lower.contains("titre pro")) {
            return "VOCATIONAL";
        }
        return "ACADEMIC";
    }

    private String inferCertificationType(String name) {
        String lower = name != null ? name.toLowerCase(Locale.ROOT) : "";
        if (lower.contains("caces") || lower.contains("habilitation") || lower.contains("sst")
                || lower.contains("electrique") || lower.contains("amiante")) {
            return "REGULATORY";
        }
        return "PROFESSIONAL";
    }
}
