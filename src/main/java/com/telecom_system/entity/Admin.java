package com.telecom_system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "admin_info")
public class Admin {
    @Id
    @Column(name = "account", length = 6, nullable = false, unique = true)
    private Integer account;
    
    @Column(name = "name", nullable = false, length = 20)
    private String name;
    
    @Column(name = "password", nullable = false, length = 20)
    private String password;
    
    // 构造方法
    public Admin() {}
    
    public Admin(Integer account, String name, String password) {
        this.account = account;
        this.name = name;
        this.password = password;
    }
    
    // Getter和Setter方法
    public Integer getAccount() { return account; }
    public void setAccount(Integer account) { this.account = account; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}