package com.fiap_soat.hackaton_video_processing_worker_fase5.producer;

import com.fiap_soat.hackaton_video_processing_worker_fase5.dto.VideoProcessedMessage;
import lombok.AllArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class VideoProcessedProducer {

    public static final String EXCHANGE_KEY = "video.processing.exchange";
    public static final String PROCESSED_ROUTING_KEY = "video.processed";

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;


    public void sendVideoProcessedMessage(VideoProcessedMessage videoProcessedMessage) {
        byte[] body = objectMapper.writeValueAsBytes(videoProcessedMessage);

        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setContentEncoding("utf-8");

        Message message = new Message(body, props);


        rabbitTemplate.send(EXCHANGE_KEY, PROCESSED_ROUTING_KEY, message);
        System.out.println("Sent processed message for videoId: " + videoProcessedMessage.videoId());
    }
}
