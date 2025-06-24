package com.DMA.Service_B.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final Counter messageProcessedCounter;
    private final Counter messageFailedCounter;

    public MetricsService(MeterRegistry registry) {
        this.messageProcessedCounter = Counter.builder("service_b_messages_processed_total")
                .description("Number of messages processed successfully")
                .register(registry);

        this.messageFailedCounter = Counter.builder("service_b_messages_failed_total")
                .description("Number of messages that failed processing")
                .register(registry);
    }

    public void incrementProcessedMessages() {
        messageProcessedCounter.increment();
    }

    public void incrementFailedMessages() {
        messageFailedCounter.increment();
    }
}
