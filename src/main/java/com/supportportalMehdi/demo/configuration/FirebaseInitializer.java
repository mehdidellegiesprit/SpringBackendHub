package com.supportportalMehdi.demo.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FirebaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseInitializer.class);

    @PostConstruct
    public void initialize() {
        try {
            // Charge le fichier de configuration Firebase depuis le classpath
            InputStream firebaseConfigInputStream = getClass().getResourceAsStream("/firebase-config.json");
            if (firebaseConfigInputStream == null) {
                throw new IOException("Fichier de configuration Firebase introuvable dans le classpath");
            }

            // Utilise les informations de configuration pour initialiser Firebase
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(firebaseConfigInputStream))
                    .build();

            // Initialise l'application Firebase si elle n'est pas déjà initialisée
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("Firebase initialisé avec succès");
            }
        } catch (IOException e) {
            logger.error("Erreur lors de l'initialisation de Firebase", e);
        }
    }
}
