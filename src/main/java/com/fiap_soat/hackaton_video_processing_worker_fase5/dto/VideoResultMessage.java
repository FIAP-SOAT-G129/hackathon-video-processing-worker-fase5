package com.fiap_soat.hackaton_video_processing_worker_fase5.dto;

public record VideoResultMessage(String videoId, String zipPath, VideoStatus status, String errorMessage) {
}
