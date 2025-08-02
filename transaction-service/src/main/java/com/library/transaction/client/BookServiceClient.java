package com.library.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "book-service", path = "/api/books")
public interface BookServiceClient {
    
    @GetMapping("/{id}")
    BookDto getBookById(@PathVariable Long id);
    
    @GetMapping("/{id}/simple")
    BookSimpleDto getBookSimpleById(@PathVariable Long id);
    
    @GetMapping("/count")
    Long getTotalBooksCount();
    
    @PutMapping("/{id}/borrow")
    void borrowBook(@PathVariable Long id);
    
    @PutMapping("/{id}/return")
    void returnBook(@PathVariable Long id);
    
    // DTO classes
    class BookDto {
        private Long id;
        private String title;
        private String author;
        private String isbn;
        private String category;
        private String publisher;
        private Integer totalCopies;
        private Integer availableCopies;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        
        public String getIsbn() { return isbn; }
        public void setIsbn(String isbn) { this.isbn = isbn; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getPublisher() { return publisher; }
        public void setPublisher(String publisher) { this.publisher = publisher; }
        
        public Integer getTotalCopies() { return totalCopies; }
        public void setTotalCopies(Integer totalCopies) { this.totalCopies = totalCopies; }
        
        public Integer getAvailableCopies() { return availableCopies; }
        public void setAvailableCopies(Integer availableCopies) { this.availableCopies = availableCopies; }
    }
    
    class BookSimpleDto {
        private Long id;
        private String title;
        private String author;
        private String isbn;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        
        public String getIsbn() { return isbn; }
        public void setIsbn(String isbn) { this.isbn = isbn; }
    }
}
