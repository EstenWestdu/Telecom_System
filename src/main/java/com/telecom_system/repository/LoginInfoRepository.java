package com.telecom_system.repository;

import com.telecom_system.entity.LoginInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginInfoRepository extends JpaRepository<LoginInfo, LoginInfo.LoginInfoPK> {
    
    // 根据用户ID查找所有登录记录
    List<LoginInfo> findByIdAccountId(Integer accountId);
    
    // 查找指定时间范围内的登录记录
    List<LoginInfo> findByIdLoginTimeBetween(LocalDateTime start, LocalDateTime end);
    
    // 查找未下线的会话（logout_time为null）
    List<LoginInfo> findByLogoutTimeIsNull();
    
    // 查找指定用户的未下线会话
    List<LoginInfo> findByIdAccountIdAndLogoutTimeIsNull(Integer accountId);
    
    // 查找最近24小时的活跃用户
    @Query("SELECT li FROM LoginInfo li WHERE li.id.loginTime >= :sinceTime")
    List<LoginInfo> findRecentLogins(@Param("sinceTime") LocalDateTime sinceTime);
    
    // 统计用户的总在线时长（小时） — 使用原生 SQL（PostgreSQL）以避免 JPQL/HQL 对 EXTRACT 的校验问题
    @Query(value = "SELECT account_id, SUM(EXTRACT(EPOCH FROM (logout_time - login_time)) / 3600) " +
           "FROM login_info " +
           "WHERE logout_time IS NOT NULL AND account_id = :accountId " +
           "GROUP BY account_id", nativeQuery = true)
    Object[] calculateTotalOnlineHours(@Param("accountId") Integer accountId);
    
    // 查找在线时长超过阈值的会话 — 使用原生 SQL 返回实体
    @Query(value = "SELECT * FROM login_info li " +
           "WHERE li.logout_time IS NOT NULL " +
           "AND (EXTRACT(EPOCH FROM (li.logout_time - li.login_time)) / 3600) > :hoursThreshold", nativeQuery = true)
    List<LoginInfo> findLongSessions(@Param("hoursThreshold") Double hoursThreshold);
    
    // 自定义查询：查找用户的最后登录时间
    @Query("SELECT MAX(li.id.loginTime) FROM LoginInfo li WHERE li.id.accountId = :accountId")
    LocalDateTime findLastLoginTimeByAccountId(@Param("accountId") Integer accountId);
}
