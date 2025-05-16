package com.draw.system.controller;

import com.draw.system.entity.Participant;
import com.draw.system.service.DrawService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DrawController {
    
    @Autowired
    private DrawService drawService;
    
    // 手机用户注册页面
    @GetMapping("/")
    public String indexPage() {
        return "register";
    }
    
    // 管理员抽奖页面
    @GetMapping("/admin")
    public String adminPage() {
        return "admin";
    }
    
    // 用户注册接口
    @PostMapping("/api/register")
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String phone = request.get("phone");
        
        try {
            Participant participant = drawService.registerParticipant(name, phone);
            return ResponseEntity.ok(participant);
        } catch (IllegalStateException e) {
            // 处理每日限制异常
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "注册失败，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 获取符合条件的参与者
    @GetMapping("/api/participants")
    @ResponseBody
    public List<Participant> getEligibleParticipants(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return drawService.getEligibleParticipants(startTime, endTime);
    }
    
    // 抽奖接口
    @PostMapping("/api/draw")
    @ResponseBody
    public List<Participant> drawWinners(
            @RequestParam int count,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return drawService.drawWinners(count, startTime, endTime);
    }
    
    // 获取所有中奖者
    @GetMapping("/api/winners")
    @ResponseBody
    public List<Participant> getAllWinners() {
        return drawService.getAllWinners();
    }
    
    // 按时间范围获取中奖者
    @GetMapping("/api/winners/range")
    @ResponseBody
    public List<Participant> getWinnersByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return drawService.getWinnersByTimeRange(startTime, endTime);
    }
} 