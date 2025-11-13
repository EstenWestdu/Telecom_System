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
}