package com.fiap_soat.hackaton_video_processing_worker_fase5.producer;

import com.fiap_soat.hackaton_video_processing_worker_fase5.dto.VideoProcessingError;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class VideoProcessingErrorProducer {
    private static final Logger log = LoggerFactory.getLogger(VideoProcessingErrorProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.rabbit.exchange}")
    private String exchange;

    @Value("${app.rabbit.error-routing-key}")
    private String errorRoutingKey;

    public void sendError(VideoProcessingError error) {
        try {
            byte[] body = objectMapper.writeValueAsBytes(error);

            MessageProperties props = new MessageProperties();
            props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            props.setContentEncoding("utf-8");

            Message message = new Message(body, props);
            rabbitTemplate.send(exchange, errorRoutingKey, message);
        } catch (Exception e) {
            log.error("Failed to send error message for videoId: {}", error.videoId(), e);
        }
    }
}
