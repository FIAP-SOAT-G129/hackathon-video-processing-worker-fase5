package com.fiap_soat.hackaton_video_processing_worker_fase5.service;

import com.fiap_soat.hackaton_video_processing_worker_fase5.dto.VideoProcessingRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class VideoProcessingServiceImpl implements VideoProcessingService {

    @Override
    public void processVideo(VideoProcessingRequest videoProcessingRequest) {
        System.out.println(videoProcessingRequest);
    }
}
