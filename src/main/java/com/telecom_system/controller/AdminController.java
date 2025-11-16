package com.telecom_system.controller;

import java.util.List;
import java.util.Map;

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
import com.telecom_system.service.StatisticsService;

@Controller
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    private final AdminService adminService;
    private final StatisticsService statisticsService;
    
    public AdminController(AdminService adminService,StatisticsService statisticsService) {
        this.adminService = adminService;
        this.statisticsService = statisticsService;
    }
    
    /**
     * 获取所有普通用户（JSON 接口）
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUser() {
        return ResponseEntity.ok(adminService.findAllUsers());
    }

    /**
     * 获取管理员主菜单页面：展示普通用户列表
     */
    @GetMapping("/menu")
    public String getAdminMenu(Model model) {
        List<User> users = adminService.findAllUsers();
        // 页面里用 ${users} 遍历
        model.addAttribute("users", users);
        return "admin_menu";
    }
    
    /**
     * 管理员创建普通用户
     */
    @PostMapping("/create-user")
    public ResponseEntity<User> createUser(@RequestBody User userInfo) {
        return ResponseEntity.ok(adminService.createUser(userInfo));
    }
    
    /**
     * 管理员更新普通用户信息
     */
    @PutMapping("/modify-{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id,
                                           @RequestBody User userInfo) {
        return ResponseEntity.ok(adminService.updateUser(id, userInfo));
    }
    
    /**
     * 管理员删除普通用户
     */
    @DeleteMapping("/delete-{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "用户删除成功"));
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
        List<Map<String,Object>> stats = statisticsService.getUserActivityStatistics();
        model.addAttribute("stats", stats);
        return ResponseEntity.ok(stats);
    }
} 