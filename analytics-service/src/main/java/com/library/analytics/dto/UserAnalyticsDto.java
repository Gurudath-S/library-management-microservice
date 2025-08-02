package com.library.analytics.dto;

import java.util.List;
import java.util.Map;

public class UserAnalyticsDto {
    private long totalUsers;
    private long activeUsers;
    private long newUsersThisMonth;
    private Map<String, Long> usersByRole;
    private List<Object[]> userGrowthStats;
    private List<Object[]> topBorrowers;
    
    // Constructors
    public UserAnalyticsDto() {}
    
    // Getters and Setters
    public long getTotalUsers() {
        return totalUsers;
    }
    
    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }
    
    public long getActiveUsers() {
        return activeUsers;
    }
    
    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }
    
    public long getNewUsersThisMonth() {
        return newUsersThisMonth;
    }
    
    public void setNewUsersThisMonth(long newUsersThisMonth) {
        this.newUsersThisMonth = newUsersThisMonth;
    }
    
    public Map<String, Long> getUsersByRole() {
        return usersByRole;
    }
    
    public void setUsersByRole(Map<String, Long> usersByRole) {
        this.usersByRole = usersByRole;
    }
    
    public List<Object[]> getUserGrowthStats() {
        return userGrowthStats;
    }
    
    public void setUserGrowthStats(List<Object[]> userGrowthStats) {
        this.userGrowthStats = userGrowthStats;
    }
    
    public List<Object[]> getTopBorrowers() {
        return topBorrowers;
    }
    
    public void setTopBorrowers(List<Object[]> topBorrowers) {
        this.topBorrowers = topBorrowers;
    }
}
