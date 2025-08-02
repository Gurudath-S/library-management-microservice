package com.library.transaction.dto;

import com.library.transaction.entity.Transaction;
import java.time.LocalDateTime;

public class TransactionResponseDto {
    
    private Long id;
    private Long userId;
    private Long bookId;
    private String userEmail;
    private String bookTitle;
    private String bookAuthor;
    private String bookIsbn;
    private Transaction.TransactionType type;
    private Transaction.TransactionStatus status;
    private LocalDateTime borrowedAt;
    private LocalDateTime dueDate;
    private LocalDateTime returnedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notes;
    private boolean overdue;
    private long daysOverdue;
    
    // Constructor from Transaction entity
    public TransactionResponseDto(Transaction transaction) {
        this.id = transaction.getId();
        this.userId = transaction.getUserId();
        this.bookId = transaction.getBookId();
        this.userEmail = transaction.getUserEmail();
        this.bookTitle = transaction.getBookTitle();
        this.bookAuthor = transaction.getBookAuthor();
        this.bookIsbn = transaction.getBookIsbn();
        this.type = transaction.getType();
        this.status = transaction.getStatus();
        this.borrowedAt = transaction.getBorrowedAt();
        this.dueDate = transaction.getDueDate();
        this.returnedAt = transaction.getReturnedAt();
        this.createdAt = transaction.getCreatedAt();
        this.updatedAt = transaction.getUpdatedAt();
        this.notes = transaction.getNotes();
        this.overdue = transaction.isOverdue();
        this.daysOverdue = transaction.getDaysOverdue();
    }
    
    // Default constructor
    public TransactionResponseDto() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getBookId() {
        return bookId;
    }
    
    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public String getBookTitle() {
        return bookTitle;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    
    public String getBookAuthor() {
        return bookAuthor;
    }
    
    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }
    
    public String getBookIsbn() {
        return bookIsbn;
    }
    
    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }
    
    public Transaction.TransactionType getType() {
        return type;
    }
    
    public void setType(Transaction.TransactionType type) {
        this.type = type;
    }
    
    public Transaction.TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(Transaction.TransactionStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getBorrowedAt() {
        return borrowedAt;
    }
    
    public void setBorrowedAt(LocalDateTime borrowedAt) {
        this.borrowedAt = borrowedAt;
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    
    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }
    
    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public boolean isOverdue() {
        return overdue;
    }
    
    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }
    
    public long getDaysOverdue() {
        return daysOverdue;
    }
    
    public void setDaysOverdue(long daysOverdue) {
        this.daysOverdue = daysOverdue;
    }
}
