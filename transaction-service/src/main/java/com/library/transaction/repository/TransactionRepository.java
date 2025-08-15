package com.library.transaction.repository;

import com.library.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Find transactions by user
    List<Transaction> findByUserId(Long userId);
    
    // Find transactions by book
    List<Transaction> findByBookId(Long bookId);
    
    // Find active transactions for a user
    List<Transaction> findByUserIdAndStatus(Long userId, Transaction.TransactionStatus status);
    
    // Find active transactions for a book
    List<Transaction> findByBookIdAndStatus(Long bookId, Transaction.TransactionStatus status);
    
    // Find active transaction for a specific user and book
    Optional<Transaction> findByUserIdAndBookIdAndStatus(Long userId, Long bookId, Transaction.TransactionStatus status);
    
    // Find all active transactions
    List<Transaction> findByStatus(Transaction.TransactionStatus status);
    
    // Find overdue transactions
    @Query("SELECT t FROM Transaction t WHERE t.dueDate < :currentDate AND t.status = :status")
    List<Transaction> findOverdueTransactions(@Param("currentDate") LocalDateTime currentDate, 
                                            @Param("status") Transaction.TransactionStatus status);
    
    // Find transactions by date range
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate);
    
    // Count transactions by date range
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);
    
    // Find transactions by type
    List<Transaction> findByType(Transaction.TransactionType type);
    
    // Count active transactions for a user
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId AND t.status = :status")
    long countActiveTransactionsByUser(@Param("userId") Long userId, @Param("status") Transaction.TransactionStatus status);
    
    // Count total transactions
    @Query("SELECT COUNT(t) FROM Transaction t")
    long countTotalTransactions();
    
    // Count transactions by status
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = :status")
    long countTransactionsByStatus(@Param("status") Transaction.TransactionStatus status);
    
    // Count transactions by type
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.type = :type")
    long countTransactionsByType(@Param("type") Transaction.TransactionType type);
    
    // Find transactions by user email (for analytics)
    @Query("SELECT t FROM Transaction t WHERE t.userEmail LIKE %:email%")
    List<Transaction> findByUserEmailContaining(@Param("email") String email);
    
    // Find transactions by book title (for analytics)
    @Query("SELECT t FROM Transaction t WHERE t.bookTitle LIKE %:title%")
    List<Transaction> findByBookTitleContaining(@Param("title") String title);
    
    // Get monthly transaction stats
    @Query("SELECT YEAR(t.createdAt), MONTH(t.createdAt), COUNT(t) FROM Transaction t " +
           "WHERE t.createdAt >= :startDate GROUP BY YEAR(t.createdAt), MONTH(t.createdAt) " +
           "ORDER BY YEAR(t.createdAt), MONTH(t.createdAt)")
    List<Object[]> getMonthlyTransactionStats(@Param("startDate") LocalDateTime startDate);
    
    // Get transaction counts by book
    @Query("SELECT t.bookId, t.bookTitle, COUNT(t) FROM Transaction t " +
           "GROUP BY t.bookId, t.bookTitle ORDER BY COUNT(t) DESC")
    List<Object[]> getTransactionCountsByBook();
    
    // Get most borrowed books
    @Query("SELECT t.bookId, t.bookTitle, t.bookAuthor, COUNT(t) FROM Transaction t " +
           "WHERE t.type = :type GROUP BY t.bookId, t.bookTitle, t.bookAuthor " +
           "ORDER BY COUNT(t) DESC")
    List<Object[]> getMostBorrowedBooks(@Param("type") Transaction.TransactionType type);
    
    // Get user borrowing patterns
    @Query("SELECT t.userId, t.userEmail, COUNT(t) FROM Transaction t " +
           "WHERE t.type = :type GROUP BY t.userId, t.userEmail " +
           "ORDER BY COUNT(t) DESC")
    List<Object[]> getUserBorrowingPatterns(@Param("type") Transaction.TransactionType type);
}
