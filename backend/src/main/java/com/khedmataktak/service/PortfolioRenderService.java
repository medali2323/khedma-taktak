package com.khedmataktak.service;

import com.khedmataktak.dto.PortfolioViewModel;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class PortfolioRenderService {

    private final SpringTemplateEngine templateEngine;

    public PortfolioRenderService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String renderPortfolio(PortfolioViewModel model) {
        Context context = new Context();
        context.setVariable("portfolio", model);
        String template = "themes/" + sanitizeTheme(model.theme()) + "/portfolio";
        return templateEngine.process(template, context);
    }

    public String renderCv(PortfolioViewModel model) {
        Context context = new Context();
        context.setVariable("portfolio", model);
        return templateEngine.process("cv/ats/cv", context);
    }

    private String sanitizeTheme(String theme) {
        if (theme == null || !theme.matches("^[a-zA-Z0-9_-]+$")) {
            return "classic";
        }
        return theme;
    }
}
