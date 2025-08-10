package com.library.analytics.client;

import com.library.analytics.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "book-service", path = "/api/books", configuration = FeignConfig.class)
public interface BookServiceClient {
    
    @GetMapping("/count")
    Long getTotalBooksCount();
    
    @GetMapping("/total-copies")
    Long getTotalCopies();
    
    @GetMapping("/available-copies")
    Long getTotalAvailableCopies();
    
    @GetMapping("/count-by-category")
    List<Map<String, Object>> getBookCountByCategory();
    
    // Fix: low-stock returns List<Book>, not List<Object[]>
    @GetMapping("/low-stock")
    List<BookDto> getLowStockBooks(@RequestParam(defaultValue = "5") Integer threshold);
    
    // Fix: out-of-stock returns List<Book>, not List<Object[]>
    @GetMapping("/out-of-stock")  
    List<BookDto> getOutOfStockBooks();
    
    // Fix: Use proper DTOs instead of Object[] for better type safety
    @GetMapping("/stats/popular")
    List<BookStatsDto> getPopularBooks();
    
    @GetMapping("/stats/recent")
    List<BookStatsDto> getRecentlyAddedBooks();
    
    // DTO classes for type safety
    class BookDto {
        private Long id;
        private String isbn;
        private String title;
        private String author;
        private String publisher;
        private Integer publicationYear;
        private String category;
        private String description;
        private Integer totalCopies;
        private Integer availableCopies;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getIsbn() { return isbn; }
        public void setIsbn(String isbn) { this.isbn = isbn; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        
        public String getPublisher() { return publisher; }
        public void setPublisher(String publisher) { this.publisher = publisher; }
        
        public Integer getPublicationYear() { return publicationYear; }
        public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Integer getTotalCopies() { return totalCopies; }
        public void setTotalCopies(Integer totalCopies) { this.totalCopies = totalCopies; }
        
        public Integer getAvailableCopies() { return availableCopies; }
        public void setAvailableCopies(Integer availableCopies) { this.availableCopies = availableCopies; }
    }
    
    class BookStatsDto {
        private Long id;
        private String title;
        private String author;
        private Long borrowedCount;
        private String createdAt;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        
        public Long getBorrowedCount() { return borrowedCount; }
        public void setBorrowedCount(Long borrowedCount) { this.borrowedCount = borrowedCount; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}
