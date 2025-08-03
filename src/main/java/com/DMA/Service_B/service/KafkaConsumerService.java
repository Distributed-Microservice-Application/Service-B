package com.DMA.Service_B.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.DMA.Service_B.metrics.PrometheusMetrics;
import com.DMA.Service_B.model.KafkaMessage;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class KafkaConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private SummationService summationService;
    
    @Autowired
    private PrometheusMetrics metrics;

    @KafkaListener(topics = "${kafka.topic.summation}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessage(KafkaMessage message) {
        if (message == null) {
            logger.warn("Received null Kafka message, skipping processing");
            return;
        }
        
        logger.info("Processing Kafka message - sum: {}", message.getSum());
        
        try {
            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime messageTimestamp = message.getTimestamp();

            if (messageTimestamp != null) {
                long latencyMs = ChronoUnit.MILLIS.between(messageTimestamp, now);
                logger.info("Received message from Kafka - sum: {}, timestamp: {}, latency: {} ms", message.getSum(), messageTimestamp, latencyMs);
                
                // Use a meaningful queue name instead of method-url combination
                metrics.recordQueueLatencyWithQueueName(latencyMs, "user-events", "GET", "/api/summation");
            } else {
                logger.info("Received message from Kafka - sum: {}, timestamp: null", message.getSum());
                metrics.recordQueueLatencyWithQueueName(0, "user-events", "GET", "/api/summation");
            }
            
            // Process the summation update
            summationService.updateSummationValue(String.valueOf(message.getSum()));
            logger.info("Successfully processed Kafka message with sum: {}", message.getSum());
            
        } catch (Exception e) {
            logger.error("Critical error processing Kafka message with sum {}: {}", message.getSum(), e.getMessage(), e);
            // Don't retry here - let Kafka handle retries based on configuration
            throw new RuntimeException("Failed to process Kafka message", e);
        }
    }
}