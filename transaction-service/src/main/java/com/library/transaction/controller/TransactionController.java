package com.library.transaction.controller;

import com.library.transaction.dto.TransactionDto;
import com.library.transaction.dto.TransactionResponseDto;
import com.library.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {
    
    @Autowired
    private TransactionService transactionService;
    
    @PostMapping("/borrow")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('MEMBER')")
    public ResponseEntity<?> borrowBook(@Valid @RequestBody TransactionDto transactionDto) {
        try {
            TransactionResponseDto transaction = transactionService.borrowBook(transactionDto);
            return ResponseEntity.ok(transaction);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/return")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('MEMBER')")
    public ResponseEntity<?> returnBook(@PathVariable Long id) {
        try {
            TransactionResponseDto transaction = transactionService.returnBook(id);
            return ResponseEntity.ok(transaction);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/return")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('MEMBER')")
    public ResponseEntity<?> returnBookByUserAndBook(@RequestParam Long userId, 
                                                    @RequestParam Long bookId) {
        try {
            TransactionResponseDto transaction = transactionService.returnBookByUserAndBook(userId, bookId);
            return ResponseEntity.ok(transaction);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> getTransactionById(@PathVariable Long id) {
        return transactionService.getTransactionById(id)
                .map(transaction -> ResponseEntity.ok().body(transaction))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionResponseDto>> getUserTransactions(@PathVariable Long userId) {
        List<TransactionResponseDto> transactions = transactionService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<TransactionResponseDto>> getBookTransactions(@PathVariable Long bookId) {
        List<TransactionResponseDto> transactions = transactionService.getBookTransactions(bookId);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<TransactionResponseDto>> getActiveTransactions() {
        List<TransactionResponseDto> transactions = transactionService.getActiveTransactions();
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<TransactionResponseDto>> getOverdueTransactions() {
        List<TransactionResponseDto> transactions = transactionService.getOverdueTransactions();
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<TransactionResponseDto> transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(transactions);
    }
    
    // Analytics endpoints
    @GetMapping("/count/total")
    public ResponseEntity<Long> getTotalTransactionsCount() {
        return ResponseEntity.ok(transactionService.getTotalTransactionsCount());
    }
    
    @GetMapping("/count/active")
    public ResponseEntity<Long> getActiveTransactionsCount() {
        return ResponseEntity.ok(transactionService.getActiveTransactionsCount());
    }
    
    @GetMapping("/count/completed")
    public ResponseEntity<Long> getCompletedTransactionsCount() {
        return ResponseEntity.ok(transactionService.getCompletedTransactionsCount());
    }
    
    @GetMapping("/count/overdue")
    public ResponseEntity<Long> getOverdueTransactionsCount() {
        return ResponseEntity.ok(transactionService.getOverdueTransactionsCount());
    }
    
    @GetMapping("/stats/monthly")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<Object[]>> getMonthlyTransactionStats() {
        return ResponseEntity.ok(transactionService.getMonthlyTransactionStats());
    }
    
    @GetMapping("/stats/most-borrowed")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<Object[]>> getMostBorrowedBooks() {
        return ResponseEntity.ok(transactionService.getMostBorrowedBooks());
    }
    
    @GetMapping("/stats/user-patterns")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<Object[]>> getUserBorrowingPatterns() {
        return ResponseEntity.ok(transactionService.getUserBorrowingPatterns());
    }
}
