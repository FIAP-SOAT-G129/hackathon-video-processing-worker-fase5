package com.fiap_soat.hackaton_video_processing_worker_fase5.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RabbitConfigTest {

    @Test
    void shouldCreateExchangeQueuesAndBindingsWithConfiguredValues() {
        RabbitConfig config = new RabbitConfig();
        setField(config, "exchange", "video.exchange");
        setField(config, "processingQueue", "video.processing.queue");
        setField(config, "processingRoutingKey", "video.processing");
        setField(config, "dlq", "video.processing.dlq");
        setField(config, "resultQueue", "video.result.queue");
        setField(config, "resultRoutingKey", "video.result");

        TopicExchange exchange = config.exchange();
        Queue processingQueue = config.processingQueue();
        Binding processingBinding = config.processingBinding();
        Queue deadLetterQueue = config.deadLetterQueue();
        Queue resultQueue = config.resultQueue();
        Binding resultBinding = config.resultBinding();

        assertEquals("video.exchange", exchange.getName());

        assertEquals("video.processing.queue", processingQueue.getName());
        assertEquals("video.processing.queue", processingBinding.getDestination());
        assertEquals("video.exchange", processingBinding.getExchange());
        assertEquals("video.processing", processingBinding.getRoutingKey());

        assertEquals("video.processing.dlq", deadLetterQueue.getName());

        assertEquals("video.result.queue", resultQueue.getName());
        assertEquals("video.result.queue", resultBinding.getDestination());
        assertEquals("video.exchange", resultBinding.getExchange());
        assertEquals("video.result", resultBinding.getRoutingKey());
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
}
