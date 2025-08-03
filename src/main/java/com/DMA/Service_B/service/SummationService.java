package com.DMA.Service_B.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

@Service
public class SummationService {
    private static final Logger logger = LoggerFactory.getLogger(SummationService.class);
    
    @Autowired
    private DatabaseService databaseService;
    
    @PostConstruct
    public void init() {
        // Initialize the summation record in both databases on startup
        databaseService.initializeSummationRecord();
        logger.info("SummationService initialized with database backend");
    }
    
    /**
     * Get the summation value from the Replica database (read operation)
     */
    public String getSummationValue() {
        try {
            Integer value = databaseService.getSummationValue();
            return String.valueOf(value);
        } catch (Exception e) {
            logger.error("Error getting summation value: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting summation value: " + e.getMessage());
        }
    }

    /**
     * Update the summation value in the Leader database and sync to Replica (write operation)
     */
    public void updateSummationValue(String newValue) {
        try {
            Integer valueToAdd = Integer.parseInt(newValue);
            logger.info("Updating summation value by adding: {}", valueToAdd);
            
            databaseService.updateSummationValue(valueToAdd);
            logger.info("Successfully updated summation value by adding: {}", valueToAdd);
        } catch (NumberFormatException e) {
            logger.error("Invalid number format: {}", e.getMessage(), e);
            throw new RuntimeException("Invalid number format: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating summation value: {}", e.getMessage(), e);
            throw new RuntimeException("Error updating summation value: " + e.getMessage());
        }
    }
}