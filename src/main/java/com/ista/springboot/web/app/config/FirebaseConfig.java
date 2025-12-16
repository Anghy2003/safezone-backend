package com.ista.springboot.web.app.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp() throws Exception {
        InputStream serviceAccount = getClass().getClassLoader()
                .getResourceAsStream("firebase/safezone-e8bcc-firebase-adminsdk-fbsvc-8161c0ed33.json");

        if (serviceAccount == null) {
            throw new IllegalStateException("No se encontró el JSON en resources/firebase/");
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }
}
