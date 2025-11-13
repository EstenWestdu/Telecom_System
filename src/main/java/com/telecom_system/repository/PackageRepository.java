package com.telecom_system.repository;

import com.telecom_system.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PackageRepository extends JpaRepository<Package, Integer> {
    
    // 根据价格范围查找套餐
    List<Package> findByCostBetween(Double minCost, Double maxCost);
    
    // 查找价格低于指定值的套餐
    List<Package> findByCostLessThanEqual(Double maxCost);
    
    // 根据时长查找套餐（模糊匹配）
    List<Package> findByDurationContaining(String durationKeyword);
    
    // 自定义查询：查找最受欢迎的套餐（使用人数最多的）
    // 使用原生 SQL：在实体字段类型不一致（Package.id 为 Integer，User.packageId 为 String）时
    // 使用原生查询并在数据库层处理类型/连接。
    @Query(value = "SELECT p.*, COUNT(u.account) AS user_count " +
        "FROM package_info p " +
        "LEFT JOIN user_info u ON p.id = u.package_id " +
        "GROUP BY p.id " +
        "ORDER BY user_count DESC", nativeQuery = true)
    List<Object[]> findPopularPackages();
    
}