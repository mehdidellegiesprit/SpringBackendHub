package com.supportportalMehdi.demo.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class FirebaseInitializer {

    @PostConstruct
    public void initialize() {
        try {
            String firebaseConfigBase64 = System.getenv("FIREBASE_CONFIG");
            byte[] decodedBytes = Base64.getDecoder().decode(firebaseConfigBase64);
            GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(decodedBytes));

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(credentials)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Gérer l'erreur ici
        }
    }

}
