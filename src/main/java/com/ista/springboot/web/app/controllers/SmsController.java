package com.ista.springboot.web.app.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ista.springboot.web.app.models.services.TwilioSmsService;

@RestController
@RequestMapping("/api/sms")
@CrossOrigin(origins = "*")
public class SmsController {

    private final TwilioSmsService twilioSmsService;

    public SmsController(TwilioSmsService twilioSmsService) {
        this.twilioSmsService = twilioSmsService;
    }

    @PostMapping("/enviar")
    public ResponseEntity<?> enviarSms(@RequestBody Map<String, String> body) {
        try {
            String to = body.get("to");
            String message = body.get("message");

            if (to == null || message == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Faltan parámetros: to o message")
                );
            }

            String sid = twilioSmsService.enviarSms(to, message);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "sid", sid,
                    "message", "SMS enviado correctamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("success", false, "error", e.getMessage())
            );
        }
    }
}
