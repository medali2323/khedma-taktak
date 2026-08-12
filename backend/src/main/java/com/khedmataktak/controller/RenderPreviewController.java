package com.khedmataktak.controller;

import com.khedmataktak.security.SecurityUtils;
import com.khedmataktak.service.PortfolioDataService;
import com.khedmataktak.service.PortfolioRenderService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/render")
public class RenderPreviewController {

    private final PortfolioDataService portfolioDataService;
    private final PortfolioRenderService portfolioRenderService;

    public RenderPreviewController(PortfolioDataService portfolioDataService,
                                   PortfolioRenderService portfolioRenderService) {
        this.portfolioDataService = portfolioDataService;
        this.portfolioRenderService = portfolioRenderService;
    }

    @GetMapping(value = "/preview", produces = MediaType.TEXT_HTML_VALUE)
    public String preview(@RequestParam(defaultValue = "en") String lang) {
        return portfolioRenderService.renderPortfolio(
                portfolioDataService.buildViewModelForUser(SecurityUtils.currentUserId(), lang));
    }
}
