package com.telecom_system.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    @Cacheable(value = "users", key = "'all'")
    public List<User> findAllByOrderByAccountAsc() { 
        List<User> users = userRepository.findAllByOrderByAccountAsc();
        return users;
    }

    /**
     * 分页查询添加缓存
     */
    @Cacheable(value = "user_pages", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public com.telecom_system.dto.PageResult<User> findAllPaged(Pageable pageable) {
        
        Page<User> users = userRepository.findAll(pageable);
        com.telecom_system.dto.PageResult<User> result = new com.telecom_system.dto.PageResult<>();
        result.setContent(users.getContent());
        result.setTotalElements(users.getTotalElements());
        result.setTotalPages(users.getTotalPages());
        result.setPage(users.getNumber());
        result.setSize(users.getSize());
        result.setFirst(users.isFirst());
        result.setLast(users.isLast());
        return result;
    }

    /**
     * 根据 id 查询单个普通用户
     */
    @Cacheable(value = "users", key = "#id", unless = "#result == null")
    public User findUserById(Integer id) {
        // .orElse(null) 配合 unless 确保只缓存存在的用户
        return userRepository.findById(id).orElse(null);
    }

    /**
     * 创建普通用户
     * 改进：需要清理分页和搜索缓存，因为总数和列表已变
     */
    @CacheEvict(value = {"users", "user_pages", "search_users"}, allEntries = true)
    public User createUser(User user) {
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
    @CacheEvict(value = {"users", "user_pages", "search_users"}, allEntries = true)
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
                    if (user.getPackageId() != null) {
                        if (!user.getPackageId().equals(existingUser.getPackageId())) {
                            // 套餐发生变化，更新套餐生效时间为当前时间
                            existingUser.setPackageStartTime(LocalDateTime.now());
                        }
                        existingUser.setPackageId(user.getPackageId());
                    }
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
    @CacheEvict(value = {"users", "user_pages", "search_users"}, allEntries = true)
    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在: " + id);
        }
        userRepository.deleteById(id);
    }

    /**
     * 根据ID或姓名搜索用户（添加缓存）
     * key 使用 #query，例如搜索 "200" 会生成缓存 key: search_users::200
     */
    @Cacheable(value = "search_users", key = "#query", unless = "#result == null || #result.isEmpty()")
    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        String trimmedQuery = query.trim();
        if (trimmedQuery.matches("\\d+")) {
            // 全数字，按ID前缀搜索
            return userRepository.findByAccountStartingWith(trimmedQuery);
        } else {
            // 按姓名搜索
            return userRepository.findByNameContaining(trimmedQuery);
        }
    }
    
    /**
     * 重置普通用户密码
     */
    @CacheEvict(value = {"users", "user_pages", "search_users"}, key = "#id")
    public void resetUserPassword(Integer id) {
        userRepository.findById(id)
                .ifPresent(user -> {
                    user.setPassword("default123"); // 重置为默认密码，按需修改
                    userRepository.save(user);
                });
    }
    
}