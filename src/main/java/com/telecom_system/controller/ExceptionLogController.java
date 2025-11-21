package com.telecom_system.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.telecom_system.dto.ClientErrorReport;
import com.telecom_system.service.ExceptionLoggingService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/logs")
public class ExceptionLogController {

    private final ExceptionLoggingService loggingService;

    public ExceptionLogController(ExceptionLoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @PostMapping("/frontend")
    public ResponseEntity<Map<String, Object>> logFrontendError(@RequestBody ClientErrorReport report,
                                                                HttpServletRequest request) {
        String description = report.getAction() != null ? report.getAction() : "前端异常";
        String message = report.getMessage() != null ? report.getMessage() : "(no message)";
        String stack = report.getStack();
        String combined = description + " - " + message + (stack != null ? " | stack:" + stack : "");
        loggingService.logMessage("FRONTEND", combined + " | url:" + request.getRequestURI());
        return ResponseEntity.ok(Map.of("success", true));
    }
}
