package com.ista.springboot.web.app.models.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class FirebaseCloudMessagingService {

    public String sendNotification(String title, String body, String token) throws Exception {

        Message message = Message.builder()
                .setToken(token)
                .setNotification(
                        Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build()
                )
                .build();

        return FirebaseMessaging.getInstance().send(message);
    }
}
