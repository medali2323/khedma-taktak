package com.khedmataktak.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CvParserServiceTest {

    private final CvParserService parser = new CvParserService();

    @Test
    void parseExtractsContactsExperiencesAndSkills() {
        String cv = """
                Jean Dupont
                Développeur Full Stack
                jean.dupont@email.com | +33 6 12 34 56 78
                linkedin.com/in/jeandupont | github.com/jeandupont

                Expérience professionnelle
                Développeur Java - ACME Corp
                2020 - Present
                Développement Spring Boot et Angular

                Formation
                Master Informatique - Université Paris
                2018 - 2020

                Compétences
                Java, Spring Boot, Angular, Docker, MySQL
                """;

        var result = parser.parse(cv);

        assertTrue(result.profile().github().contains("github.com/jeandupont"));
        assertTrue(result.profile().linkedin().contains("linkedin.com/in/jeandupont"));
        assertFalse(result.experiences().isEmpty());
        assertTrue(result.experiences().getFirst().company().toLowerCase().contains("acme"));
        assertFalse(result.skills().isEmpty());
        assertFalse(result.education().isEmpty());
    }

    @Test
    void parseHandlesFrenchCvWithCapsAndInlineDates() {
        String cv = """
                DUPONT Jean
                Ingénieur DevOps
                Email: jean.dupont@pro.fr
                LinkedIn: linkedin.com/in/jeandupont
                GitHub: github.com/jdupont
                Téléphone: +33 6 11 22 33 44

                EXPÉRIENCES PROFESSIONNELLES
                Développeur Java chez BNP Paribas    2019 à 2022
                Migration cloud AWS et CI/CD

                DevOps Engineer - Capgemini    de 2022 à aujourd'hui
                Kubernetes, Docker, Terraform

                FORMATIONS
                Master Informatique - Université Paris    2016 - 2018

                COMPÉTENCES
                Java, Spring, Docker, Kubernetes, AWS, Terraform
                Langages: Python, TypeScript
                """;

        var result = parser.parse(cv);

        assertTrue(result.profile().email().contains("jean.dupont@pro.fr"));
        assertTrue(result.profile().linkedin().contains("linkedin.com/in/jeandupont"));
        assertTrue(result.profile().github().contains("github.com/jdupont"));
        assertTrue(result.experiences().size() >= 2);
        assertTrue(result.experiences().stream().anyMatch(e -> e.company().toLowerCase().contains("bnp")));
        assertTrue(result.experiences().stream().anyMatch(e -> e.company().toLowerCase().contains("capgemini")));
        assertFalse(result.education().isEmpty());
        assertTrue(result.skills().size() >= 5);
    }
}
