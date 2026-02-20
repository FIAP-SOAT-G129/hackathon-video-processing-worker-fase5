package com.fiap_soat.hackaton_video_processing_worker_fase5.producer;

import com.fiap_soat.hackaton_video_processing_worker_fase5.dto.VideoResultMessage;
import com.fiap_soat.hackaton_video_processing_worker_fase5.dto.VideoStatus;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class VideoProcessedProducerTest {

    @Test
    void sendVideoProcessedMessageShouldPublishJsonMessageWithExpectedRouting() {
        RecordingRabbitTemplate rabbitTemplate = new RecordingRabbitTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        VideoProcessedProducer producer = new VideoProcessedProducer(rabbitTemplate, objectMapper);
        setField(producer, "exchange", "video.exchange");
        setField(producer, "resultRoutingKey", "video.processed");

        VideoResultMessage payload = new VideoResultMessage("video-1", "/tmp/video-1.zip", VideoStatus.DONE, null);
        producer.sendVideoProcessedMessage(payload);

        assertEquals("video.exchange", rabbitTemplate.exchange);
        assertEquals("video.processed", rabbitTemplate.routingKey);
        assertNotNull(rabbitTemplate.message);
        assertEquals("application/json", rabbitTemplate.message.getMessageProperties().getContentType());
        assertEquals("utf-8", rabbitTemplate.message.getMessageProperties().getContentEncoding());
        String json = new String(rabbitTemplate.message.getBody());
        assertEquals("{\"videoId\":\"video-1\",\"zipPath\":\"/tmp/video-1.zip\",\"status\":\"DONE\",\"errorMessage\":null}", json);
    }

    private static void setField(Object target, String fieldName, String value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to set test field: " + fieldName, e);
        }
    }

    private static class RecordingRabbitTemplate extends RabbitTemplate {
        private String exchange;
        private String routingKey;
        private Message message;

        @Override
        public void send(String exchange, String routingKey, Message message) {
            this.exchange = exchange;
            this.routingKey = routingKey;
            this.message = message;
        }
    }
}
