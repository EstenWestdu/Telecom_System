package com.telecom_system.repository;

import com.telecom_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StatisticsRepository extends JpaRepository<User, Integer> {

    // 系统统计信息（原生 SQL）
    @Query(value = "SELECT COUNT(u.account) AS total_users, SUM(u.balance) AS total_balance, AVG(u.balance) AS average_balance, COUNT(DISTINCT u.package_id) AS active_packages FROM user_info u", nativeQuery = true)
    Object[] getSystemStatistics();

    // 套餐使用统计（原生 SQL）
    @Query(value = "SELECT p.id, p.duration, p.cost, COUNT(u.account) AS user_count " +
           "FROM package_info p " +
           "LEFT JOIN user_info u ON p.id::text = u.package_id " +
           "GROUP BY p.id, p.duration, p.cost " +
           "ORDER BY user_count DESC", nativeQuery = true)
    List<Object[]> getPackageUsageStatistics();

    // 用户活跃度统计（原生 SQL）
    @Query(value = "SELECT u.account, u.name, COUNT(li.account_id) AS login_count, SUM(EXTRACT(EPOCH FROM (li.logout_time - li.login_time)) / 3600) AS total_hours " +
           "FROM user_info u LEFT JOIN login_info li ON u.account = li.account_id " +
           "WHERE li.logout_time IS NOT NULL " +
           "GROUP BY u.account, u.name " +
           "ORDER BY total_hours DESC", nativeQuery = true)
    List<Object[]> getUserActivityStatistics();

    /**
     * 统计一天 24 个小时每个小时的在线用户数量
     * 
     * 结果为 24 条记录，每条包含：
     * - hour: 小时数 (0-23)
     * - online_user_count: 该小时的在线用户数
     * 
     * 统计逻辑：如果登录时间的小时数 <= 当前小时 < 登出时间的小时数，则视为该小时在线。
     * 当登出时间为 null（未登出）的用户，如果登录时间的小时数 <= 当前小时，则视为该小时在线。
     */
    @Query(value = "SELECT " +
           "  hour_series.hour, " +
           "  COUNT(DISTINCT li.account_id) AS online_user_count " +
           "FROM " +
           "  (SELECT 0 AS hour UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 " +
           "   UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 " +
           "   UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 " +
           "   UNION SELECT 18 UNION SELECT 19 UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23) AS hour_series " +
           "LEFT JOIN login_info li ON " +
           "  (EXTRACT(HOUR FROM li.login_time) <= hour_series.hour) AND " +
           "  (li.logout_time IS NULL OR EXTRACT(HOUR FROM li.logout_time) > hour_series.hour) " +
           "GROUP BY hour_series.hour " +
           "ORDER BY hour_series.hour", nativeQuery = true)
    List<Object[]> getHourlyOnlineUserStatistics();
}