package com.DMA.Service_B.model;

import java.time.ZonedDateTime;

public class KafkaMessage {
    private int sum;
    
    private ZonedDateTime timestamp;

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

}
