package com.fiap_soat.hackaton_video_processing_worker_fase5.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String PROCESSING_QUEUE = "video.processing.queue";
    public static final String PROCESSING_ROUTING_KEY = "video.processing.request";
    public static final String PROCESSED_QUEUE = "video.processed.queue";
    public static final String PROCESSED_ROUTING_KEY = "video.processed";
    public static final String EXCHANGE_KEY = "video.processing.exchange";

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_KEY);
    }

    @Bean
    Queue processingQueue() {
        return new Queue(PROCESSING_QUEUE);
    }

    @Bean
    Binding processingBinding() {
        return BindingBuilder.bind(processingQueue()).to(exchange()).with(PROCESSING_ROUTING_KEY);
    }

    @Bean
    Queue processedQueue() {
        return new Queue(PROCESSED_QUEUE);
    }

    @Bean
    Binding processedBinding() {
        return BindingBuilder.bind(processedQueue()).to(exchange()).with(PROCESSED_ROUTING_KEY);
    }
}
