package com.library.book.repository;

import com.library.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    Optional<Book> findByIsbn(String isbn);
    
    List<Book> findByCategory(String category);
    
    List<Book> findByAuthor(String author);
    
    List<Book> findByPublisher(String publisher);
    
    List<Book> findByPublicationYear(Integer year);
    
    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0 AND b.status = 'AVAILABLE'")
    List<Book> findAvailableBooks();
    
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.category) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.publisher) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Book> searchBooks(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT DISTINCT b.category FROM Book b ORDER BY b.category")
    List<String> findAllCategories();
    
    @Query("SELECT DISTINCT b.author FROM Book b ORDER BY b.author")
    List<String> findAllAuthors();
    
    @Query("SELECT DISTINCT b.publisher FROM Book b ORDER BY b.publisher")
    List<String> findAllPublishers();
    
    @Query("SELECT b FROM Book b WHERE b.availableCopies = 0")
    List<Book> findOutOfStockBooks();
    
    @Query("SELECT b FROM Book b WHERE b.availableCopies <= :threshold")
    List<Book> findLowStockBooks(@Param("threshold") Integer threshold);
    
    // Analytics support methods
    @Query("SELECT COUNT(b) FROM Book b WHERE b.availableCopies > :minCopies")
    long countByAvailableCopiesGreaterThan(@Param("minCopies") int minCopies);
    
    @Query("SELECT SUM(b.totalCopies) FROM Book b")
    long getTotalCopies();
    
    @Query("SELECT SUM(b.availableCopies) FROM Book b")
    long getTotalAvailableCopies();
    
    @Query("SELECT new map(b.category as category, COUNT(b) as count) FROM Book b GROUP BY b.category")
    List<java.util.Map<String, Object>> getBookCountByCategory();
    
    // Popular books (books with high availability or frequently borrowed)
    @Query("SELECT b.id, b.title, b.author, b.totalCopies - b.availableCopies as borrowedCount " +
           "FROM Book b ORDER BY (b.totalCopies - b.availableCopies) DESC")
    List<Object[]> getPopularBooks();
    
    // Recently added books
    @Query("SELECT b.id, b.title, b.author, b.createdAt " +
           "FROM Book b ORDER BY b.createdAt DESC")
    List<Object[]> getRecentlyAddedBooks();
    
    List<Book> findByAvailableCopiesLessThan(int threshold);
}
