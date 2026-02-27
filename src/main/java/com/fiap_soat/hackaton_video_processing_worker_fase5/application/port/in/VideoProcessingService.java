package com.fiap_soat.hackaton_video_processing_worker_fase5.application.port.in;

import com.fiap_soat.hackaton_video_processing_worker_fase5.adapter.in.dto.VideoProcessingRequest;

public interface VideoProcessingService {
    void processVideo(VideoProcessingRequest videoProcessingRequest);
}
