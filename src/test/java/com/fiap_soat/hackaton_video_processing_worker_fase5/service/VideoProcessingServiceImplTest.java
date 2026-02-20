package com.fiap_soat.hackaton_video_processing_worker_fase5.service;

import com.fiap_soat.hackaton_video_processing_worker_fase5.dto.VideoResultMessage;
import com.fiap_soat.hackaton_video_processing_worker_fase5.dto.VideoProcessingRequest;
import com.fiap_soat.hackaton_video_processing_worker_fase5.dto.VideoStatus;
import com.fiap_soat.hackaton_video_processing_worker_fase5.exception.FileStorageException;
import com.fiap_soat.hackaton_video_processing_worker_fase5.producer.VideoProcessedProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VideoProcessingServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    void processVideoShouldSendErrorWhenInputVideoDoesNotExist() {
        StubVideoStorageService videoStorageService = new StubVideoStorageService();
        RecordingVideoProcessedProducer videoProcessedProducer = new RecordingVideoProcessedProducer();
        VideoProcessingServiceImpl service = createService(videoStorageService, videoProcessedProducer);
        VideoProcessingRequest request = new VideoProcessingRequest("video-1", "user-1", tempDir.resolve("missing.mp4").toString());

        service.processVideo(request);

        VideoResultMessage message = videoProcessedProducer.message;
        assertNotNull(message);
        assertEquals(0, videoStorageService.storeCallCount);
        assertEquals(1, videoProcessedProducer.callCount);
        assertEquals("video-1", message.videoId());
        assertEquals(VideoStatus.ERROR, message.status());
        assertTrue(message.errorMessage().contains("Input video not found"));
    }

    @Test
    void processVideoShouldSendResultDoneMessageWhenFramesAreExtractedAndStored() throws Exception {
        StubVideoStorageService videoStorageService = new StubVideoStorageService();
        RecordingVideoProcessedProducer videoProcessedProducer = new RecordingVideoProcessedProducer();
        VideoProcessingServiceImpl service = createService(videoStorageService, videoProcessedProducer);
        Path inputVideo = tempDir.resolve("input.mp4");
        Files.write(inputVideo, new byte[]{1, 2, 3});
        Path ffmpegScript = createFakeFfmpegScript(tempDir);

        setField(service, "ffmpegPath", ffmpegScript.toString());
        setField(service, "storageBaseUrl", "https://files.example.com/output");
        videoStorageService.storedPathToReturn = "/tmp/storage/abc_frames.zip";

        VideoProcessingRequest request = new VideoProcessingRequest("abc", "user-1", inputVideo.toString());
        service.processVideo(request);

        assertEquals(1, videoStorageService.storeCallCount);
        assertEquals(1, videoProcessedProducer.callCount);

        VideoResultMessage message = videoProcessedProducer.message;
        assertNotNull(message);
        assertEquals("abc", message.videoId());
        assertEquals(VideoStatus.DONE, message.status());
        assertEquals("https://files.example.com/output/abc_frames.zip", message.zipPath());
    }

    @Test
    void processVideoShouldSendErrorWhenFfmpegFails() throws Exception {
        StubVideoStorageService videoStorageService = new StubVideoStorageService();
        RecordingVideoProcessedProducer videoProcessedProducer = new RecordingVideoProcessedProducer();
        VideoProcessingServiceImpl service = createService(videoStorageService, videoProcessedProducer);
        Path inputVideo = tempDir.resolve("input-failed.mp4");
        Files.write(inputVideo, new byte[]{1, 2, 3});
        Path ffmpegScript = createFailingFfmpegScript(tempDir);
        setField(service, "ffmpegPath", ffmpegScript.toString());

        VideoProcessingRequest request = new VideoProcessingRequest("video-failed", "user-1", inputVideo.toString());
        service.processVideo(request);

        assertEquals(0, videoStorageService.storeCallCount);
        assertEquals(1, videoProcessedProducer.callCount);
        assertNotNull(videoProcessedProducer.message);
        assertEquals(VideoStatus.ERROR, videoProcessedProducer.message.status());
        assertTrue(videoProcessedProducer.message.errorMessage().contains("FFmpeg failed (exit 1)"));
    }

    @Test
    void processVideoShouldSendErrorWhenNoFramesAreExtracted() throws Exception {
        StubVideoStorageService videoStorageService = new StubVideoStorageService();
        RecordingVideoProcessedProducer videoProcessedProducer = new RecordingVideoProcessedProducer();
        VideoProcessingServiceImpl service = createService(videoStorageService, videoProcessedProducer);
        Path inputVideo = tempDir.resolve("input-empty.mp4");
        Files.write(inputVideo, new byte[]{1, 2, 3});
        Path ffmpegScript = createNoFrameFfmpegScript(tempDir);
        setField(service, "ffmpegPath", ffmpegScript.toString());

        VideoProcessingRequest request = new VideoProcessingRequest("video-empty", "user-1", inputVideo.toString());
        service.processVideo(request);

        assertEquals(0, videoStorageService.storeCallCount);
        assertEquals(1, videoProcessedProducer.callCount);
        assertNotNull(videoProcessedProducer.message);
        assertEquals(VideoStatus.ERROR, videoProcessedProducer.message.status());
        assertTrue(videoProcessedProducer.message.errorMessage().contains("No frames extracted from video"));
    }

    @Test
    void processVideoShouldUseStoredPathWhenStorageBaseUrlIsBlank() throws Exception {
        StubVideoStorageService videoStorageService = new StubVideoStorageService();
        RecordingVideoProcessedProducer videoProcessedProducer = new RecordingVideoProcessedProducer();
        VideoProcessingServiceImpl service = createService(videoStorageService, videoProcessedProducer);
        Path inputVideo = tempDir.resolve("input-blank-base.mp4");
        Files.write(inputVideo, new byte[]{1, 2, 3});
        Path ffmpegScript = createFakeFfmpegScript(tempDir);
        setField(service, "ffmpegPath", ffmpegScript.toString());
        setField(service, "storageBaseUrl", "  ");
        videoStorageService.storedPathToReturn = "/tmp/storage/raw_frames.zip";

        VideoProcessingRequest request = new VideoProcessingRequest("raw", "user-1", inputVideo.toString());
        service.processVideo(request);

        assertEquals(1, videoProcessedProducer.callCount);
        assertNotNull(videoProcessedProducer.message);
        assertEquals("/tmp/storage/raw_frames.zip", videoProcessedProducer.message.zipPath());
    }

    @Test
    void processVideoShouldFallbackToExceptionTypeWhenReasonIsBlank() throws Exception {
        StubVideoStorageService videoStorageService = new StubVideoStorageService();
        videoStorageService.throwStorageException = true;
        RecordingVideoProcessedProducer videoProcessedProducer = new RecordingVideoProcessedProducer();
        VideoProcessingServiceImpl service = createService(videoStorageService, videoProcessedProducer);
        Path inputVideo = tempDir.resolve("input-storage-fail.mp4");
        Files.write(inputVideo, new byte[]{1, 2, 3});
        Path ffmpegScript = createFakeFfmpegScript(tempDir);
        setField(service, "ffmpegPath", ffmpegScript.toString());

        VideoProcessingRequest request = new VideoProcessingRequest("storage-fail", "user-1", inputVideo.toString());
        service.processVideo(request);

        assertEquals(1, videoStorageService.storeCallCount);
        assertEquals(1, videoProcessedProducer.callCount);
        assertNotNull(videoProcessedProducer.message);
        assertEquals(VideoStatus.ERROR, videoProcessedProducer.message.status());
        assertEquals("FileStorageException", videoProcessedProducer.message.errorMessage());
    }

    private VideoProcessingServiceImpl createService(
        VideoStorageService videoStorageService,
        VideoProcessedProducer videoProcessedProducer
    ) {
        VideoProcessingServiceImpl service = new VideoProcessingServiceImpl(
            videoStorageService,
            videoProcessedProducer
        );
        setField(service, "framesFps", 30);
        setField(service, "storageBaseUrl", "");
        return service;
    }

    private static Path createFakeFfmpegScript(Path rootDir) throws IOException {
        Path script = rootDir.resolve("fake-ffmpeg.sh");
        Files.write(
            script,
            List.of(
                "#!/bin/sh",
                "output_pattern=\"$5\"",
                "output_file=$(printf \"%s\" \"$output_pattern\" | sed 's/%06d/000001/')",
                "mkdir -p \"$(dirname \"$output_file\")\"",
                "echo frame > \"$output_file\"",
                "exit 0"
            )
        );
        boolean executable = script.toFile().setExecutable(true);
        if (!executable) {
            throw new IllegalStateException("Failed to set fake ffmpeg script as executable");
        }
        return script;
    }

    private static Path createNoFrameFfmpegScript(Path rootDir) throws IOException {
        Path script = rootDir.resolve("fake-ffmpeg-no-frames.sh");
        Files.write(
            script,
            List.of(
                "#!/bin/sh",
                "exit 0"
            )
        );
        boolean executable = script.toFile().setExecutable(true);
        if (!executable) {
            throw new IllegalStateException("Failed to set fake ffmpeg script as executable");
        }
        return script;
    }

    private static Path createFailingFfmpegScript(Path rootDir) throws IOException {
        Path script = rootDir.resolve("fake-ffmpeg-fail.sh");
        Files.write(
            script,
            List.of(
                "#!/bin/sh",
                "echo \"ffmpeg simulated failure\"",
                "exit 1"
            )
        );
        boolean executable = script.toFile().setExecutable(true);
        if (!executable) {
            throw new IllegalStateException("Failed to set fake ffmpeg script as executable");
        }
        return script;
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to set test field: " + fieldName, e);
        }
    }

    private static class StubVideoStorageService implements VideoStorageService {
        private int storeCallCount;
        private String storedPathToReturn = "/tmp/stored/default_frames.zip";
        private boolean throwStorageException;

        @Override
        public String store(InputStream inputStream, String fileName) throws FileStorageException {
            storeCallCount++;
            if (throwStorageException) {
                throw new FileStorageException(" ", new IOException("simulated storage failure"));
            }
            return storedPathToReturn;
        }

        @Override
        public InputStream retrieve(String storagePath) {
            throw new UnsupportedOperationException("retrieve is not used in these tests");
        }
    }

    private static class RecordingVideoProcessedProducer extends VideoProcessedProducer {
        private int callCount;
        private VideoResultMessage message;

        private RecordingVideoProcessedProducer() {
            super(null, null);
        }

        @Override
        public void sendVideoProcessedMessage(VideoResultMessage videoResultMessage) {
            callCount++;
            message = videoResultMessage;
        }
    }
}
