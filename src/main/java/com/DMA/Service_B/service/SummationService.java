package com.DMA.Service_B.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

@Service
public class SummationService {
    private static final Logger logger = LoggerFactory.getLogger(SummationService.class);
    
    private static final String DEFAULT_VALUE = "0";
    private static final String FILE_PATH = System.getProperty("user.dir") + "/files/summation.txt";
    
    // Get the value from the files/summation.txt file
    public String getSummationValue() {
        try {
            String content = Files.readString(Paths.get(FILE_PATH)).trim();
            if (content.isEmpty()) {
                Files.writeString(Paths.get(FILE_PATH), DEFAULT_VALUE);
                return DEFAULT_VALUE;
            }
            return content;
        } catch (NoSuchFileException e) {
            try {
                Files.writeString(Paths.get(FILE_PATH), DEFAULT_VALUE);
                return DEFAULT_VALUE;
            } catch (IOException ex) {
                throw new RuntimeException("Error creating summation file: " + ex.getMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading summation file: " + e.getMessage());
        }
    }

    // Update the value in the files/summation.txt file
    public void updateSummationValue(String newValue) {
        try {
            logger.info("Updating summation file at: {}", FILE_PATH);
            String currentValue;
            Path filePath = Paths.get(FILE_PATH);
            
            // Create parent directories if they don't exist
            Files.createDirectories(filePath.getParent());
            
            try {
                currentValue = Files.readString(filePath).trim();
                logger.info("Current value read from file: {}", currentValue);
                if (currentValue.isEmpty()) {
                    currentValue = DEFAULT_VALUE;
                    Files.writeString(filePath, DEFAULT_VALUE);
                    logger.info("File was empty, initialized with default value: {}", DEFAULT_VALUE);
                }
            } catch (NoSuchFileException e) {
                currentValue = DEFAULT_VALUE;
                Files.writeString(filePath, DEFAULT_VALUE);
                logger.info("File not found, created with default value: {}", DEFAULT_VALUE);
            }
            
            int currentSum = Integer.parseInt(currentValue);
            int newSum = currentSum + Integer.parseInt(newValue);
            Files.writeString(filePath, String.valueOf(newSum));
            logger.info("Updated sum in file. Previous: {}, New value added: {}, New total: {}", currentSum, newValue, newSum);
        } catch (IOException e) {
            logger.error("Error writing to summation file: {}", e.getMessage(), e);
            throw new RuntimeException("Error writing to summation file: " + e.getMessage());
        } catch (NumberFormatException e) {
            logger.error("Invalid number format: {}", e.getMessage(), e);
            throw new RuntimeException("Invalid number format: " + e.getMessage());
        }
    }
}