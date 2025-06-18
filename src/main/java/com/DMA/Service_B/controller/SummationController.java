package com.DMA.Service_B.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.DMA.Service_B.service.SummationService;

@RestController
@RequestMapping("/api")
public class SummationController {
    
    @Autowired
    private SummationService summationService;

    @GetMapping("/summation")
    public int getSummationValue() {
        String summationValue = summationService.getSummationValue();
        try {
            return Integer.parseInt(summationValue);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid summation value: " + summationValue);
        }
    }
}
