// src/main/java/com/telecom_system/RepositoryTester.java
package com.telecom_system;

import com.telecom_system.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "repo.tester.enabled", havingValue = "true", matchIfMissing = false)
public class RepositoryTester {
    
    @Bean
    public CommandLineRunner testRepositories(
            UserRepository userRepository,
            AdminRepository adminRepository, 
            PackageRepository packageRepository,
            LoginInfoRepository loginInfoRepository) {
        
        return args -> {
            System.out.println("🚀 开始快速测试 Repository...");
            
            // 测试基本计数操作
            testRepository("UserRepository", userRepository::count);
            testRepository("AdminRepository", adminRepository::count);
            testRepository("PackageRepository", packageRepository::count);
            testRepository("LoginInfoRepository", loginInfoRepository::count);
            
            System.out.println("✅ 所有 Repository 基本测试完成！");
            System.exit(0); // 测试完成后退出
        };
    }
    
    private void testRepository(String name, Runnable test) {
        try {
            test.run();
            System.out.println("✅ " + name + " - 正常");
        } catch (Exception e) {
            System.out.println("❌ " + name + " - 异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}