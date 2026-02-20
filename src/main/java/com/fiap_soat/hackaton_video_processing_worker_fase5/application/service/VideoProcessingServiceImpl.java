package com.fiap_soat.hackaton_video_processing_worker_fase5.application.service;

import com.fiap_soat.hackaton_video_processing_worker_fase5.application.port.in.VideoProcessingService;
import com.fiap_soat.hackaton_video_processing_worker_fase5.application.port.out.VideoStorageService;
import com.fiap_soat.hackaton_video_processing_worker_fase5.adapter.out.messaging.rabbit.VideoResultMessage;
import com.fiap_soat.hackaton_video_processing_worker_fase5.adapter.in.dto.VideoProcessingRequest;
import com.fiap_soat.hackaton_video_processing_worker_fase5.domain.model.VideoStatus;
import com.fiap_soat.hackaton_video_processing_worker_fase5.domain.exception.FfmpegExecutionException;
import com.fiap_soat.hackaton_video_processing_worker_fase5.domain.exception.FrameZipException;
import com.fiap_soat.hackaton_video_processing_worker_fase5.domain.exception.InputVideoNotFoundException;
import com.fiap_soat.hackaton_video_processing_worker_fase5.domain.exception.NoFramesExtractedException;
import com.fiap_soat.hackaton_video_processing_worker_fase5.adapter.out.messaging.rabbit.VideoProcessedProducer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class VideoProcessingServiceImpl implements VideoProcessingService {
    private static final Logger log = LoggerFactory.getLogger(VideoProcessingServiceImpl.class);

    private final VideoStorageService videoStorageService;
    private final VideoProcessedProducer videoProcessedProducer;

    @Value("${app.video.frames-fps:30}")
    private int framesFps;

    @Value("${app.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    @Value("${app.storage.base-url:}")
    private String storageBaseUrl;

    @Override
    public void processVideo(VideoProcessingRequest videoProcessingRequest) {
        Path tempDir = null;
        try {
            Path inputPath = Path.of(videoProcessingRequest.inputVideoPath());
            if (!Files.exists(inputPath)) {
                throw new InputVideoNotFoundException("Input video not found: " + inputPath);
            }

            tempDir = Files.createTempDirectory("video-frames-" + videoProcessingRequest.videoId() + "-");
            Path framesDir = tempDir.resolve("frames");
            Files.createDirectories(framesDir);
            Path zipPath = tempDir.resolve("frames.zip");

            extractFrames(inputPath, framesDir);

            long frameCount = countFrames(framesDir);
            if (frameCount == 0) {
                throw new NoFramesExtractedException("No frames extracted from video: " + inputPath);
            }

            zipDirectory(framesDir, zipPath);

            String storedPath;
            try (InputStream zipStream = Files.newInputStream(zipPath)) {
                String zipFileName = videoProcessingRequest.videoId() + "_frames.zip";
                storedPath = videoStorageService.store(zipStream, zipFileName);
            }

            String outputUrl = buildOutputUrl(storedPath);
            sendResultDoneMessage(videoProcessingRequest, outputUrl);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            sendErrorMessage(videoProcessingRequest, e);
        } finally {
            if (tempDir != null) {
                deleteRecursively(tempDir);
            }
        }
    }

    private void extractFrames(Path inputPath, Path framesDir) throws IOException, InterruptedException, FfmpegExecutionException {
        String outputPattern = framesDir.resolve("frame_%06d.jpg").toString();
        ProcessBuilder processBuilder = new ProcessBuilder(
            ffmpegPath,
            "-i", inputPath.toString(),
            "-vf", "fps=" + framesFps,
            outputPattern
        );
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        String output = readProcessOutput(process.getInputStream());
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new FfmpegExecutionException("FFmpeg failed (exit " + exitCode + "): " + output);
        }
    }

    private long countFrames(Path framesDir) throws IOException {
        try (Stream<Path> files = Files.list(framesDir)) {
            return files.filter(path -> path.getFileName().toString().endsWith(".jpg")).count();
        }
    }

    private void zipDirectory(Path sourceDir, Path zipPath) throws IOException, FrameZipException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            List<Path> framePaths;
            try (Stream<Path> paths = Files.list(sourceDir)) {
                framePaths = paths.filter(Files::isRegularFile).sorted().toList();
            }

            for (Path path : framePaths) {
                ZipEntry entry = new ZipEntry(sourceDir.relativize(path).toString());
                try (InputStream inputStream = Files.newInputStream(path)) {
                    zipOutputStream.putNextEntry(entry);
                    inputStream.transferTo(zipOutputStream);
                    zipOutputStream.closeEntry();
                } catch (IOException e) {
                    throw new FrameZipException("Failed to zip frame: " + path, e);
                }
            }
        }
    }

    private String buildOutputUrl(String storedPath) {
        if (storageBaseUrl == null || storageBaseUrl.isBlank()) {
            return storedPath;
        }
        String fileName = Path.of(storedPath).getFileName().toString();
        String baseUrl = storageBaseUrl.endsWith("/") ? storageBaseUrl.substring(0, storageBaseUrl.length() - 1) : storageBaseUrl;
        return baseUrl + "/" + fileName;
    }

    private String readProcessOutput(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (builder.isEmpty()) {
                    builder.append(System.lineSeparator());
                }
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private void deleteRecursively(Path root) {
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.warn("Failed to cleanup temp dir: {}", root, e);
        }
    }

    private void sendErrorMessage(VideoProcessingRequest request, Exception exception) {
        String reason = exception.getMessage();
        if (reason == null || reason.isBlank()) {
            reason = exception.getClass().getSimpleName();
        }
        VideoResultMessage error = new VideoResultMessage(
            request.videoId(),
            null,
            VideoStatus.ERROR,
            reason
        );
        videoProcessedProducer.sendVideoProcessedMessage(error);
    }

    private void sendResultDoneMessage(VideoProcessingRequest request, String outputUrl) {
        VideoResultMessage message = new VideoResultMessage(
            request.videoId(),
            outputUrl,
            VideoStatus.DONE,
            null
        );
        videoProcessedProducer.sendVideoProcessedMessage(message);
    }
}
