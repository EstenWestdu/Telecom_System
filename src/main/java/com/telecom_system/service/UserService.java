package com.telecom_system.service;

import com.telecom_system.entity.User;
import com.telecom_system.entity.Package;
import com.telecom_system.repository.PackageRepository;
import com.telecom_system.repository.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;


import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PackageRepository packageRepository;

    public UserService(UserRepository userRepository,PackageRepository packageRepository) {
        this.userRepository = userRepository;
        this.packageRepository = packageRepository;
    }
    
    /**
     * 查找所有用户
     */
    public List<User> findAllByOrderByAccountDesc() {
        return userRepository.findAllByOrderByAccountDesc();
    }
    
    /**
     * 分页查询用户
     */
    public Page<User> findUsersByPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }
    
    /**
     * 根据ID查找用户
     */
    public Optional<User> findUserById(Integer account) {
        return userRepository.findById(account);
    }
    
    /**
     * 创建新用户
     */
    public User createUser(User User) {
        // 业务逻辑验证
        if (userRepository.existsById(User.getAccount())) {
            throw new RuntimeException("用户ID已存在: " + User.getAccount());
        }
        
        if (userRepository.findByName(User.getName()).isPresent()) {
            throw new RuntimeException("用户名已存在: " + User.getName());
        }
        
        // 设置默认值
        if (User.getBalance() == null) {
            User.setBalance(BigDecimal.ZERO);
        }
        
        return userRepository.save(User);
    }
    
    /**
     * 更新用户信息
     */
    public User updateUser(Integer account, User User) {
        return userRepository.findById(account)
                .map(existingUser -> {
                    // 只更新允许修改的字段
                    if (User.getName() != null) {
                        existingUser.setName(User.getName());
                    }
                    if (User.getPhone() != null) {
                        existingUser.setPhone(User.getPhone());
                    }
                    if (User.getPackageId() != null) {
                        existingUser.setPackageId(User.getPackageId());
                    }
                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new RuntimeException("用户不存在: " + account));
    }
    
    /**
     * 删除用户
     */
    public void deleteUser(Integer account) {
        if (!userRepository.existsById(account)) {
            throw new RuntimeException("用户不存在: " + account);
        }
        userRepository.deleteById(account);
    }
    
    /**
     * 用户充值
     */
    public User recharge(Integer account, Double amount) {
        if (amount <= 0) {
            throw new RuntimeException("充值金额必须大于0");
        }
        
        return userRepository.findById(account)
                .map(user -> {
                    BigDecimal newBalance = user.getBalance().add(BigDecimal.valueOf(amount));
                    user.setBalance(newBalance);
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("用户不存在: " + account));
    }
    
    /**
     * 用户消费/扣费
     */
    public User deductBalance(Integer account, Double amount) {
        if (amount <= 0) {
            throw new RuntimeException("扣费金额必须大于0");
        }
        
        return userRepository.findById(account)
                .map(user -> {
                    BigDecimal newBalance = user.getBalance().subtract(BigDecimal.valueOf(amount));
                    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                        throw new RuntimeException("余额不足，当前余额: " + user.getBalance());
                    }
                    user.setBalance(newBalance);
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("用户不存在: " + account));
    }
    
    /**
     * 更改用户套餐
     */
    public User changePackage(Integer account, Integer packageId) {
        try {
            // 1. 查询用户信息
            User user = userRepository.findById(account)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + account));
            
            // 2. 查询套餐信息
            Package pkg = packageRepository.findById(packageId)
                    .orElseThrow(() -> new RuntimeException("套餐不存在: " + packageId));
            
            // 3. 检查余额是否足够
            if (user.getBalance().compareTo(pkg.getCost()) < 0) {
                throw new RuntimeException("余额不足，当前余额: " + user.getBalance() + "，套餐费用: " + pkg.getCost());
            }
            
            // 4. 扣费
            user.setBalance(user.getBalance().subtract(pkg.getCost()));
            
            // 5. 更新套餐（不累加时长，直接覆盖）
            user.setPackageId(packageId);
            user.setPackageStartTime(LocalDateTime.now());
            
            // 6. 保存用户信息
            return userRepository.save(user);
            
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("操作失败，数据完整性约束违反: " + e.getMessage());
        }
    }
    
    /**
    * 获取用户剩余时长信息 - 使用原生SQL替代视图
    */
    public Map<String, Object> getRemainingTime(Integer account) {
        // 首先验证用户是否存在
        User user = userRepository.findById(account)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + account));
        try {
            Map<String, Object> queryData = userRepository.findRemainingTimeByAccount(account);
            
            // 处理返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("account", user.getAccount());
            result.put("name", user.getName());
            result.put("phone", user.getPhone());
            result.put("packageId", user.getPackageId());
            result.put("balance", user.getBalance());
            
            // 计算时间格式
            BigDecimal usedSeconds = (BigDecimal) queryData.get("used_seconds");
            BigDecimal remainingSeconds = (BigDecimal) queryData.get("remaining_seconds");
            BigDecimal remainingHours = (BigDecimal) queryData.get("remaining_hours");
            
            // 转换为更友好的格式
            result.put("totalDuration", queryData.get("total_duration"));
            result.put("usedSeconds", usedSeconds != null ? usedSeconds.doubleValue() : 0.0);
            result.put("usedHours", usedSeconds != null ? usedSeconds.doubleValue() / 3600 : 0.0);
            result.put("remainingSeconds", remainingSeconds != null ? remainingSeconds.doubleValue() : 0.0);
            result.put("remainingHours", remainingHours != null ? remainingHours.doubleValue() : 0.0);
            result.put("status", queryData.get("status"));
            result.put("packageCost", queryData.get("cost"));
            
            // 添加格式化后的显示文本
            result.put("usedDurationText", formatDuration(usedSeconds != null ? usedSeconds.doubleValue() : 0.0));
            result.put("remainingDurationText", formatDuration(remainingSeconds != null ? remainingSeconds.doubleValue() : 0.0));
            
            return result;
            
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("用户剩余时长信息不存在: " + account);
        }
    }
    /**
     * 格式化时间为易读格式
     */
    private String formatDuration(double seconds) {
        if (seconds <= 0) {
            return "0小时";
        }
        
        long totalSeconds = (long) seconds;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        
        if (hours == 0 && minutes == 0) {
            return totalSeconds + "秒";
        } else if (minutes == 0) {
            return hours + "小时";
        } else {
            return hours + "小时" + minutes + "分钟";
        }
    }
    

    
    /**
     * 条件搜索用户
     */
    public List<User> searchUsers(String name, String phone) {
        if (name != null && !name.trim().isEmpty()) {
            return userRepository.findByNameContaining(name);
        }
        
        if (phone != null && !phone.trim().isEmpty()) {
            return userRepository.findByPhone(phone)
                    .map(List::of)
                    .orElse(List.of());
        }
        
        return userRepository.findAll();
    }
    
    
    /**
     * 统计用户数量
     */
    public Map<String, Long> getUserStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        return stats;
    }
}