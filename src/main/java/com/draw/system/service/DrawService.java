package com.draw.system.service;

import com.draw.system.entity.Participant;

import java.time.LocalDateTime;
import java.util.List;

public interface DrawService {
    
    Participant registerParticipant(String name, String phone);
    
    List<Participant> getAllParticipants();
    
    List<Participant> getEligibleParticipants(LocalDateTime startTime, LocalDateTime endTime);
    
    List<Participant> drawWinners(int count, LocalDateTime startTime, LocalDateTime endTime);
    
    List<Participant> getAllWinners();
    
    List<Participant> getWinnersByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
} 