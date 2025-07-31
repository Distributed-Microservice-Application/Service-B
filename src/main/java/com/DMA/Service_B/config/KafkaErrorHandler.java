package com.DMA.Service_B.config;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Enhanced error handler for Kafka consumer to handle errors from Service-A messages
 * with improved logging and recovery mechanisms
 */
@Component
public class KafkaErrorHandler implements ConsumerAwareListenerErrorHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaErrorHandler.class);

    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception,
                             Consumer<?, ?> consumer) {
        
        logger.error("Error in Kafka listener: {}", exception.getMessage(), exception);
        
        // Extract ConsumerRecord if available
        Object payload = message.getPayload();
        if (payload instanceof ConsumerRecord) {
            ConsumerRecord<?, ?> record = (ConsumerRecord<?, ?>) payload;
            logger.error("Failed to process message from topic: {}, partition: {}, offset: {}, key: {}", 
                        record.topic(), record.partition(), record.offset(), record.key());
            
            // Log headers from Service-A for debugging
            record.headers().forEach(header -> 
                logger.debug("Header - {}: {}", header.key(), new String(header.value()))
            );
        }
        
        logger.info("Continuing processing after error...");
        
        return null;
    }
}
