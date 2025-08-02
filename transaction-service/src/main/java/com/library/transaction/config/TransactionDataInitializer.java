package com.library.transaction.config;

import com.library.transaction.client.BookServiceClient;
import com.library.transaction.client.UserServiceClient;
import com.library.transaction.entity.Transaction;
import com.library.transaction.repository.TransactionRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Order(3) // Run after User and Book services have initialized their data
public class TransactionDataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionDataInitializer.class);
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @Autowired
    private BookServiceClient bookServiceClient;
    
    private final Random random = new Random();
    
    @Override
    public void run(String... args) throws Exception {
        // Add delay to ensure other services are ready
        Thread.sleep(10000); // 10 second delay
        
        logger.info("Starting Transaction Service data initialization...");
        initializeTransactions();
        logDataSummary();
        logger.info("Transaction Service data initialization completed!");
    }
    
    private void initializeTransactions() {
        if (transactionRepository.count() > 0) {
            logger.info("Transactions already exist, skipping transaction initialization");
            return;
        }
        
        logger.info("Creating test transactions...");
        
        try {
            // Get total counts from other services
            Long userCount = userServiceClient.getUserCount();
            Long bookCount = bookServiceClient.getTotalBooksCount();
            
            if (userCount == null || bookCount == null || userCount == 0 || bookCount == 0) {
                logger.warn("Cannot create transactions - no users or books found in other services");
                return;
            }
            
            logger.info("Found {} users and {} books in other services", userCount, bookCount);
            
            List<Transaction> transactions = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            
            // Create realistic transaction patterns over the past 6 months
            // We'll simulate transactions using known user/book IDs (1 to count)
            for (int i = 0; i < 85; i++) {
                Long userId = (long) (random.nextInt(userCount.intValue()) + 1);
                Long bookId = (long) (random.nextInt(bookCount.intValue()) + 1);
                
                // Create transactions over past 6 months
                LocalDateTime transactionDate = now.minusMonths(random.nextInt(6))
                                                 .minusDays(random.nextInt(30))
                                                 .minusHours(random.nextInt(24));
                
                Transaction transaction = new Transaction();
                transaction.setUserId(userId);
                transaction.setBookId(bookId);
                transaction.setType(Transaction.TransactionType.BORROW);
                transaction.setBorrowedAt(transactionDate);
                transaction.setCreatedAt(transactionDate);
                transaction.setUpdatedAt(transactionDate);
                
                // Set due date (14 days from borrow)
                transaction.setDueDate(transactionDate.plusDays(14));
                
                // Try to get user and book details for denormalized data
                try {
                    UserServiceClient.UserDto user = userServiceClient.getUserById(userId);
                    if (user != null) {
                        transaction.setUserEmail(user.getEmail());
                    }
                } catch (FeignException e) {
                    logger.debug("Could not fetch user details for ID: {}", userId);
                    transaction.setUserEmail("user" + userId + "@email.com");
                }
                
                try {
                    BookServiceClient.BookDto book = bookServiceClient.getBookById(bookId);
                    if (book != null) {
                        transaction.setBookTitle(book.getTitle());
                        transaction.setBookAuthor(book.getAuthor());
                        transaction.setBookIsbn(book.getIsbn());
                    }
                } catch (FeignException e) {
                    logger.debug("Could not fetch book details for ID: {}", bookId);
                    transaction.setBookTitle("Book " + bookId);
                    transaction.setBookAuthor("Unknown Author");
                    transaction.setBookIsbn("ISBN-" + bookId);
                }
                
                // Randomly determine if book has been returned
                double returnProbability = 0.7; // 70% of books are returned
                if (random.nextDouble() < returnProbability) {
                    // Book is returned
                    LocalDateTime returnDate = transactionDate.plusDays(random.nextInt(20) + 1); // Return within 1-20 days
                    if (returnDate.isAfter(now)) {
                        returnDate = now.minusDays(1); // Ensure return date is in the past
                    }
                    
                    transaction.setReturnedAt(returnDate);
                    transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
                    transaction.setUpdatedAt(returnDate);
                } else {
                    // Book is still borrowed
                    transaction.setStatus(Transaction.TransactionStatus.ACTIVE);
                    
                    // Some active transactions are overdue
                    if (transaction.getDueDate().isBefore(now)) {
                        // This is an overdue transaction
                        transaction.setStatus(Transaction.TransactionStatus.ACTIVE); // Still active but overdue
                    }
                }
                
                transactions.add(transaction);
            }
            
            transactionRepository.saveAll(transactions);
            logger.info("Created {} transactions successfully", transactions.size());
            
        } catch (Exception e) {
            logger.error("Error creating transactions: {}", e.getMessage());
            logger.info("Creating minimal sample transactions without service calls");
            createMinimalTransactions();
        }
    }
    
    private void createMinimalTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // Create a few sample transactions with hardcoded data
        for (int i = 1; i <= 10; i++) {
            Transaction transaction = new Transaction();
            transaction.setUserId((long) (i % 5 + 1)); // Users 1-5
            transaction.setBookId((long) (i % 10 + 1)); // Books 1-10
            transaction.setType(Transaction.TransactionType.BORROW);
            transaction.setUserEmail("user" + (i % 5 + 1) + "@email.com");
            transaction.setBookTitle("Sample Book " + (i % 10 + 1));
            transaction.setBookAuthor("Sample Author " + (i % 10 + 1));
            transaction.setBookIsbn("978-000000000" + (i % 10 + 1));
            
            LocalDateTime borrowDate = now.minusDays(random.nextInt(30) + 1);
            transaction.setBorrowedAt(borrowDate);
            transaction.setDueDate(borrowDate.plusDays(14));
            transaction.setCreatedAt(borrowDate);
            transaction.setUpdatedAt(borrowDate);
            
            // Half are returned, half are active
            if (i % 2 == 0) {
                LocalDateTime returnDate = borrowDate.plusDays(random.nextInt(14) + 1);
                transaction.setReturnedAt(returnDate);
                transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            } else {
                transaction.setStatus(Transaction.TransactionStatus.ACTIVE);
            }
            
            transactions.add(transaction);
        }
        
        transactionRepository.saveAll(transactions);
        logger.info("Created {} minimal transactions", transactions.size());
    }
    
    private void logDataSummary() {
        long transactionCount = transactionRepository.count();
        long activeTransactions = transactionRepository.countTransactionsByStatus(Transaction.TransactionStatus.ACTIVE);
        long completedTransactions = transactionRepository.countTransactionsByStatus(Transaction.TransactionStatus.COMPLETED);
        
        // Count overdue transactions
        List<Transaction> overdueTransactions = transactionRepository
                .findOverdueTransactions(LocalDateTime.now(), Transaction.TransactionStatus.ACTIVE);
        long overdueCount = overdueTransactions.size();
        
        logger.info("=== TRANSACTION SERVICE DATA SUMMARY ===");
        logger.info("Total transactions created: {}", transactionCount);
        logger.info("Active transactions: {}", activeTransactions);
        logger.info("Completed transactions: {}", completedTransactions);
        logger.info("Overdue transactions: {}", overdueCount);
        logger.info("========================================");
    }
}
