package com.fiap_soat.hackaton_video_processing_worker_fase5.config;

import com.fiap.soat.storage.lib.adapter.StoragePathResolverAdapter;
import com.fiap.soat.storage.lib.config.StorageProperties;
import com.fiap.soat.storage.lib.domain.StoragePathResolverPort;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageLibConfig {

    @Bean
    @ConfigurationProperties(prefix = "storage")
    public StorageProperties storageProperties() {
        return new StorageProperties();
    }

    @Bean
    public StoragePathResolverPort storagePathResolverPort(StorageProperties storageProperties) {
        return new StoragePathResolverAdapter(storageProperties);
    }
}
