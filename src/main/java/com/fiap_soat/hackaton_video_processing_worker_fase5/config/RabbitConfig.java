package com.fiap_soat.hackaton_video_processing_worker_fase5.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
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

    @Value("${app.rabbit.exchange}")
    private String exchange;

    @Value("${app.rabbit.processing-dlq}")
    private String dlq = "video.processing.dlq";

    @Value("${app.rabbit.result-queue}")
    private String resultQueue = "video.processing.result.queue";

    @Value("${app.rabbit.result-routing-key}")
    private String resultRoutingKey = "video.processing.result";

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue processingQueue() {
        return QueueBuilder.durable(processingQueue)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", dlq)
            .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(dlq);
    }

    @Bean
    Binding processingBinding() {
        return BindingBuilder.bind(processingQueue()).to(exchange()).with(processingRoutingKey);
    }

    @Bean
    public Queue resultQueue() {
        return new Queue(resultQueue);
    }

    @Bean
    Binding resultBinding() {
        return BindingBuilder.bind(resultQueue()).to(exchange()).with(resultRoutingKey);
    }
}
