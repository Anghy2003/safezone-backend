package com.ista.springboot.web.app.grok.controller;

import com.ista.springboot.web.app.grok.service.GrokVisionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class ImageAnalyzerController {

    private final GrokVisionService grokVisionService;

    public ImageAnalyzerController(GrokVisionService grokVisionService) {
        this.grokVisionService = grokVisionService;
    }

    @PostMapping("/analyze-incident")
    public ResponseEntity<GrokVisionService.AiClassification> analyzeIncident(
            @RequestBody GrokVisionService.AnalyzeRequest request
    ) {
        return ResponseEntity.ok(grokVisionService.analyzeIncident(request));
    }
}
