package com.senibo.notificationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    /**
     * Configures the Error Handler for all Kafka Listeners.
     * Logic:
     * 1. Try to process the message.
     * 2. If it fails, wait 1 second (1000ms).
     * 3. Retry up to 2 times (Total 3 attempts).
     * 4. If it still fails, send it to the Dead Letter Topic (DLT).
     */
    @Bean
    public CommonErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
        // The "Recoverer" defines what to do when we give up. 
        // DeadLetterPublishingRecoverer sends the failed message to a topic named: original-topic.DLT
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);

        // The "BackOff" defines how long to wait between retries.
        // 1000ms = 1 second. 2 retries.
        FixedBackOff backOff = new FixedBackOff(1000L, 2);

        return new DefaultErrorHandler(recoverer, backOff);
    }
}