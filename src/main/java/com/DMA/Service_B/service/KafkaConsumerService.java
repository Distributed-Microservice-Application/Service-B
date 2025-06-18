package com.DMA.Service_B.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.DMA.Service_B.model.KafkaMessage;

@Service
public class KafkaConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private SummationService summationService;

    @KafkaListener(topics = "${kafka.topic.summation}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessage(KafkaMessage message) {
        logger.info("Received message from Kafka - sum: {}, timestamp: {}", message.getSum(), message.getTimestamp());
        summationService.updateSummationValue(String.valueOf(message.getSum()));
    }
}
