package com.draw.system.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "participants")
public class Participant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    private String phone;
    
    private LocalDateTime createTime;
    
    private LocalDateTime winTime;
    
    private boolean hasWon;

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getWinTime() {
        return winTime;
    }
    
    public void setWinTime(LocalDateTime winTime) {
        this.winTime = winTime;
    }
    
    public boolean isHasWon() {
        return hasWon;
    }
    
    public void setHasWon(boolean hasWon) {
        this.hasWon = hasWon;
    }
} 