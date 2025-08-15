package com.library.analytics.dto;

import java.util.List;
import java.util.Map;

public class UserAnalyticsDto {
    private long totalUsers;
    private long activeUsers;
    private long newUsersThisMonth;
    private double userGrowthRate;
    private Map<String, Long> usersByRole;
    private List<Object[]> userGrowthStats;
    private List<Object[]> topBorrowers;
    private List<UserActivityDto> topActiveUsers;
    
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
    
    public double getUserGrowthRate() {
        return userGrowthRate;
    }
    
    public void setUserGrowthRate(double userGrowthRate) {
        this.userGrowthRate = userGrowthRate;
    }
    
    public Map<String, Long> getUsersByRole() {
        return usersByRole;
    }
    
    public void setUsersByRole(Map<String, Long> usersByRole) {
        this.usersByRole = usersByRole;
    }
    
    public List<UserActivityDto> getTopActiveUsers() {
        return topActiveUsers;
    }
    
    public void setTopActiveUsers(List<UserActivityDto> topActiveUsers) {
        this.topActiveUsers = topActiveUsers;
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

    public static class UserActivityDto {
        private String username;
        private String email;
        private long totalTransactions;
        private long activeTransactions;

        public UserActivityDto(String username, String email, long totalTransactions, long activeTransactions) {
            this.username = username;
            this.email = email;
            this.totalTransactions = totalTransactions;
            this.activeTransactions = activeTransactions;
        }

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public long getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }

        public long getActiveTransactions() { return activeTransactions; }
        public void setActiveTransactions(long activeTransactions) { this.activeTransactions = activeTransactions; }
    }
}
