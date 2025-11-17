package com.telecom_system.service;

import com.telecom_system.entity.User;
import com.telecom_system.repository.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;

//import jakarta.persistence.criteria.CriteriaBuilder.In;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public UserService(UserRepository userRepository, JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    /**
     * 查找所有用户
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
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
                    if (User.getRole() != null) {
                        existingUser.setRole(User.getRole());
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
        return userRepository.findById(account)
                .map(user -> {
                    user.setPackageId(packageId);
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("用户不存在: " + account));
                
        } catch (DataIntegrityViolationException e) {
            // 处理外键约束违反异常
            throw new RuntimeException("套餐不存在或无效: " + packageId);
        }
    }
    
    /**
    * 获取用户剩余时长信息 - 使用原生SQL替代视图
    */
    public Map<String, Object> getRemainingTime(Integer account) {
        // 首先验证用户是否存在
        User user = userRepository.findById(account)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + account));
        
        // 使用原生SQL替代视图查询
        String sql = """
            SELECT 
                u.account,
                u.name,
                u.phone,
                p.duration as total_duration,
                COALESCE(SUM(EXTRACT(EPOCH FROM (l.logout_time - l.login_time))), 0) as used_seconds,
                COALESCE(SUM(EXTRACT(EPOCH FROM (l.logout_time - l.login_time)) / 3600), 0) as used_hours,
                (EXTRACT(EPOCH FROM p.duration::interval) - COALESCE(SUM(EXTRACT(EPOCH FROM (l.logout_time - l.login_time))), 0)) as remaining_seconds,
                ((EXTRACT(EPOCH FROM p.duration::interval) - COALESCE(SUM(EXTRACT(EPOCH FROM (l.logout_time - l.login_time))), 0)) / 3600) as remaining_hours,
                u.balance,
                p.cost,
                CASE 
                    WHEN (EXTRACT(EPOCH FROM p.duration::interval) - COALESCE(SUM(EXTRACT(EPOCH FROM (l.logout_time - l.login_time))), 0)) < 0 
                    THEN '已超时'
                    ELSE '正常'
                END as status
            FROM user_info u
            JOIN package_info p ON u.package_id = p.id
            LEFT JOIN login_info l ON u.account = l.account_id 
                AND l.logout_time IS NOT NULL
            WHERE u.account = ?
            GROUP BY u.account, u.name, u.phone, p.duration, u.balance, p.cost
            """;
        
        try {
            Map<String, Object> queryData = jdbcTemplate.queryForMap(sql, account);
            
            // 处理返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("account", user.getAccount());
            result.put("name", user.getName());
            result.put("phone", user.getPhone());
            result.put("packageId", user.getPackageId());
            result.put("balance", user.getBalance());
            
            // 计算时间格式
            Double totalSeconds = (Double) queryData.get("used_seconds");
            Double remainingSeconds = (Double) queryData.get("remaining_seconds");
            Double remainingHours = (Double) queryData.get("remaining_hours");
            
            result.put("totalDuration", queryData.get("total_duration"));
            result.put("usedDuration", formatDuration(totalSeconds));
            result.put("remainingDuration", formatDuration(remainingSeconds));
            result.put("remainingHours", Math.round(remainingHours * 100.0) / 100.0);
            result.put("status", queryData.get("status"));
            result.put("packageCost", queryData.get("cost"));
            
            return result;
            
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("用户剩余时长信息不存在: " + account);
        }
    }

    /**
     * 将秒数格式化为易读的时长字符串
     */
    private String formatDuration(Double seconds) {
        if (seconds == null || seconds == 0) {
            return "0小时";
        }
        
        long totalSeconds = seconds.longValue();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        
        if (minutes == 0) {
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