package com.library.analytics.dto;

import java.util.List;

public class BookAnalyticsDto {
    private long totalBooks;
    private long availableBooks;
    private long totalCopies;
    private long totalAvailableCopies;
    private List<Object[]> booksByCategory;
    private List<Object[]> lowStockBooks;
    private List<Object[]> popularBooks;
    private List<Object[]> recentlyAddedBooks;
    
    // Constructors
    public BookAnalyticsDto() {}
    
    // Getters and Setters
    public long getTotalBooks() {
        return totalBooks;
    }
    
    public void setTotalBooks(long totalBooks) {
        this.totalBooks = totalBooks;
    }
    
    public long getAvailableBooks() {
        return availableBooks;
    }
    
    public void setAvailableBooks(long availableBooks) {
        this.availableBooks = availableBooks;
    }
    
    public long getTotalCopies() {
        return totalCopies;
    }
    
    public void setTotalCopies(long totalCopies) {
        this.totalCopies = totalCopies;
    }
    
    public long getTotalAvailableCopies() {
        return totalAvailableCopies;
    }
    
    public void setTotalAvailableCopies(long totalAvailableCopies) {
        this.totalAvailableCopies = totalAvailableCopies;
    }
    
    public List<Object[]> getBooksByCategory() {
        return booksByCategory;
    }
    
    public void setBooksByCategory(List<Object[]> booksByCategory) {
        this.booksByCategory = booksByCategory;
    }
    
    public List<Object[]> getLowStockBooks() {
        return lowStockBooks;
    }
    
    public void setLowStockBooks(List<Object[]> lowStockBooks) {
        this.lowStockBooks = lowStockBooks;
    }
    
    public List<Object[]> getPopularBooks() {
        return popularBooks;
    }
    
    public void setPopularBooks(List<Object[]> popularBooks) {
        this.popularBooks = popularBooks;
    }
    
    public List<Object[]> getRecentlyAddedBooks() {
        return recentlyAddedBooks;
    }
    
    public void setRecentlyAddedBooks(List<Object[]> recentlyAddedBooks) {
        this.recentlyAddedBooks = recentlyAddedBooks;
    }
}
