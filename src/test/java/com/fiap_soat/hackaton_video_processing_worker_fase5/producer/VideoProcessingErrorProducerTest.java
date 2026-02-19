package com.fiap_soat.hackaton_video_processing_worker_fase5.producer;

import com.fiap_soat.hackaton_video_processing_worker_fase5.dto.VideoStatus;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class VideoProcessingErrorProducerTest {

    @Test
    void sendErrorShouldPublishJsonMessageWithExpectedRouting() {
        RecordingRabbitTemplate rabbitTemplate = new RecordingRabbitTemplate();
        VideoProcessingErrorProducer producer = new VideoProcessingErrorProducer(rabbitTemplate, new ObjectMapper());
        setField(producer, "exchange", "video.exchange");
        setField(producer, "errorRoutingKey", "video.error");

        producer.sendError(new VideoProcessingError("video-2", VideoStatus.ERROR, "invalid input"));

        assertEquals("video.exchange", rabbitTemplate.exchange);
        assertEquals("video.error", rabbitTemplate.routingKey);
        assertNotNull(rabbitTemplate.message);
        assertEquals("application/json", rabbitTemplate.message.getMessageProperties().getContentType());
        assertEquals("utf-8", rabbitTemplate.message.getMessageProperties().getContentEncoding());
        String json = new String(rabbitTemplate.message.getBody());
        assertEquals(
            "{\"videoId\":\"video-2\",\"status\":\"ERROR\",\"reason\":\"invalid input\"}",
            json
        );
    }

    @Test
    void sendErrorShouldNotThrowWhenSerializationFails() {
        RecordingRabbitTemplate rabbitTemplate = new RecordingRabbitTemplate();
        ObjectMapper failingMapper = new ObjectMapper() {
            @Override
            public byte[] writeValueAsBytes(Object value) {
                throw new RuntimeException("serialization failed");
            }
        };
        VideoProcessingErrorProducer producer = new VideoProcessingErrorProducer(rabbitTemplate, failingMapper);
        setField(producer, "exchange", "video.exchange");
        setField(producer, "errorRoutingKey", "video.error");

        producer.sendError(new VideoProcessingError("video-3", VideoStatus.ERROR, "reason"));

        assertEquals(0, rabbitTemplate.sendCallCount);
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
        private int sendCallCount;
        private String exchange;
        private String routingKey;
        private Message message;

        @Override
        public void send(String exchange, String routingKey, Message message) {
            sendCallCount++;
            this.exchange = exchange;
            this.routingKey = routingKey;
            this.message = message;
        }
    }
}
