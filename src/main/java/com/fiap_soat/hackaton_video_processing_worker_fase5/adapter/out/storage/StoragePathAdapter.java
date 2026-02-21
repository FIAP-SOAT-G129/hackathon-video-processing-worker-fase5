package com.fiap_soat.hackaton_video_processing_worker_fase5.adapter.out.storage;

import com.fiap.soat.storage.lib.domain.StoragePathResolverPort;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class StoragePathAdapter {

    private final StoragePathResolverPort storagePathResolverPort;

    public StoragePathAdapter(StoragePathResolverPort storagePathResolverPort) {
        this.storagePathResolverPort = storagePathResolverPort;
    }

    public Path getPathForVideo(String fileName) {
        return storagePathResolverPort.resolveVideoPath(fileName);
    }

    public Path getPathForZip(String fileName) {
        return storagePathResolverPort.resolveZipPath(fileName);
    }
}
