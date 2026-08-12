package com.khedmataktak.controller;

import com.khedmataktak.dto.PortfolioViewModel;
import com.khedmataktak.service.PortfolioDataService;
import com.khedmataktak.service.PortfolioRenderService;
import com.khedmataktak.service.ZipExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/u")
public class PublicPortfolioController {

    private final PortfolioDataService portfolioDataService;
    private final PortfolioRenderService portfolioRenderService;
    private final ZipExportService zipExportService;

    public PublicPortfolioController(PortfolioDataService portfolioDataService,
                                     PortfolioRenderService portfolioRenderService,
                                     ZipExportService zipExportService) {
        this.portfolioDataService = portfolioDataService;
        this.portfolioRenderService = portfolioRenderService;
        this.zipExportService = zipExportService;
    }

    @GetMapping(value = "/{slug}", produces = MediaType.TEXT_HTML_VALUE)
    public String viewPortfolio(@PathVariable String slug,
                                @RequestParam(defaultValue = "en") String lang) {
        PortfolioViewModel model = portfolioDataService.buildPublishedViewModel(slug, lang);
        return portfolioRenderService.renderPortfolio(model);
    }

    @GetMapping("/{slug}/export.zip")
    public ResponseEntity<byte[]> downloadExport(@PathVariable String slug,
                                                 @RequestParam(defaultValue = "en") String lang) {
        PortfolioViewModel model = portfolioDataService.buildPublishedViewModel(slug, lang);
        byte[] zip = zipExportService.exportZip(model);
        String filename = slug + "-export.zip";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(zip);
    }
}
