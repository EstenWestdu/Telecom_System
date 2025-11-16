package com.telecom_system.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
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
import com.telecom_system.service.UserService;
import com.telecom_system.service.PackageService;
import jakarta.servlet.http.HttpSession;
import com.telecom_system.entity.Package;
@Controller
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    private final PackageService packageService;
    public UserController(UserService userService, PackageService packageService) {
        this.userService = userService;
        this.packageService = packageService;
    }
    
    /**
     * 获取所有用户列表
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }
    
    /**
     * 获取用户主菜单
     */
    @GetMapping("/menu")
    public String getAdminMenu(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        List<Package> packages = packageService.findAllPackages();
        model.addAttribute("user", user);
        model.addAttribute("packages", packages);
        return "user_menu";
    }

    /**
     * 分页查询用户
     */
    @GetMapping("/page")
    public ResponseEntity<Page<User>> getUsersByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.findUsersByPage(page, size));
    }
    
    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/{account}")
    public ResponseEntity<?> getUserById(@PathVariable Integer account) {
        Optional<User> user = userService.findUserById(account);
        return user.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 创建新用户
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User userInfo) {
        return ResponseEntity.ok(userService.createUser(userInfo));
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{account}")
    public ResponseEntity<User> updateUser(@PathVariable Integer account, 
                                              @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(account, user));
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{account}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer account) {
        userService.deleteUser(account);
        return ResponseEntity.ok(Map.of("success", true, "message", "用户删除成功"));
    }
    
    /**
     * 用户充值
     */
    @PostMapping("/{account}/recharge")
    public ResponseEntity<?> recharge(@PathVariable Integer account, 
                                     @RequestParam Double amount) {
        User user = userService.recharge(account, amount);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "充值成功",
            "newBalance", user.getBalance()
        ));
    }
    
    /**
     * 用户消费/扣费
     */
    @PostMapping("/{account}/deduct")
    public ResponseEntity<?> deductBalance(@PathVariable Integer account, 
                                          @RequestParam Double amount) {
        User user = userService.deductBalance(account, amount);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "扣费成功", 
            "newBalance", user.getBalance()
        ));
    }
    
    /**
     * 更改用户套餐
     */
    @PostMapping("/{account}/change-package")
    public ResponseEntity<?> changePackage(@PathVariable Integer account, 
                                          @RequestParam Integer packageId) {
        User user = userService.changePackage(account, packageId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "套餐变更成功",
            "newPackage", user.getPackageId()
        ));
    }
    
    /**
     * 查询用户剩余时长
     */
    @GetMapping("/{account}/remaining-time")
    public ResponseEntity<?> getRemainingTime(@PathVariable Integer account) {
        Map<String, Object> remainingInfo = userService.getRemainingTime(account);
        return ResponseEntity.ok(remainingInfo);
    }
    
    /**
     * 根据条件搜索用户
     */
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String role) {
        return ResponseEntity.ok(userService.searchUsers(name, phone));
    }
}