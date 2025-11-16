package com.telecom_system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.telecom_system.service.LoginService;
import com.telecom_system.service.LoginInfoService;
import com.telecom_system.entity.User;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/login")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final LoginService loginService;
    private final LoginInfoService loginInfoService;

    public AuthController(LoginService loginService, LoginInfoService loginInfoService) {
        this.loginService = loginService;
        this.loginInfoService = loginInfoService;
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/user")
    public String userLogin(@RequestParam String identifier, 
                            @RequestParam String password,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        return loginService.userLogin(identifier, password)
                .map(user -> {
                    // 记录登录时间
                    try {
                        loginInfoService.recordLogin(user.getAccount());
                    } catch (Exception e) {
                        // 记录失败不应阻塞登录
                        e.printStackTrace();
                    }
                    session.setAttribute("user",user);
                    return "redirect:/user/menu";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "用户名或密码错误");
                    return "redirect:/login";
                });
    }
    
    /**
     * 管理员登录
     */
    @PostMapping("/admin")
    public String adminLogin(@RequestParam String identifier, 
                                       @RequestParam String password,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        return loginService.adminLogin(identifier, password)
                .map(admin -> {
                    session.setAttribute("admin", admin);
                    return "redirect:/admin/menu";
                })
                .orElseGet(()->{
                    redirectAttributes.addFlashAttribute("error", "管理员用户名或密码错误");
                    return "redirect:/login";
                });
    }
    
   
    
    /**
     * 退出登录（记录登出时间）
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, org.springframework.ui.Model model) {
        Object userObj = session.getAttribute("user");
        Object adminObj = session.getAttribute("admin");
        String userName = null;

        if (userObj instanceof User) {
            User u = (User) userObj;
            Integer accountId = u.getAccount();
            userName = u.getName();
            try {
                loginInfoService.recordLogout(accountId);
            } catch (Exception e) {
                // 记录登出失败：打印日志但继续登出流程
                e.printStackTrace();
            }
        } else if (adminObj instanceof com.telecom_system.entity.Admin) {
            // 管理员仅显示名字，不记录上线/下线
            userName = ((com.telecom_system.entity.Admin) adminObj).getName();
        }

        // 在失效 session 之前把需要显示的数据放入 model
        model.addAttribute("userName", userName);
        session.invalidate();
        return "logout";
    }
}