package com.telecom_system;

import com.telecom_system.entity.LoginInfo;
import com.telecom_system.entity.User;
import com.telecom_system.repository.LoginInfoRepository;
import com.telecom_system.repository.StatisticsRepository;
import com.telecom_system.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional  // 测试完成后回滚数据
class LoginInfoRepositoryTest {

    
    @Autowired
    private LoginInfoRepository loginInfoRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StatisticsRepository statisticsRepository;

    @Test 
    void testHourlyOnlineUserStatistics() {
        // 测试按小时统计在线用户数量
        System.out.println("-- testHourlyOnlineUserStatistics begin --");
        List<Object[]> stats = statisticsRepository.getHourlyOnlineUserStatistics();
        System.out.println("Hourly Online User Statistics:");
        for (Object[] record : stats) {
            Integer hour = (Integer) record[0];
            Long onlineUserCount = ((Number) record[1]).longValue();
            System.out.printf("Hour: %02d, Online Users: %d%n", hour, onlineUserCount);
        }
        System.out.println("-- testHourlyOnlineUserStatistics over --\n");
        
    }

    @Test
    void testFindByIdAccountId() {
        // 测试根据用户ID查找登录记录
        System.out.println("-- testFindByIdAccountId begin --");
        long total = loginInfoRepository.count();
        System.out.println("login_info all_records: " + total);
        List<LoginInfo> results = loginInfoRepository.findByIdAccountId(200001);
        System.out.println("select accountId=200001 results: " + results.size());
        results.forEach(li -> System.out.println(prettyLoginInfo(li)));
        assertNotNull(results);
        System.out.println("-- testFindByIdAccountId over --\n");
    }
    
    @Test
    void testFindByLogoutTimeIsNull() {
        // 测试查找在线用户
        System.out.println("-- testFindByLogoutTimeIsNull begin --");
        List<LoginInfo> onlineUsers = loginInfoRepository.findByLogoutTimeIsNull();
        System.out.println("login_in uesrs (logout_time is null): " + onlineUsers.size());
        onlineUsers.forEach(li -> System.out.println(prettyLoginInfo(li)));
        assertNotNull(onlineUsers);
        System.out.println("-- testFindByLogoutTimeIsNull over --\n");
    }
    
    

    @Test
    void testSaveAndFindLoginInfo() {
        // 测试保存和查询登录信息
        LoginInfo.LoginInfoPK pk = new LoginInfo.LoginInfoPK();
        pk.setAccountId(999999);
        pk.setLoginTime(LocalDateTime.now());
        
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setId(pk);
        loginInfo.setLogoutTime(LocalDateTime.now().plusHours(1));
        
        System.out.println("-- testSaveAndFindLoginInfo begin --");
        System.out.println("store before login_info nums: " + loginInfoRepository.count());
        System.out.println("store after user_info nums: " + userRepository.count());

        // 先创建并保存一个对应的用户，避免外键约束失败
        User u = new User();
        u.setName("test_user");
        u.setPassword("password");
        u.setBalance(new java.math.BigDecimal("0.00"));
        u.setPackageId(1);
        u.setPhone("0000000000");
        User savedUser = userRepository.save(u);
        System.out.println("have saved user: " + prettyUser(savedUser));

        // 保存登录信息
        LoginInfo saved = loginInfoRepository.save(loginInfo);
        System.out.println("have saved login_info: " + prettyLoginInfo(saved));
        assertNotNull(saved);
        
        // 查询验证
        List<LoginInfo> results = loginInfoRepository.findByIdAccountId(999999);
        System.out.println("select accountId=999999 results: " + results.size());
        results.forEach(li -> System.out.println(prettyLoginInfo(li)));
        assertFalse(results.isEmpty());
        assertEquals(999999, results.get(0).getAccountId());
        System.out.println("-- testSaveAndFindLoginInfo over --\n");
    }

    // 辅助打印方法
    private String prettyLoginInfo(LoginInfo li) {
        if (li == null) return "null";
        return String.format("LoginInfo{accountId=%s, loginTime=%s, logoutTime=%s}",
                li.getAccountId(), li.getLoginTime(), li.getLogoutTime());
    }

    private String prettyUser(User u) {
        if (u == null) return "null";
        return String.format("User{account=%s, name=%s, packageId=%s, balance=%s}",
                u.getAccount(), u.getName(), u.getPackageId(), u.getBalance());
    }
}