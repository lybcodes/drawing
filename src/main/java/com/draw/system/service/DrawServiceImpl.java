package com.draw.system.service;

import com.draw.system.entity.Participant;
import com.draw.system.repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DrawServiceImpl implements DrawService {
    
    @Autowired
    private ParticipantRepository participantRepository;
    
    @Override
    public Participant registerParticipant(String name, String phone) {
        Participant participant = new Participant();
        participant.setName(name);
        participant.setPhone(phone);
        participant.setCreateTime(LocalDateTime.now());
        participant.setHasWon(false);
        return participantRepository.save(participant);
    }
    
    @Override
    public List<Participant> getAllParticipants() {
        return participantRepository.findAll();
    }
    
    @Override
    public List<Participant> getEligibleParticipants(LocalDateTime startTime, LocalDateTime endTime) {
        return participantRepository.findEligibleParticipantsByTimeRange(startTime, endTime);
    }
    
    @Override
    public List<Participant> drawWinners(int count, LocalDateTime startTime, LocalDateTime endTime) {
        // 获取符合条件的参与者
        List<Participant> eligibleParticipants = getEligibleParticipants(startTime, endTime);
        
        // 移除半年内已中奖的用户
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Participant> winnersInPastSixMonths = participantRepository.findWinnersByTimeRange(sixMonthsAgo, LocalDateTime.now());
        
        // 获取这些中奖者的电话号码
        List<String> excludedPhones = winnersInPastSixMonths.stream()
                .map(Participant::getPhone)
                .collect(Collectors.toList());
        
        // 过滤掉半年内已中奖的用户
        eligibleParticipants = eligibleParticipants.stream()
                .filter(p -> !excludedPhones.contains(p.getPhone()))
                .collect(Collectors.toList());
        
        // 随机抽取指定数量的中奖者
        List<Participant> winners = new ArrayList<>();
        if (!eligibleParticipants.isEmpty()) {
            Collections.shuffle(eligibleParticipants);
            // 抽取不超过指定数量的中奖者
            int actualCount = Math.min(count, eligibleParticipants.size());
            for (int i = 0; i < actualCount; i++) {
                Participant winner = eligibleParticipants.get(i);
                winner.setHasWon(true);
                winner.setWinTime(LocalDateTime.now());
                winners.add(participantRepository.save(winner));
            }
        }
        
        return winners;
    }
    
    @Override
    public List<Participant> getAllWinners() {
        return participantRepository.findByHasWonTrue();
    }
    
    @Override
    public List<Participant> getWinnersByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return participantRepository.findWinnersByTimeRange(startTime, endTime);
    }
}