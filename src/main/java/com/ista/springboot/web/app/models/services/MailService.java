package com.ista.springboot.web.app.models.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetLink(String to, String link) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Restablecer contraseña - SafeZone");
        msg.setText(
            "Hola,\n\n" +
            "Para restablecer tu contraseña abre este enlace:\n\n" +
            link + "\n\n" +
            "Este enlace expira en 30 minutos.\n" +
            "Si no solicitaste esto, ignora este correo.\n"
        );
        mailSender.send(msg);
    }
}
