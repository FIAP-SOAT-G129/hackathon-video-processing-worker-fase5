package com.fiap_soat.hackaton_video_processing_worker_fase5.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class VideoStorageServiceImpl implements VideoStorageService {

    @Value("${app.storage.local-path:/tmp/videos}")
    private String storageBaseDir;

    @Override
    public String store(InputStream inputStream, String fileName) {
        try {
            Path uploadPath = Paths.get(storageBaseDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String uniqueFileName = UUID.randomUUID() + "_" + fileName;
            Path filePath = uploadPath.resolve(uniqueFileName);

            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            return filePath.toString();
        } catch (IOException e) {
//            throw new StorageException("Could not store file: " + e.getMessage());
            throw new RuntimeException("Could not store file: " + e.getMessage());
        }
    }

    @Override
    public InputStream retrieve(String storagePath) {
        try {
            Path filePath = Paths.get(storagePath);
            if (!Files.exists(filePath)) {
//                throw new StorageException("File not found at path: " + storagePath);
                throw new RuntimeException("File not found at path: " + storagePath);
            }
            return Files.newInputStream(filePath);
        } catch (IOException e) {
//            throw new StorageException("Could not retrieve file: " + e.getMessage());
            throw new RuntimeException("Could not retrieve file: " + e.getMessage());
        }
    }
}
