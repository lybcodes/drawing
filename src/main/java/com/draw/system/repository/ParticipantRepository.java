package com.draw.system.repository;

import com.draw.system.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    
    List<Participant> findByHasWonFalse();
    
    List<Participant> findByHasWonTrue();
    
    @Query("SELECT p FROM Participant p WHERE p.createTime BETWEEN :startTime AND :endTime AND p.hasWon = false")
    List<Participant> findEligibleParticipantsByTimeRange(
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT p FROM Participant p WHERE p.hasWon = true AND p.winTime BETWEEN :startTime AND :endTime")
    List<Participant> findWinnersByTimeRange(
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime);
} 