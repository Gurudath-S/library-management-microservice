package com.library.transaction.service;

import com.library.transaction.client.BookServiceClient;
import com.library.transaction.client.UserServiceClient;
import com.library.transaction.dto.TransactionDto;
import com.library.transaction.dto.TransactionResponseDto;
import com.library.transaction.entity.Transaction;
import com.library.transaction.repository.TransactionRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @Autowired
    private BookServiceClient bookServiceClient;
    
    public TransactionResponseDto borrowBook(TransactionDto transactionDto) {
        logger.info("Processing borrow request for user: {} and book: {}", 
                   transactionDto.getUserId(), transactionDto.getBookId());
        
        // Validate user exists
        UserServiceClient.UserDto user;
        try {
            user = userServiceClient.getUserById(transactionDto.getUserId());
        } catch (FeignException e) {
            throw new RuntimeException("User not found with ID: " + transactionDto.getUserId());
        }
        
        // Validate book exists and is available
        BookServiceClient.BookDto book;
        try {
            book = bookServiceClient.getBookById(transactionDto.getBookId());
        } catch (FeignException e) {
            throw new RuntimeException("Book not found with ID: " + transactionDto.getBookId());
        }
        
        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("Book is not available for borrowing");
        }
        
        // Check if user already has this book borrowed
        Optional<Transaction> existingTransaction = transactionRepository
                .findByUserIdAndBookIdAndStatus(transactionDto.getUserId(), 
                                              transactionDto.getBookId(), 
                                              Transaction.TransactionStatus.ACTIVE);
        
        if (existingTransaction.isPresent()) {
            throw new RuntimeException("User has already borrowed this book");
        }
        
        // Check user's borrowing limit (max 5 books)
        long activeTransactions = transactionRepository
                .countActiveTransactionsByUser(transactionDto.getUserId(), 
                                             Transaction.TransactionStatus.ACTIVE);
        
        if (activeTransactions >= 5) {
            throw new RuntimeException("User has reached maximum borrowing limit (5 books)");
        }
        
        // Create transaction
        Transaction transaction = new Transaction(transactionDto.getUserId(), 
                                                transactionDto.getBookId(), 
                                                Transaction.TransactionType.BORROW);
        
        // Set additional info
        transaction.setUserEmail(user.getEmail());
        transaction.setBookTitle(book.getTitle());
        transaction.setBookAuthor(book.getAuthor());
        transaction.setBookIsbn(book.getIsbn());
        transaction.setNotes(transactionDto.getNotes());
        
        if (transactionDto.getDueDate() != null) {
            transaction.setDueDate(transactionDto.getDueDate());
        }
        
        // Save transaction
        transaction = transactionRepository.save(transaction);
        
        // Update book inventory
        try {
            bookServiceClient.borrowBook(transactionDto.getBookId());
        } catch (FeignException e) {
            logger.error("Failed to update book inventory for book ID: {}", transactionDto.getBookId());
            throw new RuntimeException("Failed to update book inventory");
        }
        
        logger.info("Book borrowed successfully. Transaction ID: {}", transaction.getId());
        return new TransactionResponseDto(transaction);
    }
    
    public TransactionResponseDto returnBook(Long transactionId) {
        logger.info("Processing return request for transaction: {}", transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));
        
        if (transaction.getStatus() != Transaction.TransactionStatus.ACTIVE) {
            throw new RuntimeException("Transaction is not active");
        }
        
        // Mark transaction as returned
        transaction.markAsReturned();
        transaction = transactionRepository.save(transaction);
        
        // Update book inventory
        try {
            bookServiceClient.returnBook(transaction.getBookId());
        } catch (FeignException e) {
            logger.error("Failed to update book inventory for book ID: {}", transaction.getBookId());
            throw new RuntimeException("Failed to update book inventory");
        }
        
        logger.info("Book returned successfully. Transaction ID: {}", transaction.getId());
        return new TransactionResponseDto(transaction);
    }
    
    public TransactionResponseDto returnBookByUserAndBook(Long userId, Long bookId) {
        logger.info("Processing return request for user: {} and book: {}", userId, bookId);
        
        Transaction transaction = transactionRepository
                .findByUserIdAndBookIdAndStatus(userId, bookId, Transaction.TransactionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active transaction found for user and book"));
        
        return returnBook(transaction.getId());
    }
    
    public List<TransactionResponseDto> getUserTransactions(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return transactions.stream()
                .map(TransactionResponseDto::new)
                .collect(Collectors.toList());
    }
    
    public List<TransactionResponseDto> getActiveTransactions() {
        List<Transaction> transactions = transactionRepository
                .findByStatus(Transaction.TransactionStatus.ACTIVE);
        return transactions.stream()
                .map(TransactionResponseDto::new)
                .collect(Collectors.toList());
    }
    
    public List<TransactionResponseDto> getOverdueTransactions() {
        List<Transaction> transactions = transactionRepository
                .findOverdueTransactions(LocalDateTime.now(), Transaction.TransactionStatus.ACTIVE);
        return transactions.stream()
                .map(TransactionResponseDto::new)
                .collect(Collectors.toList());
    }
    
    public List<TransactionResponseDto> getBookTransactions(Long bookId) {
        List<Transaction> transactions = transactionRepository.findByBookId(bookId);
        return transactions.stream()
                .map(TransactionResponseDto::new)
                .collect(Collectors.toList());
    }
    
    public List<TransactionResponseDto> getTransactionsByDateRange(LocalDateTime startDate, 
                                                                  LocalDateTime endDate) {
        List<Transaction> transactions = transactionRepository.findByDateRange(startDate, endDate);
        return transactions.stream()
                .map(TransactionResponseDto::new)
                .collect(Collectors.toList());
    }
    
    public Optional<TransactionResponseDto> getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(TransactionResponseDto::new);
    }
    
    // Analytics methods
    public long getTotalTransactionsCount() {
        return transactionRepository.countTotalTransactions();
    }
    
    public long getActiveTransactionsCount() {
        return transactionRepository.countTransactionsByStatus(Transaction.TransactionStatus.ACTIVE);
    }
    
    public long getCompletedTransactionsCount() {
        return transactionRepository.countTransactionsByStatus(Transaction.TransactionStatus.COMPLETED);
    }
    
    public long getOverdueTransactionsCount() {
        List<Transaction> overdueTransactions = transactionRepository
                .findOverdueTransactions(LocalDateTime.now(), Transaction.TransactionStatus.ACTIVE);
        return overdueTransactions.size();
    }
    
    public List<Object[]> getMonthlyTransactionStats() {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(12);
        return transactionRepository.getMonthlyTransactionStats(startDate);
    }
    
    public List<Object[]> getMostBorrowedBooks() {
        return transactionRepository.getMostBorrowedBooks(Transaction.TransactionType.BORROW);
    }
    
    public List<Object[]> getUserBorrowingPatterns() {
        return transactionRepository.getUserBorrowingPatterns(Transaction.TransactionType.BORROW);
    }
}
