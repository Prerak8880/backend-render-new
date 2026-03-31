package com.example.demo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    @Bean
    public Firestore firestore() throws Exception {
        InputStream serviceAccount;
        
        // Try to read from environment variable first (production on Render)
        String firebaseCredsBase64 = System.getenv("FIREBASE_CREDENTIALS_BASE64");
        
        if (firebaseCredsBase64 != null && !firebaseCredsBase64.isEmpty()) {
            // Production: decode from environment variable
            byte[] decodedBytes = Base64.getDecoder().decode(firebaseCredsBase64);
            serviceAccount = new ByteArrayInputStream(decodedBytes);
            System.out.println("✅ Firebase initialized with environment variable");
        } else {
            // Development: read from file
            serviceAccount = getClass().getClassLoader().getResourceAsStream("ServiceAccountKey.json");
            if (serviceAccount == null) {
                throw new RuntimeException("ServiceAccountKey.json not found in resources!");
            }
            System.out.println("✅ Firebase initialized with local file");
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        return FirestoreClient.getFirestore();
    }
}