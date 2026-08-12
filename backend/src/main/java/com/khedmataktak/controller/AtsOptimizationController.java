package com.khedmataktak.controller;

import com.khedmataktak.domain.CvOptimizationMode;
import com.khedmataktak.domain.TradeCategoryCatalog;
import com.khedmataktak.domain.TradeCategoryCatalog.TradeCategory;
import com.khedmataktak.dto.ats.AtsDtos.AtsAnalyzeRequest;
import com.khedmataktak.dto.ats.AtsDtos.AtsAnalysisResult;
import com.khedmataktak.dto.ats.AtsDtos.AtsOptimizeResult;
import com.khedmataktak.security.SecurityUtils;
import com.khedmataktak.service.AtsOptimizationService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AtsOptimizationController {

    private final AtsOptimizationService atsOptimizationService;

    public AtsOptimizationController(AtsOptimizationService atsOptimizationService) {
        this.atsOptimizationService = atsOptimizationService;
    }

    @GetMapping("/trades/categories")
    public List<TradeCategory> listTradeCategories() {
        return TradeCategoryCatalog.all();
    }

    @PostMapping("/ats/analyze")
    public AtsAnalysisResult analyze(@RequestBody AtsAnalyzeRequest request) {
        return atsOptimizationService.analyze(
                SecurityUtils.currentUserId(),
                parseMode(request.mode()),
                request.targetDomain()
        );
    }

    @PostMapping("/ats/optimize")
    public AtsOptimizeResult optimize(@RequestBody AtsAnalyzeRequest request) {
        return atsOptimizationService.optimize(
                SecurityUtils.currentUserId(),
                parseMode(request.mode()),
                request.targetDomain()
        );
    }

    private CvOptimizationMode parseMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return CvOptimizationMode.OFFICE;
        }
        try {
            return CvOptimizationMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return CvOptimizationMode.OFFICE;
        }
    }
}
