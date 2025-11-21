package com.telecom_system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_info")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 实际值由数据库触发器/序列分配
    @Column(name = "account", nullable = false, unique = true)
    private Integer account;                    //用户ID    PK
    
    @Column(name = "name", nullable = false, length = 20)
    private String name; // 用户名
    
    @Column(name = "password", nullable = false, length = 60)
    private String password; // 用户密码

    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal balance; // 账户余额

    @Column(name = "package_id", nullable = false, length = 6)
    private Integer packageId; // 所选套餐ID
    
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;  // 新增电话号码字段

    @Column(name = "package_start_time",nullable = false, length = 30)
    private LocalDateTime packageStartTime; // 套餐开始时间

    @PrePersist
    private void ensurePackageStartTime() {
        if (this.packageStartTime == null) {
            this.packageStartTime = LocalDateTime.now();
        }
    }
    
    // 构造方法
    public User() {}
    
    public User(String name, String password, BigDecimal balance, Integer packageId) {
        this.name = name;
        this.password = password;
        this.balance = balance;
        this.packageId = packageId;
    }
    
    // Getter和Setter方法
    public Integer getAccount() { return account; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public Integer getPackageId() { return packageId; }
    public void setPackageId(Integer packageId) { this.packageId = packageId; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDateTime getPackageStartTime() { return packageStartTime; }
    public void setPackageStartTime(LocalDateTime packageStartTime) { this.packageStartTime = packageStartTime; }
}