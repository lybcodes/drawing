package com.draw.system.service;

import com.draw.system.entity.Participant;
import com.draw.system.repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DrawServiceImpl implements DrawService {
    
    @Autowired
    private ParticipantRepository participantRepository;
    
    @Override
    public Participant registerParticipant(String name, String phone) {
        // 检查今天是否已经用该手机号注册过
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        
        boolean alreadyRegisteredToday = participantRepository.existsByPhoneAndCreatedToday(phone, startOfDay, endOfDay);
        
        if (alreadyRegisteredToday) {
            throw new IllegalStateException("该手机号今天已经报名，请明天再来");
        }
        
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
            // 洗牌
            Collections.shuffle(eligibleParticipants);
            
            // 抽取不超过指定数量的中奖者，确保本次抽奖中不会有重复的手机号
            int actualCount = Math.min(count, eligibleParticipants.size());
            Set<String> currentDrawPhones = new HashSet<>();
            
            int i = 0;
            int processed = 0;
            
            while (winners.size() < actualCount && processed < eligibleParticipants.size()) {
                Participant candidate = eligibleParticipants.get(i);
                i = (i + 1) % eligibleParticipants.size(); // 循环遍历
                processed++;
                
                // 如果当前抽奖中已经有相同手机号的用户中奖，则跳过
                if (currentDrawPhones.contains(candidate.getPhone())) {
                    continue;
                }
                
                // 标记为中奖并保存
                candidate.setHasWon(true);
                candidate.setWinTime(LocalDateTime.now());
                winners.add(participantRepository.save(candidate));
                currentDrawPhones.add(candidate.getPhone());
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