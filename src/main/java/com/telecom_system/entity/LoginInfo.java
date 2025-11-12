package com.telecom_system.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;


@Entity
@Table(name = "login_info")
public class LoginInfo {
    
    // 使用嵌入式主键
    @EmbeddedId
    private LoginInfoPK id;
    
    @Column(name = "logout_time")
    private LocalDateTime logoutTime;
    
    @ManyToOne
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private User user;
    
    // 构造方法
    public LoginInfo() {}
    
    public LoginInfo(Integer accountId, LocalDateTime loginTime) {
        this.id = new LoginInfoPK(accountId, loginTime);
    }
    
    // 便捷的getter方法
    public Integer getAccountId() {
        return id != null ? id.getAccountId() : null;
    }
    
    public LocalDateTime getLoginTime() {
        return id != null ? id.getLoginTime() : null;
    }
    
    // Getter和Setter
    public LoginInfoPK getId() { return id; }
    public void setId(LoginInfoPK id) { this.id = id; }
    public LocalDateTime getLogoutTime() { return logoutTime; }
    public void setLogoutTime(LocalDateTime logoutTime) { this.logoutTime = logoutTime; }
    public User getUserInfo() { return user; }
    public void setUserInfo(User user) { this.user = user; }
    
    // 嵌入式主键类（作为内部类）
    @Embeddable
    public static class LoginInfoPK implements Serializable {
        @Column(name = "account_id", nullable = false)
        private Integer accountId;
        
        @Column(name = "login_time", nullable = false)
        private LocalDateTime loginTime;
        
        // 必须有无参构造方法
        public LoginInfoPK() {}
        
        public LoginInfoPK(Integer accountId, LocalDateTime loginTime) {
            this.accountId = accountId;
            this.loginTime = loginTime;
        }
        
        // Getter和Setter
        public Integer getAccountId() { return accountId; }
        public void setAccountId(Integer accountId) { this.accountId = accountId; }
        
        public LocalDateTime getLoginTime() { return loginTime; }
        public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
        
        // 必须重写equals和hashCode
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LoginInfoPK that = (LoginInfoPK) o;
            return Objects.equals(accountId, that.accountId) &&
                   Objects.equals(loginTime, that.loginTime);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(accountId, loginTime);
        }
    }
}