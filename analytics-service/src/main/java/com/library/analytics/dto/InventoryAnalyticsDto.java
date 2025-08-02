package com.library.analytics.dto;

public class InventoryAnalyticsDto {
    private long totalBooks;
    private long totalCopies;
    private long availableCopies;
    private long borrowedCopies;
    private double utilizationRate;
    private long lowStockCount;
    private long outOfStockCount;
    
    // Constructors
    public InventoryAnalyticsDto() {}
    
    // Getters and Setters
    public long getTotalBooks() {
        return totalBooks;
    }
    
    public void setTotalBooks(long totalBooks) {
        this.totalBooks = totalBooks;
    }
    
    public long getTotalCopies() {
        return totalCopies;
    }
    
    public void setTotalCopies(long totalCopies) {
        this.totalCopies = totalCopies;
    }
    
    public long getAvailableCopies() {
        return availableCopies;
    }
    
    public void setAvailableCopies(long availableCopies) {
        this.availableCopies = availableCopies;
    }
    
    public long getBorrowedCopies() {
        return borrowedCopies;
    }
    
    public void setBorrowedCopies(long borrowedCopies) {
        this.borrowedCopies = borrowedCopies;
    }
    
    public double getUtilizationRate() {
        return utilizationRate;
    }
    
    public void setUtilizationRate(double utilizationRate) {
        this.utilizationRate = utilizationRate;
    }
    
    public long getLowStockCount() {
        return lowStockCount;
    }
    
    public void setLowStockCount(long lowStockCount) {
        this.lowStockCount = lowStockCount;
    }
    
    public long getOutOfStockCount() {
        return outOfStockCount;
    }
    
    public void setOutOfStockCount(long outOfStockCount) {
        this.outOfStockCount = outOfStockCount;
    }
}
