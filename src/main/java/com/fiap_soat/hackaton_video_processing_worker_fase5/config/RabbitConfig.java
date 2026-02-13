package com.fiap_soat.hackaton_video_processing_worker_fase5.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Value("${app.rabbit.processing-queue}")
    private String processingQueue;

    @Value("${app.rabbit.processing-routing-key}")
    private String processingRoutingKey;

    @Value("${app.rabbit.processed-queue}")
    private String processedQueue;

    @Value("${app.rabbit.processed-routing-key}")
    private String processedRoutingKey;

    @Value("${app.rabbit.exchange}")
    private String exchange;

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    Queue processingQueue() {
        return new Queue(processingQueue);
    }

    @Bean
    Binding processingBinding() {
        return BindingBuilder.bind(processingQueue()).to(exchange()).with(processingRoutingKey);
    }

    @Bean
    Queue processedQueue() {
        return new Queue(processedQueue);
    }

    @Bean
    Binding processedBinding() {
        return BindingBuilder.bind(processedQueue()).to(exchange()).with(processedRoutingKey);
    }
}
