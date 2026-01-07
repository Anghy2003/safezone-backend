package com.ista.springboot.web.app.controllers;



import com.ista.springboot.web.app.grok.service.GrokChatService;
import com.ista.springboot.web.app.grok.service.GrokVisionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final GrokChatService chatService;
    private final GrokVisionService visionService;

    public AiController(GrokChatService chatService, GrokVisionService visionService) {
        this.chatService = chatService;
        this.visionService = visionService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest req) {
        String reply = chatService.chatIsis(req.emergencyType(), req.userMessage(), req.history());
        return new ChatResponse(reply);
    }

    @PostMapping("/analyze")
    public GrokVisionService.AiClassification analyze(@RequestBody GrokVisionService.AnalyzeRequest req) {
        return visionService.analyzeIncident(req);
    }

    public record ChatRequest(
            String emergencyType,
            String userMessage,
            List<GrokChatService.ChatTurn> history
    ) {}

    public record ChatResponse(String reply) {}
}
