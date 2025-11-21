package com.telecom_system.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.telecom_system.entity.User;
import com.telecom_system.service.AdminService;
import com.telecom_system.service.PackageService;
import com.telecom_system.service.StatisticsService;

@Controller
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    private final AdminService adminService;
    private final StatisticsService statisticsService;
    private final PackageService packageService;

    public AdminController(AdminService adminService, StatisticsService statisticsService, PackageService packageService) {
        this.adminService = adminService;
        this.statisticsService = statisticsService;
        this.packageService = packageService;
    }
    
    /**
     * 获取所有普通用户（JSON 接口）
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUser() {
        return ResponseEntity.ok(adminService.findAllByOrderByAccountAsc());
    }

    /**
     * 获取管理员主菜单页面：展示普通用户列表
     */
    @GetMapping("/menu")
    public String getAdminMenu(Model model,
                               @RequestParam(name = "page", defaultValue = "0") int page,
                               @RequestParam(name = "size", defaultValue = "10") int size) {
        Page<User> usersPage = adminService.findAllPaged(PageRequest.of(page, size, Sort.by("account").ascending()));
        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("pageNumber", usersPage.getNumber());
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("totalElements", usersPage.getTotalElements());
        model.addAttribute("pageSize", usersPage.getSize());
        return "admin_menu";
    }

    /**
     * 分页获取用户（前端滑动窗口异步加载使用）
     */
    @GetMapping("/users")
    public ResponseEntity<?> getUsersPaged(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        Page<User> usersPage = adminService.findAllPaged(PageRequest.of(page, size, Sort.by("account").ascending()));
        return ResponseEntity.ok(Map.of(
                "content", usersPage.getContent(),
                "pageNumber", usersPage.getNumber(),
                "totalPages", usersPage.getTotalPages(),
                "totalElements", usersPage.getTotalElements()
        ));
    }
    
    /**
     * 管理员创建普通用户
     */
    @PostMapping("/create-user")
    public ResponseEntity<User> createUser(@RequestBody User userInfo) {
        validatePackage(userInfo.getPackageId());
        validatePhone(userInfo.getPhone());
        validateBalance(userInfo.getBalance());
        User created = adminService.createUser(userInfo);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * 管理员更新普通用户信息
     */
    @PutMapping("/modify-{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id,
                                           @RequestBody User userInfo) {
        validatePhone(userInfo.getPhone());
        validateBalance(userInfo.getBalance());
        validatePackage(userInfo.getPackageId());
        User updated = adminService.updateUser(id, userInfo);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * 管理员删除普通用户
     */
    @DeleteMapping("/delete-{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "用户删除成功"));
    }

    private void validatePhone(String phone) {
        if (phone == null || !phone.matches("^[0-9]{11}$")) {
            throw new IllegalArgumentException("手机号必须为11位数字");
        }
    }

    private void validateBalance(java.math.BigDecimal balance) {
        if (balance == null) {
            throw new IllegalArgumentException("余额不能为空");
        }
        if (balance.doubleValue() < 0) {
            throw new IllegalArgumentException("余额不能为负数");
        }
    }

    private void validatePackage(Integer packageId) {
        if (packageId == null || packageService.findPackageById(packageId).isEmpty()) {
            throw new IllegalArgumentException("套餐ID不存在");
        }
    }
    
    /**
     * 管理员重置普通用户密码
     */
    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetUserPassword(@PathVariable Integer id) {
        adminService.resetUserPassword(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "密码重置成功"));
    }
    /**
     * 获取用户流量使用统计数据
     */
    @GetMapping("/traffic-stats")
    public ResponseEntity<?> getTrafficStats(Model model) {
        List<Map<String,Object>> stats = statisticsService.getHourlyOnlineUserStatistics();
        model.addAttribute("stats", stats);
        return ResponseEntity.ok(stats);
    }
} 