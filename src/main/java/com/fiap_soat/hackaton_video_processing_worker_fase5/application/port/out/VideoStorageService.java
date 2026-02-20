package com.fiap_soat.hackaton_video_processing_worker_fase5.application.port.out;

import com.fiap_soat.hackaton_video_processing_worker_fase5.domain.exception.FileRetrievalException;
import com.fiap_soat.hackaton_video_processing_worker_fase5.domain.exception.FileStorageException;
import com.fiap_soat.hackaton_video_processing_worker_fase5.domain.exception.StoredFileNotFoundException;

import java.io.InputStream;

public interface VideoStorageService {
    String store(InputStream inputStream, String fileName) throws FileStorageException;

    InputStream retrieve(String storagePath) throws StoredFileNotFoundException, FileRetrievalException;
}
