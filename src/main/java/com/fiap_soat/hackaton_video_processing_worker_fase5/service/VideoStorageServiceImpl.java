package com.fiap_soat.hackaton_video_processing_worker_fase5.service;

import com.fiap_soat.hackaton_video_processing_worker_fase5.exception.FileRetrievalException;
import com.fiap_soat.hackaton_video_processing_worker_fase5.exception.FileStorageException;
import com.fiap_soat.hackaton_video_processing_worker_fase5.exception.StoredFileNotFoundException;
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
    public String store(InputStream inputStream, String fileName) throws FileStorageException {
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
            throw new FileStorageException("Could not store file: " + fileName, e);
        }
    }

    @Override
    public InputStream retrieve(String storagePath) throws StoredFileNotFoundException, FileRetrievalException {
        try {
            Path filePath = Paths.get(storagePath);
            if (!Files.exists(filePath)) {
                throw new StoredFileNotFoundException("File not found at path: " + storagePath);
            }
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new FileRetrievalException("Could not retrieve file: " + storagePath, e);
        }
    }
}
