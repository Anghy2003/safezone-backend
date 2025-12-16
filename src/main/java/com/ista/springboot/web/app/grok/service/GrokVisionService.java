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
public class GrokVisionService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    private final String model;
    private final String imageDetail;
    private final int maxOutputTokens;

    public GrokVisionService(
            ObjectMapper objectMapper,
            @Value("${xai.base-url:https://api.x.ai}") String baseUrl,
            @Value("${xai.api-key}") String apiKey,
            @Value("${xai.model:grok-2-vision-1212}") String model,
            @Value("${xai.image-detail:low}") String imageDetail,
            @Value("${xai.max-output-tokens:350}") int maxOutputTokens
    ) {
        this.objectMapper = objectMapper;
        this.model = model;
        this.imageDetail = imageDetail;
        this.maxOutputTokens = maxOutputTokens;

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Clasifica incidente con texto + imágenes (URLs o base64 data URL).
     * Para video: pásale frames como imageUrls.
     * Para audio: pásale transcript en el texto (transcripción).
     */
    public AiClassification analyzeIncident(AnalyzeRequest req) {
        if (req == null || !StringUtils.hasText(req.text())) {
            throw new IllegalArgumentException("El campo 'text' es obligatorio.");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("max_tokens", maxOutputTokens);
        payload.put("temperature", 0.2);

        // System prompt: salida JSON estricta (sin texto extra)
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content",
                "Eres un clasificador de incidentes de una app de seguridad comunitaria (SafeZone). " +
                "Devuelve SOLO un JSON válido (sin markdown) con: " +
                "{category, priority, confidence, possible_fake, reasons, risk_flags, recommended_action}. " +
                "priority: ALTA|MEDIA|BAJA. confidence: 0..1. possible_fake: boolean."
        ));

        // User content multimodal: lista de {type:image_url|text}
        List<Map<String, Object>> userContent = new ArrayList<>();

        // Imágenes por URL (recomendado)
        if (req.imageUrls() != null) {
            for (String url : req.imageUrls()) {
                if (StringUtils.hasText(url)) {
                    userContent.add(Map.of(
                            "type", "image_url",
                            "image_url", Map.of(
                                    "url", url,
                                    "detail", imageDetail // low para ahorrar tokens
                            )
                    ));
                }
            }
        }

        // Imagen base64: debe venir como data URL: data:image/jpeg;base64,XXXX
        if (StringUtils.hasText(req.imageBase64DataUrl())) {
            userContent.add(Map.of(
                    "type", "image_url",
                    "image_url", Map.of(
                            "url", req.imageBase64DataUrl(),
                            "detail", imageDetail
                    )
            ));
        }

        // Texto (incluye transcript si lo tienes)
        userContent.add(Map.of(
                "type", "text",
                "text", buildPrompt(req)
        ));

        messages.add(Map.of("role", "user", "content", userContent));
        payload.put("messages", messages);

        // POST /v1/chat/completions
        String raw = restClient.post()
                .uri("/v1/chat/completions")
                .body(payload)
                .retrieve()
                .body(String.class);

        return parseClassificationFromResponse(raw);
    }

    private String buildPrompt(AnalyzeRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append("Clasifica este incidente.\n\n");
        sb.append("DESCRIPCION:\n").append(req.text()).append("\n\n");

        if (StringUtils.hasText(req.audioTranscript())) {
            sb.append("AUDIO_TRANSCRIPCION:\n").append(req.audioTranscript()).append("\n\n");
        }

        if (StringUtils.hasText(req.userContext())) {
            sb.append("CONTEXTO_USUARIO:\n").append(req.userContext()).append("\n\n");
        }

        sb.append("REGLAS PARA 'possible_fake':\n");
        sb.append("- Si no hay evidencia (sin descripcion util y sin imagen): probable.\n");
        sb.append("- Si contradicciones fuertes entre imagen y texto: probable.\n");
        sb.append("- Si el reporte no describe riesgo real: probable.\n\n");
        sb.append("Devuelve SOLO JSON.");
        return sb.toString();
    }

    private AiClassification parseClassificationFromResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String content = root.at("/choices/0/message/content").asText(null);
            if (!StringUtils.hasText(content)) {
                throw new IllegalStateException("Respuesta sin contenido en choices[0].message.content");
            }

            // Asegurar JSON aunque el modelo meta texto extra (fallback)
            String json = extractJsonObject(content);

            JsonNode j = objectMapper.readTree(json);

            return new AiClassification(
                    textOrNull(j, "category"),
                    textOrNull(j, "priority"),
                    j.path("confidence").isNumber() ? j.path("confidence").asDouble() : null,
                    j.path("possible_fake").isBoolean() ? j.path("possible_fake").asBoolean() : null,
                    arrayToList(j.path("reasons")),
                    arrayToList(j.path("risk_flags")),
                    textOrNull(j, "recommended_action"),
                    rawResponse // útil para debug
            );
        } catch (Exception e) {
            throw new RuntimeException("No se pudo parsear la respuesta de xAI: " + e.getMessage(), e);
        }
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v != null && v.isTextual()) ? v.asText() : null;
    }

    private static List<String> arrayToList(JsonNode arr) {
        if (arr == null || !arr.isArray()) return List.of();
        List<String> out = new ArrayList<>();
        for (JsonNode n : arr) {
            if (n.isTextual()) out.add(n.asText());
        }
        return out;
    }

    private static String extractJsonObject(String s) {
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return s.substring(start, end + 1).trim();
        }
        // si no hay llaves, fallamos explícito
        throw new IllegalStateException("El modelo no devolvió JSON. Respuesta: " + s);
    }

    // ===== DTOs internos (para no crear más archivos si no quieres) =====

    public record AnalyzeRequest(
            String text,
            List<String> imageUrls,
            String imageBase64DataUrl,
            String audioTranscript,
            String userContext
    ) {}

    public record AiClassification(
            String category,
            String priority,
            Double confidence,
            Boolean possibleFake,
            List<String> reasons,
            List<String> riskFlags,
            String recommendedAction,
            String debugRawResponse
    ) {}
}
