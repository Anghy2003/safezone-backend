package com.ista.springboot.web.app.grok.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class GrokChatService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String chatModel;

    public GrokChatService(
            ObjectMapper objectMapper,
            @Value("${xai.base-url:https://api.x.ai}") String baseUrl,
            @Value("${xai.api-key}") String apiKey,
            @Value("${xai.chat-model:grok-2-1212}") String chatModel
    ) {
        this.objectMapper = objectMapper;
        this.chatModel = chatModel;

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String chatIsis(String emergencyType, String userMessage, List<ChatTurn> history) {
        if (!StringUtils.hasText(userMessage)) {
            throw new IllegalArgumentException("userMessage es obligatorio");
        }

        String systemPrompt = """
Eres Isis Ayuda, la asistente virtual de SafeZone.
Responde siempre en español, de forma empática, cercana y calmada.
No uses listas ni pasos.
No des instrucciones médicas ni técnicas.
No incites a confrontaciones.
Si el usuario pide ayuda urgente, recuérdale que puede presionar tres veces el botón de volumen para enviar una alerta SafeZone.
""";

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // Historia (opcional)
        if (history != null) {
            for (ChatTurn t : history) {
                if (t != null && StringUtils.hasText(t.role()) && StringUtils.hasText(t.content())) {
                    messages.add(Map.of("role", t.role(), "content", t.content()));
                }
            }
        }

        // Mensaje actual (puedes inyectar emergencyType como contexto)
        String finalUser = (StringUtils.hasText(emergencyType))
                ? ("Tipo de emergencia: " + emergencyType + "\nMensaje: " + userMessage)
                : userMessage;

        messages.add(Map.of("role", "user", "content", finalUser));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", chatModel);
        payload.put("temperature", 0.5);
        payload.put("messages", messages);

        String raw = restClient.post()
                .uri("/v1/chat/completions")
                .body(payload)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(raw);
            String content = root.at("/choices/0/message/content").asText(null);
            if (!StringUtils.hasText(content)) {
                throw new IllegalStateException("Respuesta sin contenido en choices[0].message.content");
            }
            return content;
        } catch (Exception e) {
            throw new RuntimeException("Error parseando respuesta de xAI: " + e.getMessage(), e);
        }
    }

    public record ChatTurn(String role, String content) {}
}
