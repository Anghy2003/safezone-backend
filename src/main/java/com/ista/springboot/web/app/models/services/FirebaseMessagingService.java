package com.ista.springboot.web.app.models.services;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

/**
 * Servicio para enviar notificaciones push usando Firebase Cloud Messaging (FCM).
 * Depende de la configuración de FirebaseApp definida en FirebaseConfig.
 */
@Service
public class FirebaseMessagingService {

    private final FirebaseMessaging firebaseMessaging;

    // Inyectamos el FirebaseApp que definimos en FirebaseConfig
    public FirebaseMessagingService(FirebaseApp firebaseApp) {
        this.firebaseMessaging = FirebaseMessaging.getInstance(firebaseApp);
    }

    /**
     * Envía una notificación a un dispositivo específico usando su token FCM.
     *
     * @param tokenDestino Token FCM del dispositivo
     * @param titulo       Título de la notificación
     * @param cuerpo       Cuerpo de la notificación
     * @param data         Datos adicionales (clave-valor) opcionales para la app
     * @return ID del mensaje enviado por Firebase
     * @throws Exception si ocurre un error al enviar
     */
    public String enviarNotificacionAToken(String tokenDestino,
                                           String titulo,
                                           String cuerpo,
                                           Map<String, String> data) throws Exception {

        Notification notification = Notification.builder()
                .setTitle(titulo)
                .setBody(cuerpo)
                .build();

        Message.Builder messageBuilder = Message.builder()
                .setToken(tokenDestino)
                .setNotification(notification);

        if (data != null && !data.isEmpty()) {
            messageBuilder.putAllData(data);
        }

        Message message = messageBuilder.build();

        // Devuelve el messageId generado por FCM
        return firebaseMessaging.send(message);
    }

    /**
     * Envía una notificación a todos los dispositivos suscritos a un topic.
     *
     * @param topic  Nombre del topic (sin /topics/, solo el nombre)
     * @param titulo Título de la notificación
     * @param cuerpo Cuerpo de la notificación
     * @param data   Datos adicionales (clave-valor) opcionales
     * @return ID del mensaje enviado por Firebase
     * @throws Exception si ocurre un error al enviar
     */
    public String enviarNotificacionATopic(String topic,
                                           String titulo,
                                           String cuerpo,
                                           Map<String, String> data) throws Exception {

        Notification notification = Notification.builder()
                .setTitle(titulo)
                .setBody(cuerpo)
                .build();

        Message.Builder messageBuilder = Message.builder()
                .setTopic(topic)
                .setNotification(notification);

        if (data != null && !data.isEmpty()) {
            messageBuilder.putAllData(data);
        }

        Message message = messageBuilder.build();

        return firebaseMessaging.send(message);
    }
}
