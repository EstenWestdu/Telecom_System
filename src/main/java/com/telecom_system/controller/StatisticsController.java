package com.telecom_system.controller;

import com.telecom_system.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }
    
    /**
     * 获取系统总体统计
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getSystemOverview() {
        return ResponseEntity.ok(statisticsService.getSystemStatistics());
    }
    
    /**
     * 获取套餐使用统计
     */
    @GetMapping("/package-usage")
    public ResponseEntity<?> getPackageUsageStatistics() {
        return ResponseEntity.ok(statisticsService.getPackageUsageStatistics());
    }
    
    /**
     * 获取用户活跃度统计
     */
    @GetMapping("/user-activity")
    public ResponseEntity<?> getUserActivityStatistics() {
        return ResponseEntity.ok(statisticsService.getUserActivityStatistics());
    }

    /**
     * 获取小时活跃度统计
     */
    @GetMapping("/user-hourly-activity")
    public ResponseEntity<?> getHourlyOnlineUserStatistics() {
        return ResponseEntity.ok(statisticsService.getHourlyOnlineUserStatistics());
    }
    
    /**
     * 获取收入统计
     */
    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenueStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(statisticsService.getRevenueStatistics(start, end));
    }
    
    /**
     * 获取实时数据统计
     */
    @GetMapping("/realtime")
    public ResponseEntity<Map<String, Object>> getRealtimeStatistics() {
        return ResponseEntity.ok(statisticsService.getRealtimeStatistics());
    }
}