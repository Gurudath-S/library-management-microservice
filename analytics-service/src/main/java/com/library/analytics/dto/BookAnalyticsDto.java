package com.library.analytics.dto;

import java.util.List;
import java.util.Map;

public class BookAnalyticsDto {
    private long totalBooks;
    private long availableBooks;
    private long totalCopies;
    private long borrowedBooks;
    private Map<String, Long> booksByCategory;
    private List<PopularBookDto> mostBorrowedBooks;
    private List<PopularBookDto> leastBorrowedBooks;
    private double averageBooksPerUser;
    
    // Constructors
    public BookAnalyticsDto() {}

    public BookAnalyticsDto(long totalBooks, long availableBooks, long borrowedBooks,
                            Map<String, Long> booksByCategory, List<PopularBookDto> mostBorrowedBooks,
                            List<PopularBookDto> leastBorrowedBooks, double averageBooksPerUser) {
        this.totalBooks = totalBooks;
        this.availableBooks = availableBooks;
        this.borrowedBooks = borrowedBooks;
        this.booksByCategory = booksByCategory;
        this.mostBorrowedBooks = mostBorrowedBooks;
        this.leastBorrowedBooks = leastBorrowedBooks;
        this.averageBooksPerUser = averageBooksPerUser;
    }
    
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
    
    public long getBorrowedBooks() {
        return borrowedBooks;
    }
    
    public void setBorrowedBooks(long borrowedBooks) {
        this.borrowedBooks = borrowedBooks;
    }
    
    public long getTotalCopies() {
        return totalCopies;
    }
    
    public void setTotalCopies(long totalCopies) {
        this.totalCopies = totalCopies;
    }

    public Map<String, Long> getBooksByCategory() {
        return booksByCategory;
    }
    
    public void setBooksByCategory(Map<String, Long> booksByCategory) {
        this.booksByCategory = booksByCategory;
    }
    
    public List<PopularBookDto> getMostBorrowedBooks() {
        return mostBorrowedBooks;
    }
    
    public void setMostBorrowedBooks(List<PopularBookDto> mostBorrowedBooks) {
        this.mostBorrowedBooks = mostBorrowedBooks;
    }
    
    public List<PopularBookDto> getLeastBorrowedBooks() {
        return leastBorrowedBooks;
    }
    
    public void setLeastBorrowedBooks(List<PopularBookDto> leastBorrowedBooks) {
        this.leastBorrowedBooks = leastBorrowedBooks;
    }
    
    public double getAverageBooksPerUser() {
        return averageBooksPerUser;
    }
    
    public void setAverageBooksPerUser(double averageBooksPerUser) {
        this.averageBooksPerUser = averageBooksPerUser;
    }


    public static class PopularBookDto {
        private String title;
        private String author;
        private String category;
        private long borrowCount;

        public PopularBookDto(String title, String author, String category, long borrowCount) {
            this.title = title;
            this.author = author;
            this.category = category;
            this.borrowCount = borrowCount;
        }

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public long getBorrowCount() { return borrowCount; }
        public void setBorrowCount(long borrowCount) { this.borrowCount = borrowCount; }
    }
}
