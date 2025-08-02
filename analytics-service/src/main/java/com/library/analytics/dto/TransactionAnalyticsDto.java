package com.library.analytics.dto;

import java.util.List;

public class TransactionAnalyticsDto {
    private long totalTransactions;
    private long activeTransactions;
    private long completedTransactions;
    private long overdueTransactions;
    private List<Object[]> monthlyStats;
    private List<Object[]> mostBorrowedBooks;
    private List<Object[]> userBorrowingPatterns;
    
    // Constructors
    public TransactionAnalyticsDto() {}
    
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
    
    public long getCompletedTransactions() {
        return completedTransactions;
    }
    
    public void setCompletedTransactions(long completedTransactions) {
        this.completedTransactions = completedTransactions;
    }
    
    public long getOverdueTransactions() {
        return overdueTransactions;
    }
    
    public void setOverdueTransactions(long overdueTransactions) {
        this.overdueTransactions = overdueTransactions;
    }
    
    public List<Object[]> getMonthlyStats() {
        return monthlyStats;
    }
    
    public void setMonthlyStats(List<Object[]> monthlyStats) {
        this.monthlyStats = monthlyStats;
    }
    
    public List<Object[]> getMostBorrowedBooks() {
        return mostBorrowedBooks;
    }
    
    public void setMostBorrowedBooks(List<Object[]> mostBorrowedBooks) {
        this.mostBorrowedBooks = mostBorrowedBooks;
    }
    
    public List<Object[]> getUserBorrowingPatterns() {
        return userBorrowingPatterns;
    }
    
    public void setUserBorrowingPatterns(List<Object[]> userBorrowingPatterns) {
        this.userBorrowingPatterns = userBorrowingPatterns;
    }
}
