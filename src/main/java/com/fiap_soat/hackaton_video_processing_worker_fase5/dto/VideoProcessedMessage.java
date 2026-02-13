package com.fiap_soat.hackaton_video_processing_worker_fase5.dto;

public record VideoProcessedMessage(String videoId, String outputVideoPath, VideoStatus status) {
}
