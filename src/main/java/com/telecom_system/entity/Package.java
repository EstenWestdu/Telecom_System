package com.telecom_system.entity;

import java.math.BigDecimal;
import jakarta.persistence.*;

@Entity  //JPA实体类，映射到数据库表 package_info
@Table(name = "package_info")
public class Package {
    @Id
    @Column(name = "id", nullable = false, length = 6, unique = true)
    private Integer id;             // 套餐ID    PK
    
    @Column(name = "duration", columnDefinition = "INTERVAL", nullable = false)
    private String duration; // 使用String存储INTERVAL，如 "100 hours"

    @Column(name = "cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal cost;
    
    // 构造方法
    public Package() {}
    
    public Package(Integer id, String duration, BigDecimal cost) {
        this.id = id;
        this.duration = duration;
        this.cost = cost;
    }
    
    // Getter和Setter方法
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
    
    // 便捷方法：获取小时数
    public Long getHours() {
        if (duration != null && duration.contains("hours")) {
            return Long.parseLong(duration.split(" ")[0]);
        }
        return 0L;
    }
}