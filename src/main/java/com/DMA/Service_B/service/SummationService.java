package com.DMA.Service_B.service;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.NoSuchFileException;

@Service
public class SummationService {
    
    private static final String DEFAULT_VALUE = "0";
    private static final String FILE_PATH = "files/summation.txt";
    
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
            String currentValue;
            try {
                currentValue = Files.readString(Paths.get(FILE_PATH)).trim();
                if (currentValue.isEmpty()) {
                    currentValue = DEFAULT_VALUE;
                    Files.writeString(Paths.get(FILE_PATH), DEFAULT_VALUE);
                }
            } catch (NoSuchFileException e) {
                currentValue = DEFAULT_VALUE;
                Files.writeString(Paths.get(FILE_PATH), DEFAULT_VALUE);
            }
            
            int currentSum = Integer.parseInt(currentValue);
            int newSum = currentSum + Integer.parseInt(newValue);
            Files.writeString(Paths.get(FILE_PATH), String.valueOf(newSum));
        } catch (IOException e) {
            throw new RuntimeException("Error writing to summation file: " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number format: " + e.getMessage());
        }
    }
}