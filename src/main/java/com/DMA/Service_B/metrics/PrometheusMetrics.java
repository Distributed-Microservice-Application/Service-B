package com.DMA.Service_B.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PrometheusMetrics {

    private final MeterRegistry registry;
    private final ConcurrentHashMap<String, Counter> requestCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, DistributionSummary> latencySummaries = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, DistributionSummary> queueTimers = new ConcurrentHashMap<>();

    public PrometheusMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * Records queue latency with a specific queue name (recommended for Kafka consumers)
     */
    public void recordQueueLatencyWithQueueName(double latencyMs, String queueName, String method, String url) {
        String key = String.format("%s-%s", method, url);
        
        DistributionSummary summary = queueTimers.computeIfAbsent(key, k ->
            DistributionSummary.builder("example_queue_latency_ms")
                              .description("Histogram for queue latency in milliseconds")
                              .tag("application", "Service-B")
                              .tag("queue", queueName)
                              .tag("method", method)
                              .tag("url", url)
                              .publishPercentileHistogram(true)
                              .serviceLevelObjectives(100, 200, 300, 400, 500, 600, 700, 800, 900, 1000)
                              .register(registry)
        );

        summary.record(latencyMs);
    }

    public void recordRequestLatency(String method, String url, double latencyMs) {
        String key = String.format("%s-%s", method, url);
        
        DistributionSummary summary = latencySummaries.computeIfAbsent(key, k ->
            DistributionSummary.builder("example_request_latency_ms")
                              .description("Histogram for request latency in millisecond")
                              .tag("application", "Service-B")
                              .tag("method", method)
                              .tag("url", url)
                              .publishPercentileHistogram(true)
                              .serviceLevelObjectives(100, 200, 300, 400, 500, 600, 700, 800, 900, 1000)
                              .register(registry)
        );
        
        summary.record(latencyMs);
    }

    public void incrementRequestCount(String method, String url, String status) {
        String key = String.format("%s-%s-%s", method, url, status);
        
        Counter counter = requestCounters.computeIfAbsent(key, k -> 
            Counter.builder("example_requests_count_total")
                  .description("Request counter per method, url and status code")
                  .tag("application", "Service-B")
                  .tag("method", method)
                  .tag("url", url)
                  .tag("status", status)
                  .register(registry)
        );
        
        counter.increment();
    }
}
