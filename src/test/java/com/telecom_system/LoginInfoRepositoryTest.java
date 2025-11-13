package com.telecom_system;

import com.telecom_system.entity.LoginInfo;
import com.telecom_system.entity.User;
import com.telecom_system.repository.LoginInfoRepository;
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

    // Helper: 创建示例用户和登录信息
    private void createSampleData() {
        // user 200001 with one login (logout_time null)
        if (!userRepository.existsById(200001)) {
            User u1 = new User();
            u1.setAccount(200001);
            u1.setName("测试用户1");
            u1.setPassword("pass123");
            u1.setBalance(new java.math.BigDecimal("100.00"));
            u1.setPackageId(1);
            u1.setPhone("13800138001");
            userRepository.save(u1);
        }

        if (loginInfoRepository.findByIdAccountId(200001).isEmpty()) {
            LoginInfo.LoginInfoPK pk = new LoginInfo.LoginInfoPK();
            pk.setAccountId(200001);
            pk.setLoginTime(LocalDateTime.now().minusHours(1));
            LoginInfo li = new LoginInfo();
            li.setId(pk);
            // leave logoutTime null to represent online user
            loginInfoRepository.save(li);
        }
    }
    
    @Autowired
    private LoginInfoRepository loginInfoRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Test
    void testFindByIdAccountId() {
        // 测试根据用户ID查找登录记录
        createSampleData();
        System.out.println("-- testFindByIdAccountId 开始 --");
        long total = loginInfoRepository.count();
        System.out.println("login_info 总记录数: " + total);
        List<LoginInfo> results = loginInfoRepository.findByIdAccountId(200001);
        System.out.println("查询 accountId=200001 的结果数: " + results.size());
        results.forEach(li -> System.out.println(prettyLoginInfo(li)));
        assertNotNull(results);
        System.out.println("-- testFindByIdAccountId 结束 --\n");
    }
    
    @Test
    void testFindByLogoutTimeIsNull() {
        // 测试查找在线用户
        createSampleData();
        System.out.println("-- testFindByLogoutTimeIsNull 开始 --");
        List<LoginInfo> onlineUsers = loginInfoRepository.findByLogoutTimeIsNull();
        System.out.println("在线用户数量 (logout_time is null): " + onlineUsers.size());
        onlineUsers.forEach(li -> System.out.println(prettyLoginInfo(li)));
        assertNotNull(onlineUsers);
        System.out.println("-- testFindByLogoutTimeIsNull 结束 --\n");
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
        
        System.out.println("-- testSaveAndFindLoginInfo 开始 --");
        System.out.println("保存前 login_info 数: " + loginInfoRepository.count());
        System.out.println("保存前 user_info 数: " + userRepository.count());

        // 先创建并保存一个对应的用户，避免外键约束失败
        User u = new User();
        u.setAccount(999999);
        u.setName("test_user");
        u.setPassword("password");
        u.setBalance(new java.math.BigDecimal("0.00"));
        u.setPackageId(1);
        u.setPhone("0000000000");
        User savedUser = userRepository.save(u);
        System.out.println("已保存用户: " + prettyUser(savedUser));

        // 保存登录信息
        LoginInfo saved = loginInfoRepository.save(loginInfo);
        System.out.println("已保存登录信息: " + prettyLoginInfo(saved));
        assertNotNull(saved);
        
        // 查询验证
        List<LoginInfo> results = loginInfoRepository.findByIdAccountId(999999);
        System.out.println("查询 accountId=999999 的结果数: " + results.size());
        results.forEach(li -> System.out.println(prettyLoginInfo(li)));
        assertFalse(results.isEmpty());
        assertEquals(999999, results.get(0).getAccountId());
        System.out.println("-- testSaveAndFindLoginInfo 结束 --\n");
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