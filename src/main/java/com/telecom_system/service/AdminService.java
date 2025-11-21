package com.telecom_system.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.telecom_system.entity.User;
import com.telecom_system.repository.UserRepository;

@Service
@Transactional
public class AdminService {

    // 注意：UserRepository
    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ========== 普通用户的增删改查 ==========

    /**
     * 查询所有普通用户
     */
    public List<User> findAllByOrderByAccountAsc() {
        return userRepository.findAllByOrderByAccountAsc();
    }

    /**
     * 分页查询（支持自定义排序）
     */
    public Page<User> findAllPaged(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * 根据 id 查询单个普通用户
     */
    public Optional<User> findUserById(Integer id) {
        return userRepository.findById(id);
    }

    /**
     * 创建普通用户
     */
    public User createUser(User user) {
        // 创建用户时不要手动设置主键 account（实体使用 @GeneratedValue）
        // 只校验用户名和手机号等字段；主键由数据库生成，避免 Hibernate 的 stale/unsaved-value 错误
        validateUniqueness(user);
        validateFieldFormats(user);
        return userRepository.save(user);
    }
    /**
     * 唯一性检查
     */
    private void validateUniqueness(User user) {
        
        // 检查用户名是否已存在
        if (userRepository.findByName(user.getName()).isPresent()) {
            throw new RuntimeException("用户名已存在: " + user.getName());
        }
        
        // 检查手机号是否已存在（如果提供了手机号）
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            userRepository.findByPhone(user.getPhone()).ifPresent(existingUser -> {
                throw new RuntimeException("手机号已被使用: " + user.getPhone());
            });
        }
    }
    /**
     * 字段格式验证
     */
    private void validateFieldFormats(User user) {

        // 手机号格式验证
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            String phoneRegex = "^1[3-9]\\d{9}$";
            if (!user.getPhone().matches(phoneRegex)) {
                throw new RuntimeException("手机号格式错误：必须是11位有效手机号");
            }
        }
        
        // 余额验证：不能为负数
        if (user.getBalance() == null || user.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("余额不能为负数");
        }
    }
    /**
     * 更新普通用户信息
     */
    public User updateUser(Integer id, User user) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    // 验证：用户名/手机号不能重复（允许与自己相同）
                    if (user.getName() != null && !user.getName().equals(existingUser.getName())) {
                        userRepository.findByName(user.getName()).ifPresent(u -> {
                            throw new RuntimeException("用户名已存在: " + user.getName());
                        });
                    }

                    if (user.getPhone() != null && !user.getPhone().trim().isEmpty()
                            && !user.getPhone().equals(existingUser.getPhone())) {
                        userRepository.findByPhone(user.getPhone()).ifPresent(u -> {
                            if (!u.getAccount().equals(id)) {
                                throw new RuntimeException("手机号已被使用: " + user.getPhone());
                            }
                        });
                    }

                    // 字段格式验证（更新时允许部分字段为空）
                    validateFieldFormatsForUpdate(user);

                    // 合并更新可变字段到现有实体
                    if (user.getName() != null) existingUser.setName(user.getName());
                    if (user.getPhone() != null) existingUser.setPhone(user.getPhone());
                    if (user.getPackageId() != null) existingUser.setPackageId(user.getPackageId());
                    if (user.getBalance() != null) existingUser.setBalance(user.getBalance());
                    if (user.getPassword() != null) existingUser.setPassword(user.getPassword());

                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
    }

    /**
     * 更新时的字段格式校验：只校验非 null 的字段，不要求 account 出现在请求体中。
     */
    private void validateFieldFormatsForUpdate(User user) {
        // 用户名格式：2-20位，支持中文、英文、数字、下划线
        if (user.getName() != null) {
            String nameRegex = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]{2,20}$";
            if (!user.getName().matches(nameRegex)) {
                throw new RuntimeException("用户名格式错误：2-20位，支持中文、英文、数字、下划线");
            }
        }

        // 手机号格式验证（如果提供了手机号）
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            String phoneRegex = "^1[3-9]\\d{9}$";
            if (!user.getPhone().matches(phoneRegex)) {
                throw new RuntimeException("手机号格式错误：必须是11位有效手机号");
            }
        }

        // 余额验证：不能为负数（如果提供）
        if (user.getBalance() != null) {
            if (user.getBalance().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("余额不能为负数");
            }
        }
    }

    /**
     * 删除普通用户
     */
    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在: " + id);
        }
        userRepository.deleteById(id);
    }

    /**
     * 重置普通用户密码
     */
    public void resetUserPassword(Integer id) {
        userRepository.findById(id)
                .ifPresent(user -> {
                    user.setPassword("default123"); // 重置为默认密码，按需修改
                    userRepository.save(user);
                });
    }
    
}