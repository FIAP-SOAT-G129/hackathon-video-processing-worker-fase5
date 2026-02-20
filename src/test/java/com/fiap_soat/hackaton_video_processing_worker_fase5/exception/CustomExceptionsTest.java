package com.fiap_soat.hackaton_video_processing_worker_fase5.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CustomExceptionsTest {

    @Test
    void ffmpegExecutionExceptionShouldKeepMessage() {
        FfmpegExecutionException exception = new FfmpegExecutionException("ffmpeg failed");
        assertEquals("ffmpeg failed", exception.getMessage());
    }

    @Test
    void inputVideoNotFoundExceptionShouldKeepMessage() {
        InputVideoNotFoundException exception = new InputVideoNotFoundException("not found");
        assertEquals("not found", exception.getMessage());
    }

    @Test
    void noFramesExtractedExceptionShouldKeepMessage() {
        NoFramesExtractedException exception = new NoFramesExtractedException("no frames");
        assertEquals("no frames", exception.getMessage());
    }

    @Test
    void storedFileNotFoundExceptionShouldKeepMessage() {
        StoredFileNotFoundException exception = new StoredFileNotFoundException("missing storage file");
        assertEquals("missing storage file", exception.getMessage());
    }

    @Test
    void fileRetrievalExceptionShouldKeepMessageAndCause() {
        RuntimeException cause = new RuntimeException("retrieve cause");
        FileRetrievalException exception = new FileRetrievalException("retrieve failed", cause);
        assertEquals("retrieve failed", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void fileStorageExceptionShouldKeepMessageAndCause() {
        RuntimeException cause = new RuntimeException("store cause");
        FileStorageException exception = new FileStorageException("store failed", cause);
        assertEquals("store failed", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void frameZipExceptionShouldKeepMessageAndCause() {
        RuntimeException cause = new RuntimeException("zip cause");
        FrameZipException exception = new FrameZipException("zip failed", cause);
        assertEquals("zip failed", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
