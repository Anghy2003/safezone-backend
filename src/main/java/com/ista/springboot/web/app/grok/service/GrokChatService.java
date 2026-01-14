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

    /**
     * Chat principal para Isis Ayuda.
     * - Mantiene conversación fluida usando history.
     * - Evita saludos repetitivos y muletillas.
     * - Ajusta el "estilo institucional" según el tipo de emergencia (Bomberos / Policía / Cruz Roja / ECU-911).
     */
    public String chatIsis(String emergencyType, String userMessage, List<ChatTurn> history) {
        if (!StringUtils.hasText(userMessage)) {
            throw new IllegalArgumentException("userMessage es obligatorio");
        }

        // Detecta si es el primer turno (para permitir saludo SOLO al inicio si aplica)
        boolean isFirstTurn = (history == null || history.isEmpty());

        // Decide la institución guía para dar consejos con enfoque coherente
        String guide = inferInstitutionGuide(emergencyType, userMessage);

        // Prompt base: continuidad conversacional + límites + enfoque local
        String systemPrompt = buildSystemPrompt(isFirstTurn);

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // Contexto institucional adicional: obliga a "sonar" como la institución guía sin inventar protocolos
        messages.add(Map.of("role", "system", "content", buildInstitutionContext(guide)));

        // Historia (opcional) -> es CLAVE para conversación fluida
        if (history != null) {
            for (ChatTurn t : history) {
                if (t != null && StringUtils.hasText(t.role()) && StringUtils.hasText(t.content())) {
                    // Normaliza roles a los aceptados por chat completions
                    String role = normalizeRole(t.role());
                    if (role != null) {
                        messages.add(Map.of("role", role, "content", t.content()));
                    }
                }
            }
        }

        // Mensaje actual: inyecta contexto compacto y útil (sin volverlo largo)
        String finalUser = buildFinalUser(emergencyType, userMessage);
        messages.add(Map.of("role", "user", "content", finalUser));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", chatModel);
        payload.put("temperature", 0.4); // un poco menor para consistencia conversacional
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
            return content.trim();
        } catch (Exception e) {
            throw new RuntimeException("Error parseando respuesta de xAI: " + e.getMessage(), e);
        }
    }

    /**
     * Prompt principal: maximiza continuidad y evita "reinicios" (saludos repetidos).
     * isFirstTurn permite saludo SOLO si es primer turno o si el usuario saluda.
     */
    private String buildSystemPrompt(boolean isFirstTurn) {
        // Nota: no podemos "garantizar 100%" porque depende del modelo, pero esto maximiza mucho la continuidad.
        return """
Eres Isis Ayuda, la asistente virtual de SafeZone.

Idioma y tono:
Responde siempre en español, con un tono empático, cercano y calmado. Sé directa y útil, sin dramatizar.

Conversación fluida (prioridad máxima):
Mantén continuidad con el contexto previo y con el historial del chat.
No reinicies la conversación.
No saludes en cada mensaje.
Solo puedes saludar si (a) es el primer turno o (b) el usuario te saluda explícitamente.
Evita muletillas repetidas como “no te preocupes”, “tranquilo/a”, “todo va a estar bien”; si necesitas contener, varía el lenguaje y úsalo con moderación.
Responde como chat real: 1–2 párrafos breves.
Si falta información, haz 1 pregunta corta (máximo 2) para aclarar lo esencial.

Contexto local:
Tus recomendaciones deben ser pertinentes para Cuenca/Azuay cuando aplique, considerando particularidades del entorno.
No finjas ser personal oficial de ninguna institución.

Límites y seguridad:
No des diagnósticos médicos ni tratamientos.
No des instrucciones técnicas detalladas (por ejemplo, maniobras médicas específicas, procedimientos complejos).
No incites a confrontaciones ni acciones agresivas.
No inventes números oficiales, protocolos internos o procedimientos exactos.

Urgencia:
Si hay peligro inmediato, prioriza seguridad personal.
Recuérdale que puede presionar tres veces el botón de volumen para enviar una alerta SafeZone.
Sugiere contactar servicios de emergencia o a una persona de confianza.

Formato:
No uses listas, viñetas ni pasos enumerados.
""";
    }

    /**
     * Contexto institucional: hace que la respuesta "suene" como consejos típicos de la institución guía.
     * Sin inventar protocolos internos ni detalles técnicos.
     */
    private String buildInstitutionContext(String guide) {
        return ("""
Contexto de orientación institucional:
Para este caso, tu voz guía debe alinearse al estilo de: %s.

Reglas por institución (sin listas en la respuesta final):
- Si es BOMBEROS: enfócate en prevención de riesgos, evacuación segura, evitar humo/fuego, alejarse de fuentes de peligro, y pedir apoyo inmediato.
- Si es CRUZ_ROJA: enfócate en contención emocional y recomendaciones generales de seguridad y cuidado básico NO técnico, sin diagnóstico ni tratamiento.
- Si es POLICIA: enfócate en autoprotección, evitar confrontación, moverse a un lugar seguro, preservar evidencia de forma general (sin instrucciones operativas), y pedir ayuda.
- Si es ECU_911: enfócate en coordinación general, ubicación, datos mínimos del incidente y canalización segura.

Recuerda:
No saludes si ya vienes conversando.
No uses listas en tu respuesta.
""").formatted(guide);
    }

    /**
     * Construye el mensaje final del usuario, compacto y consistente.
     */
    private String buildFinalUser(String emergencyType, String userMessage) {
        if (!StringUtils.hasText(emergencyType)) return userMessage.trim();

        // Contexto mínimo para que el modelo no pierda el hilo
        return ("Tipo de emergencia: " + emergencyType.trim() + "\n" +
                "Mensaje del usuario: " + userMessage.trim());
    }

    /**
     * Heurística simple para asignar institución guía.
     * Ajusta keywords según tus emergencyType reales.
     */
    private String inferInstitutionGuide(String emergencyType, String userMessage) {
        String t = (emergencyType == null ? "" : emergencyType).toLowerCase(Locale.ROOT);
        String m = (userMessage == null ? "" : userMessage).toLowerCase(Locale.ROOT);

        // BOMBEROS: incendios, gas, humo, rescate, desastres
        if (containsAny(t, "incend", "fuego", "gas", "explos", "rescate", "sismo", "inund", "derrum", "choque")
                || containsAny(m, "humo", "llamas", "olor a gas", "se está quemando", "explosión", "atrapado", "derrumbe")) {
            return "BOMBEROS";
        }

        // POLICIA: robos, amenazas, violencia, persecución, armas
        if (containsAny(t, "asalto", "robo", "amenaza", "violencia", "acoso", "secuestro", "intruso", "agresión")
                || containsAny(m, "arma", "me siguen", "me amenazaron", "me quieren robar", "me están persiguiendo", "golpe")) {
            return "POLICIA";
        }

        // CRUZ ROJA: salud/lesiones/accidente con heridos (sin entrar en maniobras)
        if (containsAny(t, "salud", "herida", "sangr", "desmayo", "accidente", "dolor", "convuls")
                || containsAny(m, "sangre", "me desmayé", "no responde", "me duele", "herida", "mareo")) {
            return "CRUZ_ROJA";
        }

        // Por defecto: coordinación
        return "ECU_911";
    }

    /**
     * Normaliza roles para evitar enviar roles inválidos.
     */
    private String normalizeRole(String roleRaw) {
        if (!StringUtils.hasText(roleRaw)) return null;
        String r = roleRaw.trim().toLowerCase(Locale.ROOT);

        // Acepta roles típicos
        if (r.equals("system") || r.equals("user") || r.equals("assistant")) return r;

        // Compatibilidad si guardas roles con nombres distintos
        if (r.equals("usuario")) return "user";
        if (r.equals("asis") || r.equals("assistant_message") || r.equals("bot")) return "assistant";

        // Si viene algo raro, lo omitimos para no romper el request
        return null;
    }

    private boolean containsAny(String text, String... parts) {
        if (!StringUtils.hasText(text)) return false;
        for (String p : parts) {
            if (p != null && !p.isBlank() && text.contains(p.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    public record ChatTurn(String role, String content) {}
}
