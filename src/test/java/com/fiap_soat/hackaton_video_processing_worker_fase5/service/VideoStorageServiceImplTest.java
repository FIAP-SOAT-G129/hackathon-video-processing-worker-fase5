package com.fiap_soat.hackaton_video_processing_worker_fase5.service;

import com.fiap_soat.hackaton_video_processing_worker_fase5.exception.FileRetrievalException;
import com.fiap_soat.hackaton_video_processing_worker_fase5.exception.FileStorageException;
import com.fiap_soat.hackaton_video_processing_worker_fase5.exception.StoredFileNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VideoStorageServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    void storeShouldPersistInputStreamAndReturnStoredPath() throws Exception {
        VideoStorageServiceImpl storageService = new VideoStorageServiceImpl();
        setField(storageService, "storageBaseDir", tempDir.toString());
        byte[] expectedContent = "video-zip-content".getBytes();

        String storedPath = storageService.store(new ByteArrayInputStream(expectedContent), "frames.zip");

        Path filePath = Path.of(storedPath);
        assertNotNull(storedPath);
        assertTrue(storedPath.contains("frames.zip"));
        assertTrue(Files.exists(filePath));
        assertArrayEquals(expectedContent, Files.readAllBytes(filePath));
    }

    @Test
    void retrieveShouldReturnInputStreamWhenFileExists() throws Exception {
        VideoStorageServiceImpl storageService = new VideoStorageServiceImpl();
        setField(storageService, "storageBaseDir", tempDir.toString());
        Path filePath = tempDir.resolve("stored.zip");
        byte[] content = "stored-content".getBytes();
        Files.write(filePath, content);

        try (InputStream retrieved = storageService.retrieve(filePath.toString())) {
            assertArrayEquals(content, retrieved.readAllBytes());
        }
    }

    @Test
    void retrieveShouldThrowWhenFileDoesNotExist() {
        VideoStorageServiceImpl storageService = new VideoStorageServiceImpl();
        setField(storageService, "storageBaseDir", tempDir.toString());

        assertThrows(
            StoredFileNotFoundException.class,
            () -> storageService.retrieve(tempDir.resolve("missing.zip").toString())
        );
    }

    @Test
    void storeShouldThrowFileStorageExceptionWhenBasePathCannotBeCreated() throws Exception {
        VideoStorageServiceImpl storageService = new VideoStorageServiceImpl();
        Path invalidBasePath = tempDir.resolve("not-a-directory");
        Files.writeString(invalidBasePath, "occupied by file");
        setField(storageService, "storageBaseDir", invalidBasePath.toString());

        assertThrows(
            FileStorageException.class,
            () -> storageService.store(new ByteArrayInputStream("x".getBytes()), "frames.zip")
        );
    }

    @Test
    void retrieveShouldThrowFileRetrievalExceptionWhenFileIsNotReadable() throws Exception {
        VideoStorageServiceImpl storageService = new VideoStorageServiceImpl();
        setField(storageService, "storageBaseDir", tempDir.toString());
        Path protectedFile = tempDir.resolve("protected.zip");
        Files.writeString(protectedFile, "cannot read");
        Set<PosixFilePermission> originalPermissions = Files.getPosixFilePermissions(protectedFile);
        Files.setPosixFilePermissions(protectedFile, Set.of());

        try {
            assertThrows(
                FileRetrievalException.class,
                () -> storageService.retrieve(protectedFile.toString())
            );
        } finally {
            Files.setPosixFilePermissions(protectedFile, originalPermissions);
        }
    }

    private static void setField(Object target, String fieldName, String value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to set test field: " + fieldName, e);
        }
    }
}
