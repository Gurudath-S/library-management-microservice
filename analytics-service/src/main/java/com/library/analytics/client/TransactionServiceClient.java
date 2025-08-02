package com.library.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "transaction-service", path = "/api/transactions")
public interface TransactionServiceClient {
    
    @GetMapping("/count/total")
    Long getTotalTransactionsCount();
    
    @GetMapping("/count/active")
    Long getActiveTransactionsCount();
    
    @GetMapping("/count/completed")
    Long getCompletedTransactionsCount();
    
    @GetMapping("/count/overdue")
    Long getOverdueTransactionsCount();
    
    @GetMapping("/stats/monthly")
    List<Object[]> getMonthlyTransactionStats();
    
    @GetMapping("/stats/most-borrowed")
    List<Object[]> getMostBorrowedBooks();
    
    @GetMapping("/stats/user-patterns")
    List<Object[]> getUserBorrowingPatterns();
}
