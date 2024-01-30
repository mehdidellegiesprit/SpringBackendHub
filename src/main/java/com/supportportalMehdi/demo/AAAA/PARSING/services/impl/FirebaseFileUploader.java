package com.supportportalMehdi.demo.AAAA.PARSING.services.impl;
import com.google.cloud.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FirebaseFileUploader {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseFileUploader.class);

    // Méthode pour télécharger un fichier
    public String uploadFile(Path filePath, String originalFileName) throws IOException {
        // Obtenir le timestamp actuel
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String newFileName = "bankStatements/" + timestamp + "-" + originalFileName;

        // Log avant le téléchargement
        logger.info("Tentative de téléchargement du fichier '{}' vers Firebase Storage sous le nom '{}'", originalFileName, newFileName);

        try {
            BlobId blobId = BlobId.of("myfactpfe.appspot.com", newFileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            Storage storage = StorageOptions.getDefaultInstance().getService();
            storage.create(blobInfo, Files.readAllBytes(filePath));

            String mediaLink = storage.get(blobId).getMediaLink();
            logger.info("Fichier téléchargé avec succès. Media link: {}", mediaLink);
            return mediaLink;
        } catch (Exception e) {
            logger.error("Erreur lors du téléchargement du fichier vers Firebase Storage", e);
            throw e;
        }
    }
}
