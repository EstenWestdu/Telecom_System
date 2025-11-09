package com.telecom_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    private String role; // "ENTERPRISE" 或 "NORMAL"
    private Double balance = 0.0;
    private String phone;
    private String plan = "基础套餐";
    private Double dataUsage = 0.0;
    private Double dataLimit = 1024.0; // 1GB
    
    private LocalDateTime createTime = LocalDateTime.now();
    
    // 构造方法
    public User() {}
    
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
    
    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    
    public Double getDataUsage() { return dataUsage; }
    public void setDataUsage(Double dataUsage) { this.dataUsage = dataUsage; }
    
    public Double getDataLimit() { return dataLimit; }
    public void setDataLimit(Double dataLimit) { this.dataLimit = dataLimit; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}