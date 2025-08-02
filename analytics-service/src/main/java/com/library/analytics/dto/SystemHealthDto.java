package com.library.analytics.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class SystemHealthDto {
    private String status;
    private Map<String, String> serviceStatus;
    private Map<String, Object> performanceMetrics;
    private LocalDateTime lastChecked;
    
    // Constructors
    public SystemHealthDto() {
        this.lastChecked = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Map<String, String> getServiceStatus() {
        return serviceStatus;
    }
    
    public void setServiceStatus(Map<String, String> serviceStatus) {
        this.serviceStatus = serviceStatus;
    }
    
    public Map<String, Object> getPerformanceMetrics() {
        return performanceMetrics;
    }
    
    public void setPerformanceMetrics(Map<String, Object> performanceMetrics) {
        this.performanceMetrics = performanceMetrics;
    }
    
    public LocalDateTime getLastChecked() {
        return lastChecked;
    }
    
    public void setLastChecked(LocalDateTime lastChecked) {
        this.lastChecked = lastChecked;
    }
}
