package com.telecom_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ExceptionLoggingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionLoggingService.class);

    public void logException(String category, String description, Throwable throwable, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "unknown";

        LOGGER.error("[{}] {} - path: {}", category, description, path, throwable);
    }

    public void logException(String category, String description, Throwable throwable) {
        LOGGER.error("[{}] {}", category, description, throwable);
    }

    public void logMessage(String category, String description) {
        LOGGER.error("[{}] {}", category, description);
    }
}
