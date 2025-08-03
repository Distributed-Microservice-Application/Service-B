package com.DMA.Service_B.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "outbox")
public class Outbox {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sum", nullable = false)
    private Integer sum = 0;
    
    public Outbox() {}
    
    public Outbox(Integer sum) {
        this.sum = sum;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getSum() {
        return sum;
    }
    
    public void setSum(Integer sum) {
        this.sum = sum;
    }
    
    @Override
    public String toString() {
        return "Outbox{" +
                "id=" + id +
                ", sum=" + sum +
                '}';
    }

}
