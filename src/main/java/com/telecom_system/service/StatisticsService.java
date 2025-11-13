package com.telecom_system.service;

import com.telecom_system.entity.Admin;
import com.telecom_system.entity.User;
import com.telecom_system.repository.LoginInfoRepository;
import com.telecom_system.repository.PackageRepository;
import com.telecom_system.repository.UserRepository;
import com.telecom_system.repository.AdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StatisticsService {
    
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PackageRepository packageRepository;
    private final LoginInfoRepository loginInfoRepository;
    
    public StatisticsService(UserRepository userRepository, 
                            AdminRepository adminRepository,
                           PackageRepository packageRepository,
                           LoginInfoRepository loginInfoRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.packageRepository = packageRepository;
        this.loginInfoRepository = loginInfoRepository;
    }
    
    /**
     * 获取系统总体统计信息
     */
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 用户统计
        List<User> allUsers = userRepository.findAll();
        List<Admin> allAdmins = adminRepository.findAll();
        long totalUsers = allUsers.size() + allAdmins.size();
        
        // 余额统计
        BigDecimal totalBalance = allUsers.stream()
                .map(User::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // 修复：使用新的 RoundingMode 枚举
        BigDecimal averageBalance = totalUsers > 0 ? 
                totalBalance.divide(BigDecimal.valueOf(totalUsers), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
        
        // 套餐统计
        long activePackages = packageRepository.count();
        
        // 在线用户统计
        List<Object> onlineUsers = loginInfoRepository.findByLogoutTimeIsNull().stream()
                .map(login -> {
                    Map<String, Object> onlineUser = new HashMap<>();
                    onlineUser.put("accountId", login.getAccountId());
                    onlineUser.put("loginTime", login.getLoginTime());
                    return onlineUser;
                })
                .collect(Collectors.toList());
        
        stats.put("totalUsers", totalUsers);
        stats.put("Users", allUsers.size());
        stats.put("Admins", allAdmins.size());
        stats.put("totalBalance", totalBalance);
        stats.put("averageBalance", averageBalance);
        stats.put("activePackages", activePackages);
        stats.put("onlineUsersCount", onlineUsers.size());
        stats.put("onlineUsers", onlineUsers);
        stats.put("lastUpdated", LocalDateTime.now());
        
        return stats;
    }
    
    /**
     * 获取套餐使用统计
     */
    public List<Map<String, Object>> getPackageUsageStatistics() {
        List<Object[]> results = packageRepository.findPopularPackages();
        
        return results.stream()
                .map(result -> {
                    Map<String, Object> packageStats = new HashMap<>();
                    
                    // 这里需要根据实际的 PackageInfo 结构调整
                    Object packageObj = result[0];
                    Long userCount = (Long) result[1];
                    
                    if (packageObj instanceof com.telecom_system.entity.Package) {
                        com.telecom_system.entity.Package packageInfo = 
                            (com.telecom_system.entity.Package) packageObj;
                        packageStats.put("packageId", packageInfo.getId());
                        packageStats.put("duration", packageInfo.getDuration());
                        packageStats.put("cost", packageInfo.getCost());
                    } else {
                        // 如果是数组形式
                        Object[] packageArray = (Object[]) packageObj;
                        packageStats.put("packageId", packageArray[0]);
                        packageStats.put("duration", packageArray[1]);
                        packageStats.put("cost", packageArray[2]);
                    }
                    
                    packageStats.put("userCount", userCount);
                    
                    // 计算使用率
                    long totalUsers = userRepository.count();
                    double usageRate = totalUsers > 0 ? (userCount.doubleValue() / totalUsers) * 100 : 0;
                    packageStats.put("usageRate", Math.round(usageRate * 100.0) / 100.0);
                    
                    return packageStats;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户活跃度统计
     */
    public List<Map<String, Object>> getUserActivityStatistics() {
        List<User> allUsers = userRepository.findAll();
        
        return allUsers.stream()
                .map(user -> {
                    Map<String, Object> activityStats = new HashMap<>();
                    
                    // 获取用户的登录记录
                    List<com.telecom_system.entity.LoginInfo> userLogins = 
                        loginInfoRepository.findByIdAccountId(user.getAccount());
                    
                    // 计算活跃度指标
                    long loginCount = userLogins.size();
                    
                    double totalOnlineHours = userLogins.stream()
                            .filter(login -> login.getLogoutTime() != null)
                            .mapToDouble(login -> {
                                java.time.Duration duration = java.time.Duration.between(
                                    login.getLoginTime(), login.getLogoutTime());
                                return duration.toMinutes() / 60.0;
                            })
                            .sum();
                    
                    // 计算最后登录时间
                    java.util.Optional<LocalDateTime> lastLogin = userLogins.stream()
                            .map(com.telecom_system.entity.LoginInfo::getLoginTime)
                            .max(LocalDateTime::compareTo);
                    
                    // 活跃度评级
                    String activityLevel;
                    if (loginCount == 0) {
                        activityLevel = "未活跃";
                    } else if (loginCount <= 5) {
                        activityLevel = "低活跃";
                    } else if (loginCount <= 20) {
                        activityLevel = "中活跃";
                    } else {
                        activityLevel = "高活跃";
                    }
                    
                    activityStats.put("accountId", user.getAccount());
                    activityStats.put("userName", user.getName());
                    activityStats.put("loginCount", loginCount);
                    activityStats.put("totalOnlineHours", Math.round(totalOnlineHours * 100.0) / 100.0);
                    activityStats.put("lastLoginTime", lastLogin.orElse(null));
                    activityStats.put("activityLevel", activityLevel);
                    activityStats.put("balance", user.getBalance());
                    activityStats.put("packageId", user.getPackageId());
                    
                    return activityStats;
                })
                .sorted((a, b) -> Long.compare(
                    (Long) b.get("loginCount"), 
                    (Long) a.get("loginCount")
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取收入统计
     */
    public Map<String, Object> getRevenueStatistics(LocalDateTime start, LocalDateTime end) {
        Map<String, Object> revenueStats = new HashMap<>();
        
        // 获取所有用户
        List<User> allUsers = userRepository.findAll();
        
        // 计算套餐收入（基于用户选择的套餐）
        BigDecimal totalPackageRevenue = allUsers.stream()
                .map(user -> {
                    // 这里需要根据用户套餐计算收入
                    // 简化处理：使用固定值或从套餐表获取
                    return BigDecimal.valueOf(50); // 示例值
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 计算充值统计
        BigDecimal totalBalance = allUsers.stream()
                .map(User::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 用户增长统计（基于创建时间）
        long newUsersInPeriod = allUsers.stream()
                .filter(user -> {
                    LocalDateTime createTime = user.getCreateTime();
                    return createTime != null && 
                           !createTime.isBefore(start) && 
                           !createTime.isAfter(end);
                })
                .count();
        
        revenueStats.put("period", start + " 至 " + end);
        revenueStats.put("totalPackageRevenue", totalPackageRevenue);
        revenueStats.put("totalUserBalance", totalBalance);
        revenueStats.put("newUsers", newUsersInPeriod);
        revenueStats.put("totalUsers", allUsers.size());
        revenueStats.put("estimatedMonthlyRevenue", totalPackageRevenue.multiply(BigDecimal.valueOf(0.3))); // 估算
        
        return revenueStats;
    }
    
    /**
     * 获取实时数据统计
     */
    public Map<String, Object> getRealtimeStatistics() {
        Map<String, Object> realtimeStats = new HashMap<>();
        
        // 当前在线用户
        List<com.telecom_system.entity.LoginInfo> onlineSessions = 
            loginInfoRepository.findByLogoutTimeIsNull();
        int onlineUsers = onlineSessions.size();
        
        // 今日登录用户
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        List<com.telecom_system.entity.LoginInfo> todayLogins = 
            loginInfoRepository.findByIdLoginTimeBetween(todayStart, todayEnd);
        int todayLoginUsers = (int) todayLogins.stream()
                .map(com.telecom_system.entity.LoginInfo::getAccountId)
                .distinct()
                .count();
        
        // 新注册用户（今日）
        List<User> allUsers = userRepository.findAll();
        int newUsersToday = (int) allUsers.stream()
                .filter(user -> {
                    LocalDateTime createTime = user.getCreateTime();
                    return createTime != null && 
                           !createTime.isBefore(todayStart) && 
                           !createTime.isAfter(todayEnd);
                })
                .count();
        
        // 系统负载信息
        long totalUsers = userRepository.count();
        long totalPackages = packageRepository.count();
        long totalLoginRecords = loginInfoRepository.count();
        
        realtimeStats.put("onlineUsers", onlineUsers);
        realtimeStats.put("todayLoginUsers", todayLoginUsers);
        realtimeStats.put("newUsersToday", newUsersToday);
        realtimeStats.put("totalUsers", totalUsers);
        realtimeStats.put("totalPackages", totalPackages);
        realtimeStats.put("totalLoginRecords", totalLoginRecords);
        realtimeStats.put("serverTime", LocalDateTime.now());
        realtimeStats.put("systemStatus", "正常运行");
        
        return realtimeStats;
    }
    
    /**
     * 获取套餐分布统计
     */
    public Map<String, Object> getPackageDistribution() {
        Map<String, Object> distribution = new HashMap<>();
        
        List<User> allUsers = userRepository.findAll();
        
        // 按套餐分组统计
        Map<Integer, Long> packageDistribution = allUsers.stream()
                .collect(Collectors.groupingBy(
                    User::getPackageId,
                    Collectors.counting()
                ));
        
        distribution.put("packageDistribution", packageDistribution);
        distribution.put("totalUsers", allUsers.size());
        
        // 计算最受欢迎的套餐
        Integer mostPopularPackage = packageDistribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        
        distribution.put("mostPopularPackage", mostPopularPackage);
        
        return distribution;
    }
}