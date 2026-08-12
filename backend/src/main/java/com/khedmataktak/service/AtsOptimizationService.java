package com.khedmataktak.service;

import com.khedmataktak.domain.CvOptimizationMode;
import com.khedmataktak.dto.ats.AtsDtos.AtsAnalysisResult;
import com.khedmataktak.dto.ats.AtsDtos.AtsOptimizeResult;
import com.khedmataktak.dto.wizard.WizardDtos.PortfolioDto;
import com.khedmataktak.dto.wizard.WizardDtos.UserProfileDto;
import com.khedmataktak.entity.CertificationType;
import com.khedmataktak.entity.EducationType;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AtsOptimizationService {

    private static final Map<String, List<String>> TRADE_KEYWORDS = Map.ofEntries(
            Map.entry("plomberie", List.of("sanitaire", "chauffage", "tuyauterie", "cuivre", "PER", "dépannage")),
            Map.entry("menuiserie", List.of("ossature bois", "agencement", "ébénisterie", "charpente", "pose")),
            Map.entry("maconnerie", List.of("gros œuvre", "enduit", "parpaing", "béton", "façade", "terrassement")),
            Map.entry("electricite", List.of("tableau électrique", "câblage", "domotique", "habilitation", "BR")),
            Map.entry("peinture", List.of("préparation support", "revêtement", "façade", "décoration", "sol")),
            Map.entry("chauffage", List.of("climatisation", "pompe à chaleur", "gaz", "ventilation", "CVC"))
    );

    private static final Map<String, List<String>> OFFICE_KEYWORDS = Map.ofEntries(
            Map.entry("tech", List.of("agile", "API", "cloud", "CI/CD", "architecture", "microservices")),
            Map.entry("marketing", List.of("SEO", "analytics", "campaign", "content", "conversion", "brand")),
            Map.entry("finance", List.of("comptabilité", "audit", "budget", "reporting", "IFRS", "trésorerie"))
    );

    private final PortfolioWizardService portfolioWizardService;

    public AtsOptimizationService(PortfolioWizardService portfolioWizardService) {
        this.portfolioWizardService = portfolioWizardService;
    }

    @Transactional(readOnly = true)
    public AtsAnalysisResult analyze(UUID userId, CvOptimizationMode mode, String targetDomain) {
        PortfolioDto portfolio = portfolioWizardService.getPortfolio(userId);
        return buildAnalysis(portfolio, mode, targetDomain);
    }

    @Transactional(readOnly = true)
    public AtsOptimizeResult optimize(UUID userId, CvOptimizationMode mode, String targetDomain) {
        PortfolioDto portfolio = portfolioWizardService.getPortfolio(userId);
        AtsAnalysisResult analysis = buildAnalysis(portfolio, mode, targetDomain);
        UserProfileDto optimized = buildOptimizedProfile(portfolio.profile(), analysis, mode);
        return new AtsOptimizeResult(analysis, optimized);
    }

    private AtsAnalysisResult buildAnalysis(PortfolioDto portfolio, CvOptimizationMode mode, String targetDomain) {
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        List<String> keywords = suggestedKeywords(mode, targetDomain);

        int score = mode == CvOptimizationMode.TRADES
                ? scoreTrades(portfolio, strengths, weaknesses, recommendations, keywords)
                : scoreOffice(portfolio, strengths, weaknesses, recommendations, keywords, targetDomain);

        score = Math.min(100, Math.max(0, score));
        return new AtsAnalysisResult(score, mode.name(), targetDomain, strengths, weaknesses, recommendations, keywords);
    }

    private int scoreOffice(PortfolioDto portfolio,
                            List<String> strengths,
                            List<String> weaknesses,
                            List<String> recommendations,
                            List<String> keywords,
                            String targetDomain) {
        int score = 0;
        UserProfileDto profile = portfolio.profile();

        if (isNotBlank(profile.title())) {
            score += 10;
            strengths.add("Titre professionnel renseigné");
        } else {
            weaknesses.add("Titre professionnel manquant");
            recommendations.add("Ajoutez un titre clair (ex. Développeur Full Stack)");
        }

        if (profile.summary() != null && profile.summary().length() >= 50) {
            score += 15;
            strengths.add("Résumé professionnel détaillé");
        } else {
            weaknesses.add("Résumé trop court ou absent");
            recommendations.add("Rédigez un résumé de 3–5 lignes avec vos points forts");
        }

        int expCount = portfolio.experiences() != null ? portfolio.experiences().size() : 0;
        if (expCount > 0) {
            score += 20;
            strengths.add(expCount + " expérience(s) renseignée(s)");
        } else {
            weaknesses.add("Aucune expérience professionnelle");
            recommendations.add("Ajoutez vos expériences avec verbes d'action (développé, piloté, optimisé…)");
        }

        int skillCount = portfolio.skills() != null ? portfolio.skills().size() : 0;
        if (skillCount >= 5) {
            score += 15;
            strengths.add("Compétences variées (" + skillCount + ")");
        } else {
            weaknesses.add("Peu de compétences listées");
            recommendations.add("Listez au moins 5 compétences pertinentes pour votre domaine");
        }

        int eduCount = portfolio.education() != null ? portfolio.education().size() : 0;
        if (eduCount > 0) {
            score += 10;
            strengths.add("Formation renseignée");
        } else {
            weaknesses.add("Formation absente");
        }

        if (isNotBlank(profile.email()) && isNotBlank(profile.phone())) {
            score += 10;
            strengths.add("Coordonnées complètes");
        } else {
            weaknesses.add("Coordonnées incomplètes");
            recommendations.add("Vérifiez email et téléphone");
        }

        score += keywordMatchScore(portfolio, keywords);
        if (score >= 80 && keywordMatchScore(portfolio, keywords) >= 15) {
            strengths.add("Bon alignement avec le domaine cible : " + targetDomain);
        } else if (!keywords.isEmpty()) {
            weaknesses.add("Peu de mots-clés du domaine « " + targetDomain + " »");
            recommendations.add("Intégrez ces mots-clés : " + String.join(", ", keywords.subList(0, Math.min(5, keywords.size()))));
        }

        return score;
    }

    private int scoreTrades(PortfolioDto portfolio,
                            List<String> strengths,
                            List<String> weaknesses,
                            List<String> recommendations,
                            List<String> keywords) {
        int score = 0;
        UserProfileDto profile = portfolio.profile();

        if (isNotBlank(profile.primaryTrade())) {
            score += 15;
            strengths.add("Métier principal défini : " + profile.primaryTrade());
        } else {
            weaknesses.add("Métier principal non renseigné");
            recommendations.add("Indiquez votre métier (plomberie, menuiserie, maçonnerie…)");
        }

        long regulatoryCerts = countCertificationsByType(portfolio, CertificationType.REGULATORY.name());
        long totalCerts = portfolio.certifications() != null ? portfolio.certifications().size() : 0;
        if (regulatoryCerts > 0) {
            score += 20;
            strengths.add("Habilitations / certifications réglementaires présentes");
        } else if (totalCerts > 0) {
            score += 10;
            strengths.add("Certifications professionnelles renseignées");
        } else {
            weaknesses.add("Aucune certification (CACES, habilitation, CAP…)");
            recommendations.add("Ajoutez vos certifications et habilitations obligatoires");
        }

        long vocational = countEducationByType(portfolio, EducationType.VOCATIONAL.name());
        if (vocational > 0) {
            score += 15;
            strengths.add("Formation professionnelle (CAP, BP, Bac Pro…) renseignée");
        } else {
            weaknesses.add("Formation professionnelle absente");
            recommendations.add("Ajoutez votre CAP, BP ou titre professionnel");
        }

        int expCount = portfolio.experiences() != null ? portfolio.experiences().size() : 0;
        if (expCount > 0) {
            score += 20;
            strengths.add("Expériences chantier / employeur renseignées");
        } else {
            weaknesses.add("Aucune expérience métier");
            recommendations.add("Décrivez vos chantiers et employeurs précédents");
        }

        if (isNotBlank(profile.drivingLicense())) {
            score += 10;
            strengths.add("Permis renseigné : " + profile.drivingLicense());
        } else {
            weaknesses.add("Permis de conduire non indiqué");
            recommendations.add("Indiquez votre permis (souvent requis sur chantier)");
        }

        if (profile.mobilityRadiusKm() != null && profile.mobilityRadiusKm() > 0) {
            score += 10;
            strengths.add("Zone de mobilité définie (" + profile.mobilityRadiusKm() + " km)");
        } else {
            weaknesses.add("Rayon de mobilité non renseigné");
            recommendations.add("Précisez votre rayon d'intervention en km");
        }

        if (isNotBlank(profile.toolsEquipment())) {
            score += 10;
            strengths.add("Outillage / équipements listés");
        } else {
            recommendations.add("Listez les outils et équipements que vous maîtrisez");
        }

        score += keywordMatchScore(portfolio, keywords);
        return score;
    }

    private int keywordMatchScore(PortfolioDto portfolio, List<String> keywords) {
        if (keywords.isEmpty()) {
            return 0;
        }
        String corpus = buildCorpus(portfolio).toLowerCase(Locale.ROOT);
        int matches = 0;
        for (String keyword : keywords) {
            if (corpus.contains(keyword.toLowerCase(Locale.ROOT))) {
                matches++;
            }
        }
        return Math.min(20, matches * 4);
    }

    private String buildCorpus(PortfolioDto portfolio) {
        StringBuilder sb = new StringBuilder();
        UserProfileDto p = portfolio.profile();
        if (p != null) {
            sb.append(nullToEmpty(p.title())).append(' ');
            sb.append(nullToEmpty(p.summary())).append(' ');
            sb.append(nullToEmpty(p.primaryTrade())).append(' ');
            sb.append(nullToEmpty(p.tradeSpecialties())).append(' ');
            sb.append(nullToEmpty(p.toolsEquipment())).append(' ');
        }
        if (portfolio.skills() != null) {
            portfolio.skills().forEach(s -> sb.append(s.name()).append(' '));
        }
        if (portfolio.experiences() != null) {
            portfolio.experiences().forEach(e -> {
                sb.append(e.position()).append(' ');
                sb.append(e.description()).append(' ');
            });
        }
        return sb.toString();
    }

    private UserProfileDto buildOptimizedProfile(UserProfileDto profile, AtsAnalysisResult analysis, CvOptimizationMode mode) {
        if (profile == null) {
            return null;
        }
        String summary = profile.summary();
        if (summary == null || summary.isBlank()) {
            summary = mode == CvOptimizationMode.TRADES
                    ? buildTradesSummary(profile, analysis.suggestedKeywords())
                    : buildOfficeSummary(profile, analysis.suggestedKeywords());
        } else if (!analysis.suggestedKeywords().isEmpty()) {
            summary = enrichSummary(summary, analysis.suggestedKeywords());
        }

        return new UserProfileDto(
                profile.id(),
                profile.firstName(),
                profile.lastName(),
                profile.email(),
                profile.title(),
                summary,
                profile.phone(),
                profile.location(),
                profile.website(),
                profile.linkedin(),
                profile.github(),
                profile.userType(),
                profile.photoUrl(),
                profile.primaryTrade(),
                profile.tradeSpecialties(),
                profile.drivingLicense(),
                profile.hasOwnVehicle(),
                profile.mobilityRadiusKm(),
                profile.toolsEquipment(),
                profile.studentInstitution(),
                profile.studentYear(),
                profile.internshipSought()
        );
    }

    private String buildTradesSummary(UserProfileDto profile, List<String> keywords) {
        String trade = isNotBlank(profile.primaryTrade()) ? profile.primaryTrade() : "professionnel du BTP";
        String mobility = profile.mobilityRadiusKm() != null
                ? " Mobilité : " + profile.mobilityRadiusKm() + " km."
                : "";
        String license = isNotBlank(profile.drivingLicense())
                ? " Permis " + profile.drivingLicense() + "."
                : "";
        String kw = keywords.isEmpty() ? "" : " Compétences : " + String.join(", ", keywords.subList(0, Math.min(4, keywords.size()))) + ".";
        return trade + " expérimenté, rigoureux et autonome sur chantier." + kw + mobility + license;
    }

    private String buildOfficeSummary(UserProfileDto profile, List<String> keywords) {
        String title = isNotBlank(profile.title()) ? profile.title() : "Professionnel";
        String kw = keywords.isEmpty() ? "" : " Expertise : " + String.join(", ", keywords.subList(0, Math.min(4, keywords.size()))) + ".";
        return title + " motivé avec une solide expérience." + kw + " Orienté résultats et amélioration continue.";
    }

    private String enrichSummary(String summary, List<String> keywords) {
        Set<String> missing = new LinkedHashSet<>();
        String lower = summary.toLowerCase(Locale.ROOT);
        for (String keyword : keywords.subList(0, Math.min(3, keywords.size()))) {
            if (!lower.contains(keyword.toLowerCase(Locale.ROOT))) {
                missing.add(keyword);
            }
        }
        if (missing.isEmpty()) {
            return summary;
        }
        return summary.trim() + " Compétences clés : " + String.join(", ", missing) + ".";
    }

    private List<String> suggestedKeywords(CvOptimizationMode mode, String targetDomain) {
        String key = targetDomain != null ? targetDomain.toLowerCase(Locale.ROOT).trim() : "";
        if (mode == CvOptimizationMode.TRADES) {
            for (var entry : TRADE_KEYWORDS.entrySet()) {
                if (key.contains(entry.getKey()) || entry.getKey().contains(key)) {
                    return entry.getValue();
                }
            }
            return TRADE_KEYWORDS.getOrDefault("plomberie", List.of());
        }
        for (var entry : OFFICE_KEYWORDS.entrySet()) {
            if (key.contains(entry.getKey()) || entry.getKey().contains(key)) {
                return entry.getValue();
            }
        }
        return OFFICE_KEYWORDS.getOrDefault("tech", List.of());
    }

    private long countCertificationsByType(PortfolioDto portfolio, String type) {
        if (portfolio.certifications() == null) {
            return 0;
        }
        return portfolio.certifications().stream()
                .filter(c -> type.equalsIgnoreCase(nullToEmpty(c.certificationType())))
                .count();
    }

    private long countEducationByType(PortfolioDto portfolio, String type) {
        if (portfolio.education() == null) {
            return 0;
        }
        return portfolio.education().stream()
                .filter(e -> type.equalsIgnoreCase(nullToEmpty(e.educationType())))
                .count();
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
