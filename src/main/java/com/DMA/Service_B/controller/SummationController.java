package com.DMA.Service_B.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.DMA.Service_B.metrics.PrometheusMetrics;
import com.DMA.Service_B.service.SummationService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class SummationController {
    
    @Autowired
    private SummationService summationService;
    
    @Autowired
    private PrometheusMetrics metrics;

    @GetMapping("/summation")
    public int getSummationValue(HttpServletRequest request) {
        // Record start time for latency measurement
        long startTime = System.currentTimeMillis();
        
        // Process request
        String summationValue = summationService.getSummationValue();
        int result;
        try {
            result = Integer.parseInt(summationValue);
            
            // Record metrics
            long latency = System.currentTimeMillis() - startTime;
            metrics.recordRequestLatency("GET", "/api/summation", latency);
            metrics.incrementRequestCount("GET", "/api/summation", String.valueOf(HttpStatus.OK.value()));
            
            return result;
        } catch (NumberFormatException e) {
            // Record failure metrics
            long latency = System.currentTimeMillis() - startTime;
            metrics.recordRequestLatency("GET", "/api/summation", latency);
            metrics.incrementRequestCount("GET", "/api/summation", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            
            throw new RuntimeException("Invalid summation value: " + summationValue);
        }
    }
}
