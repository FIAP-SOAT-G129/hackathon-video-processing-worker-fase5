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
        setField(config, "processedQueue", "video.processed.queue");
        setField(config, "processedRoutingKey", "video.processed");
        setField(config, "errorQueue", "video.error.queue");
        setField(config, "errorRoutingKey", "video.error");

        TopicExchange exchange = config.exchange();
        Queue processingQueue = config.processingQueue();
        Binding processingBinding = config.processingBinding();
        Queue processedQueue = config.processedQueue();
        Binding processedBinding = config.processedBinding();
        Queue errorQueue = config.errorQueue();
        Binding errorBinding = config.errorBinding();

        assertEquals("video.exchange", exchange.getName());

        assertEquals("video.processing.queue", processingQueue.getName());
        assertEquals("video.processing.queue", processingBinding.getDestination());
        assertEquals("video.exchange", processingBinding.getExchange());
        assertEquals("video.processing", processingBinding.getRoutingKey());

        assertEquals("video.processed.queue", processedQueue.getName());
        assertEquals("video.processed.queue", processedBinding.getDestination());
        assertEquals("video.exchange", processedBinding.getExchange());
        assertEquals("video.processed", processedBinding.getRoutingKey());

        assertEquals("video.error.queue", errorQueue.getName());
        assertEquals("video.error.queue", errorBinding.getDestination());
        assertEquals("video.exchange", errorBinding.getExchange());
        assertEquals("video.error", errorBinding.getRoutingKey());
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
