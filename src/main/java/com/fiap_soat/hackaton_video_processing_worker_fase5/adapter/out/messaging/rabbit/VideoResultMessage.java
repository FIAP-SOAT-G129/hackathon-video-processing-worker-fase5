package com.fiap_soat.hackaton_video_processing_worker_fase5.adapter.out.messaging.rabbit;

import com.fiap_soat.hackaton_video_processing_worker_fase5.domain.model.VideoStatus;

public record VideoResultMessage(String videoId, String zipPath, VideoStatus status, String errorMessage) {
}
