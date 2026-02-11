package com.fiap_soat.hackaton_video_processing_worker_fase5.listener;

import com.fiap_soat.hackaton_video_processing_worker_fase5.dto.VideoProcessingRequest;
import com.fiap_soat.hackaton_video_processing_worker_fase5.service.VideoProcessingService;
import lombok.AllArgsConstructor;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class VideoProcessingListener {

    private final ObjectMapper objectMapper;
    private final VideoProcessingService videoProcessingService;

    @RabbitListener(queues = "video.processing.queue")
    public void receiveMessage(Message message) {
        try {
            VideoProcessingRequest request = objectMapper.readValue(
                message.getBody(),
                VideoProcessingRequest.class
            );

            videoProcessingService.processVideo(request);
        } catch (Exception e) {
            throw new AmqpRejectAndDontRequeueException("Invalid message payload", e);
        }
    }
}
