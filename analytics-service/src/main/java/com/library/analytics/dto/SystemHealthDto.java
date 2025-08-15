package com.library.analytics.dto;

import java.util.List;
import java.util.Map;

public class SystemHealthDto {
    private String status;
    private double responseTime;
    private long uptime;
    private Map<String, String> moduleStatus;
    private List<String> recentErrors;
    
    // Constructors
    public SystemHealthDto() {}
    
    public SystemHealthDto(String status, double responseTime, long uptime,
                          Map<String, String> moduleStatus, List<String> recentErrors) {
        this.status = status;
        this.responseTime = responseTime;
        this.uptime = uptime;
        this.moduleStatus = moduleStatus;
        this.recentErrors = recentErrors;
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public double getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(double responseTime) {
        this.responseTime = responseTime;
    }
    
    public long getUptime() {
        return uptime;
    }
    
    public void setUptime(long uptime) {
        this.uptime = uptime;
    }
    
    public Map<String, String> getModuleStatus() {
        return moduleStatus;
    }
    
    public void setModuleStatus(Map<String, String> moduleStatus) {
        this.moduleStatus = moduleStatus;
    }
    
    public List<String> getRecentErrors() {
        return recentErrors;
    }
    
    public void setRecentErrors(List<String> recentErrors) {
        this.recentErrors = recentErrors;
    }
}
