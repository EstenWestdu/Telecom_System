package com.telecom_system.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.telecom_system.entity.User;
import com.telecom_system.repository.UserRepository;

@Service
@Transactional
public class AdminService {

    // 注意：这里改成操作 UserRepository，而不是 AdminRepository
    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ========== 普通用户的增删改查 ==========

    /**
     * 查询所有普通用户
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
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
        // 根据实际业务做唯一性校验，比如账号/手机号/邮箱等
        // 示例：假设 User 有 username 字段，且仓库有 existsByUsername
        // if (userRepository.existsByUsername(user.getUsername())) {
        //     throw new RuntimeException("用户名已存在: " + user.getUsername());
        // }

        return userRepository.save(user);
    }

    /**
     * 更新普通用户信息
     */
    public User updateUser(Integer id, User user) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    // 只更新允许修改的字段
                    if (user.getName() != null) {
                        existingUser.setName(user.getName());
                    }
                    if (user.getPhone() != null) {
                        existingUser.setPhone(user.getPhone());
                    }
                    if (user.getRole() != null) {
                        existingUser.setRole(user.getRole());
                    }
                    if (user.getPackageId() != null) {
                        existingUser.setPackageId(user.getPackageId());
                    }
                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
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