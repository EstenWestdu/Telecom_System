package com.telecom_system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_info")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自动递增
    @Column(name = "account",nullable = false,length = 6, unique = true)
    private Integer account;                    //用户ID    PK
    
    @Column(name = "name", nullable = false, length = 20)
    private String name; // 用户名
    
    @Column(name = "password", nullable = false, length = 20)
    private String password; // 用户密码

    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal balance; // 账户余额

    @Column(name = "package_id", nullable = false, length = 6)
    private Integer packageId; // 所选套餐ID
    
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;  // 新增电话号码字段

    // 原有其他字段（根据业务需要保留）
    private String role; // "ENTERPRISE" 或 "NORMAL"
    private LocalDateTime createTime = LocalDateTime.now();

    
    // 构造方法
    public User() {}
    
    public User(Integer account, String name, String password, BigDecimal balance, Integer packageId) {
        this.account = account;
        this.name = name;
        this.password = password;
        this.balance = balance;
        this.packageId = packageId;
    }
    
    // Getter和Setter方法
    public Integer getAccount() { return account; }
    public void setAccount(Integer account) { this.account = account; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public Integer getPackageId() { return packageId; }
    public void setPackageId(Integer packageId) { this.packageId = packageId; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}