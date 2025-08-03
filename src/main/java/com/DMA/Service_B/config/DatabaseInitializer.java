package com.DMA.Service_B.config;

import com.DMA.Service_B.service.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    
    @Autowired
    private DatabaseService databaseService;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing databases on application startup...");
        try {
            databaseService.initializeSummationRecord();
            logger.info("Database initialization completed successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize databases: {}", e.getMessage(), e);
            throw e;
        }
    }
}
