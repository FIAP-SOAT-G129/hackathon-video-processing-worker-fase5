package com.fiap_soat.hackaton_video_processing_worker_fase5.producer;

import com.fiap_soat.hackaton_video_processing_worker_fase5.dto.VideoProcessedMessage;
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
public class VideoProcessedProducer {
    private static final Logger log = LoggerFactory.getLogger(VideoProcessedProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.rabbit.exchange}")
    private String exchange;

    @Value("${app.rabbit.processed-routing-key}")
    private String processedRoutingKey;


    public void sendVideoProcessedMessage(VideoProcessedMessage videoProcessedMessage) {
        byte[] body = objectMapper.writeValueAsBytes(videoProcessedMessage);

        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setContentEncoding("utf-8");

        Message message = new Message(body, props);

        rabbitTemplate.send(exchange, processedRoutingKey, message);
        log.info("Sent processed message for videoId: {}", videoProcessedMessage.videoId());
    }
}
