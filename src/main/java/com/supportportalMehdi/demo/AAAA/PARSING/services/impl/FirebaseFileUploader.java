package com.supportportalMehdi.demo.AAAA.PARSING.services.impl;
import com.google.cloud.storage.*;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FirebaseFileUploader {

    // Méthode pour télécharger un fichier
    public String uploadFile(Path filePath, String originalFileName) throws IOException {
        // Obtenir le timestamp actuel
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String newFileName = "bankStatements/" + timestamp + "-" + originalFileName;

        // Télécharger le fichier sur Firebase
        BlobId blobId = BlobId.of("myfactpfe.appspot.com", newFileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        Storage storage = StorageOptions.getDefaultInstance().getService();
        storage.create(blobInfo, Files.readAllBytes(filePath));

        return storage.get(blobId).getMediaLink();
    }
}