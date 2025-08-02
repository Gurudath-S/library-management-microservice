package com.library.transaction.dto;

import com.library.transaction.entity.Transaction;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class TransactionDto {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Book ID is required")
    private Long bookId;
    
    @NotNull(message = "Transaction type is required")
    private Transaction.TransactionType type;
    
    private String notes;
    
    private LocalDateTime dueDate;
    
    // Constructors
    public TransactionDto() {}
    
    public TransactionDto(Long userId, Long bookId, Transaction.TransactionType type) {
        this.userId = userId;
        this.bookId = bookId;
        this.type = type;
    }
    
    // Getters and Setters
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
    
    public Transaction.TransactionType getType() {
        return type;
    }
    
    public void setType(Transaction.TransactionType type) {
        this.type = type;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
}
