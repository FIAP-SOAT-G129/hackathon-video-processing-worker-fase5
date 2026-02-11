package com.fiap_soat.hackaton_video_processing_worker_fase5.listener;

import com.fiap_soat.hackaton_video_processing_worker_fase5.domain.dto.VideoProcessingRequest;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class VideoProcessingListener {

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "video.processing.queue")
    public void receiveMessage(VideoProcessingRequest message) {
        System.out.println("Received message: " + message);
    }
}
