package com.telecom_system.repository;

import com.telecom_system.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
    
    // 根据管理员名查找
    Optional<Admin> findByName(String name);
    
    // === 登录验证方法 ===
    
    // 方式1：使用 account + 密码验证
    default boolean validateLoginByAccount(Integer account, String password) {
        return findById(account)
                .map(admin -> admin.getPassword().equals(password))
                .orElse(false);
    }
    
    // 方式2：使用 name + 密码验证
    default boolean validateLoginByName(String name, String password) {
        return findByName(name)
                .map(admin -> admin.getPassword().equals(password))
                .orElse(false);
    }
    
    // 综合登录验证
    default Optional<Admin> validateLogin(String identifier, String password) {
        // 先尝试按account登录
        try {
            Integer account = Integer.valueOf(identifier);
            Optional<Admin> admin = findById(account);
            if (admin.isPresent() && admin.get().getPassword().equals(password)) {
                return admin;
            }
        } catch (NumberFormatException e) {
            // 继续尝试按name登录
        }
        
        // 按name登录
        Optional<Admin> admin = findByName(identifier);
        if (admin.isPresent() && admin.get().getPassword().equals(password)) {
            return admin;
        }
        
        return Optional.empty();
    }
    // 检查管理员名是否存在
    boolean existsByName(String name);
}

