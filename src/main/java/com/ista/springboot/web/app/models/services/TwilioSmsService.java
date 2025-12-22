package com.ista.springboot.web.app.models.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class TwilioSmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromPhone;

    private boolean initialized = false;

    // =====================================================
    // Inicializa Twilio una sola vez
    // =====================================================
    private synchronized void initTwilio() {
        if (!initialized) {
            Twilio.init(accountSid, authToken);
            initialized = true;
        }
    }

    // =====================================================
    // Enviar SMS
    // =====================================================
    public String enviarSms(String to, String mensaje) {
        initTwilio();

        Message sms = Message.creator(
                new PhoneNumber(to),          // +5939xxxxxxx
                new PhoneNumber(fromPhone),   // número Twilio
                mensaje
        ).create();

        return sms.getSid(); // útil para logs/auditoría
    }
}
