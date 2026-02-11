package com.fiap_soat.hackaton_video_processing_worker_fase5.service;

import java.io.InputStream;

public interface VideoStorageService {
    String store(InputStream inputStream, String fileName);
    InputStream retrieve(String storagePath);
}
