package com.telecom_system.repository;

import com.telecom_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    // 根据用户名查找用户
    Optional<User> findByName(String name);

    // === 登录验证方法 ===
    
    // 方式1：使用 account + 密码验证
    default boolean validateLoginByAccount(Integer account, String password) {
        return findById(account)
                .map(user -> user.getPassword().equals(password))
                .orElse(false);
    }
    
    // 方式2：使用 name + 密码验证  
    default boolean validateLoginByName(String name, String password) {
        return findByName(name)
                .map(user -> user.getPassword().equals(password))
                .orElse(false);
    }
    
    // 综合登录验证：自动判断是account还是name
    default Optional<User> validateLogin(String identifier, String password) {
        // 先尝试按account登录（如果identifier是数字）
        try {
            Integer account = Integer.valueOf(identifier);
            Optional<User> user = findById(account);
            if (user.isPresent() && user.get().getPassword().equals(password)) {
                return user;
            }
        } catch (NumberFormatException e) {
            // 如果不是数字，继续尝试按name登录
        }
        
        // 按name登录
        Optional<User> user = findByName(identifier);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            return user;
        }
        
        return Optional.empty();
    }
    
    // 获取登录用户信息（不验证密码，用于查询）
    default Optional<User> findUserForLogin(String identifier) {
        // 先尝试按account查找
        try {
            Integer account = Integer.valueOf(identifier);
            Optional<User> user = findById(account);
            if (user.isPresent()) {
                return user;
            }
        } catch (NumberFormatException e) {
            // 如果不是数字，继续尝试按name查找
        }
        
        // 按name查找
        return findByName(identifier);
    }
    
    // 根据电话号码查找用户
    Optional<User> findByPhone(String phone);
    
    // 查找使用指定套餐的用户
    List<User> findByPackageId(Integer packageId);
    
    // 根据余额范围查找用户
    List<User> findByBalanceBetween(Double minBalance, Double maxBalance);
    
    // 查找余额大于指定值的用户
    List<User> findByBalanceGreaterThan(Double balance);
    
    // 自定义查询：查找套餐使用率高的用户
    @Query("SELECT u FROM User u WHERE u.balance < :minBalance AND u.packageId = :packageId")
    List<User> findUsersWithLowBalanceAndPackage(
        @Param("minBalance") Double minBalance, 
        @Param("packageId") Integer packageId
    );

    List<User> findByNameContaining(String name);
 
    
}