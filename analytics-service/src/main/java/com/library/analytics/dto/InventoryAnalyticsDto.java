package com.library.analytics.dto;

import java.util.List;
import java.util.Map;

public class InventoryAnalyticsDto {
    private long totalCopies;
    private long availableCopies;
    private long borrowedCopies;
    private double utilizationRate;
    private List<String> lowStockBooks;
    private List<String> highDemandBooks;
    private Map<String, Double> categoryUtilization;
    
    // Constructors
    public InventoryAnalyticsDto() {}
    
    public InventoryAnalyticsDto(long totalCopies, long availableCopies, long borrowedCopies,
                                double utilizationRate, List<String> lowStockBooks,
                                List<String> highDemandBooks, Map<String, Double> categoryUtilization) {
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.borrowedCopies = borrowedCopies;
        this.utilizationRate = utilizationRate;
        this.lowStockBooks = lowStockBooks;
        this.highDemandBooks = highDemandBooks;
        this.categoryUtilization = categoryUtilization;
    }
    
    // Getters and Setters
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
    
    public List<String> getLowStockBooks() {
        return lowStockBooks;
    }
    
    public void setLowStockBooks(List<String> lowStockBooks) {
        this.lowStockBooks = lowStockBooks;
    }
    
    public List<String> getHighDemandBooks() {
        return highDemandBooks;
    }
    
    public void setHighDemandBooks(List<String> highDemandBooks) {
        this.highDemandBooks = highDemandBooks;
    }
    
    public Map<String, Double> getCategoryUtilization() {
        return categoryUtilization;
    }
    
    public void setCategoryUtilization(Map<String, Double> categoryUtilization) {
        this.categoryUtilization = categoryUtilization;
    }
}
