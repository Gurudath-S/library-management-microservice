package com.library.analytics.dto;

import java.time.LocalDateTime;

public class AnalyticsDashboardDto {
    private UserAnalyticsDto userAnalytics;
    private BookAnalyticsDto bookAnalytics;
    private TransactionAnalyticsDto transactionAnalytics;
    private InventoryAnalyticsDto inventoryAnalytics;
    private SystemHealthDto systemHealth;
    private LocalDateTime generatedAt;
    
    public AnalyticsDashboardDto() {
        this.generatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public UserAnalyticsDto getUserAnalytics() {
        return userAnalytics;
    }
    
    public void setUserAnalytics(UserAnalyticsDto userAnalytics) {
        this.userAnalytics = userAnalytics;
    }
    
    public BookAnalyticsDto getBookAnalytics() {
        return bookAnalytics;
    }
    
    public void setBookAnalytics(BookAnalyticsDto bookAnalytics) {
        this.bookAnalytics = bookAnalytics;
    }
    
    public TransactionAnalyticsDto getTransactionAnalytics() {
        return transactionAnalytics;
    }
    
    public void setTransactionAnalytics(TransactionAnalyticsDto transactionAnalytics) {
        this.transactionAnalytics = transactionAnalytics;
    }
    
    public InventoryAnalyticsDto getInventoryAnalytics() {
        return inventoryAnalytics;
    }
    
    public void setInventoryAnalytics(InventoryAnalyticsDto inventoryAnalytics) {
        this.inventoryAnalytics = inventoryAnalytics;
    }
    
    public SystemHealthDto getSystemHealth() {
        return systemHealth;
    }
    
    public void setSystemHealth(SystemHealthDto systemHealth) {
        this.systemHealth = systemHealth;
    }
    
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
