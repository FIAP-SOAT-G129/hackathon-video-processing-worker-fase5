package com.fiap_soat.hackaton_video_processing_worker_fase5.service;

import com.fiap_soat.hackaton_video_processing_worker_fase5.domain.dto.VideoProcessingRequest;

public interface VideoProcessingService {
    void processVideo(VideoProcessingRequest videoProcessingRequest);
}
