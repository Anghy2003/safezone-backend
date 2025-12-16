package com.ista.springboot.web.app.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ista.springboot.web.app.models.services.FirebaseMessagingService;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionTestController {

    @Autowired
    private FirebaseMessagingService firebaseMessagingService;

    // POST /api/notificaciones/test
    @PostMapping("/test")
    public ResponseEntity<?> enviarNotificacionTest(@RequestBody Map<String, Object> payload) {

        try {
            String token = (String) payload.get("token");
            String titulo = (String) payload.get("titulo");
            String cuerpo = (String) payload.get("cuerpo");

            Map<String, String> data = null;
            if (payload.get("data") instanceof Map) {
                data = (Map<String, String>) payload.get("data");
            }

            String messageId = firebaseMessagingService.enviarNotificacionAToken(
                    token,
                    titulo,
                    cuerpo,
                    data
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Notificación enviada con éxito",
                    "firebaseMessageId", messageId
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Error al enviar notificación",
                    "detalle", e.getMessage()
            ));
        }
    }
}
