package com.library.analytics.dto;

import java.util.List;
import java.util.Map;

public class TransactionAnalyticsDto {
    private long totalTransactions;
    private long activeTransactions;
    private long overdueTransactions;
    private long transactionsToday;
    private long transactionsThisWeek;
    private long transactionsThisMonth;
    private double averageReturnTime;
    private Map<String, Long> transactionsByType;
    private List<DailyTransactionDto> recentActivity;
    
    // Constructors
    public TransactionAnalyticsDto() {}
    
    public TransactionAnalyticsDto(long totalTransactions, long activeTransactions, long overdueTransactions,
                                  long transactionsToday, long transactionsThisWeek, long transactionsThisMonth,
                                  double averageReturnTime, Map<String, Long> transactionsByType,
                                  List<DailyTransactionDto> recentActivity) {
        this.totalTransactions = totalTransactions;
        this.activeTransactions = activeTransactions;
        this.overdueTransactions = overdueTransactions;
        this.transactionsToday = transactionsToday;
        this.transactionsThisWeek = transactionsThisWeek;
        this.transactionsThisMonth = transactionsThisMonth;
        this.averageReturnTime = averageReturnTime;
        this.transactionsByType = transactionsByType;
        this.recentActivity = recentActivity;
    }
    
    // Getters and Setters
    public long getTotalTransactions() {
        return totalTransactions;
    }
    
    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }
    
    public long getActiveTransactions() {
        return activeTransactions;
    }
    
    public void setActiveTransactions(long activeTransactions) {
        this.activeTransactions = activeTransactions;
    }
    
    public long getOverdueTransactions() {
        return overdueTransactions;
    }
    
    public void setOverdueTransactions(long overdueTransactions) {
        this.overdueTransactions = overdueTransactions;
    }
    
    public long getTransactionsToday() {
        return transactionsToday;
    }
    
    public void setTransactionsToday(long transactionsToday) {
        this.transactionsToday = transactionsToday;
    }
    
    public long getTransactionsThisWeek() {
        return transactionsThisWeek;
    }
    
    public void setTransactionsThisWeek(long transactionsThisWeek) {
        this.transactionsThisWeek = transactionsThisWeek;
    }
    
    public long getTransactionsThisMonth() {
        return transactionsThisMonth;
    }
    
    public void setTransactionsThisMonth(long transactionsThisMonth) {
        this.transactionsThisMonth = transactionsThisMonth;
    }
    
    public double getAverageReturnTime() {
        return averageReturnTime;
    }
    
    public void setAverageReturnTime(double averageReturnTime) {
        this.averageReturnTime = averageReturnTime;
    }
    
    public Map<String, Long> getTransactionsByType() {
        return transactionsByType;
    }
    
    public void setTransactionsByType(Map<String, Long> transactionsByType) {
        this.transactionsByType = transactionsByType;
    }
    
    public List<DailyTransactionDto> getRecentActivity() {
        return recentActivity;
    }
    
    public void setRecentActivity(List<DailyTransactionDto> recentActivity) {
        this.recentActivity = recentActivity;
    }
    
    // Helper DTO
    public static class DailyTransactionDto {
        private String date;
        private long borrowings;
        private long returns;
        
        public DailyTransactionDto(String date, long borrowings, long returns) {
            this.date = date;
            this.borrowings = borrowings;
            this.returns = returns;
        }
        
        // Getters and Setters
        public String getDate() {
            return date;
        }
        
        public void setDate(String date) {
            this.date = date;
        }
        
        public long getBorrowings() {
            return borrowings;
        }
        
        public void setBorrowings(long borrowings) {
            this.borrowings = borrowings;
        }
        
        public long getReturns() {
            return returns;
        }
        
        public void setReturns(long returns) {
            this.returns = returns;
        }
    }
}
