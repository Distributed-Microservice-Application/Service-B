package com.DMA.Service_B.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.ZonedDateTime;

public class KafkaMessage {
    private int sum;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX")
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
