package com.fiap_soat.hackaton_video_processing_worker_fase5.dto;

public record VideoProcessingError(String videoId, VideoStatus status, String reason) {
}
