package com.library.analytics.client;

import com.library.analytics.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

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
    
    @GetMapping("/low-stock")
    List<Object[]> getLowStockBooks();
    
    @GetMapping("/out-of-stock")
    List<Object[]> getOutOfStockBooks();
    
    @GetMapping("/stats/popular")
    List<Object[]> getPopularBooks();
    
    @GetMapping("/stats/recent")
    List<Object[]> getRecentlyAddedBooks();
}
