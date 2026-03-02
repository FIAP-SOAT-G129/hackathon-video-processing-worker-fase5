package com.fiap_soat.hackaton_video_processing_worker_fase5.config;

import com.fiap.soat.storage.VideoStorageService;
import com.fiap.soat.storage.local.LocalVideoStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    public VideoStorageService videoStorageService(
            @Value("${app.storage.local-path:/tmp/zips}") String storageBaseDir) {
        return new LocalVideoStorageService(storageBaseDir);
    }
}
