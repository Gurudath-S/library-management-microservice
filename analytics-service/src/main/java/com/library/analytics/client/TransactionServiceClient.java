package com.library.analytics.client;

import com.library.analytics.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "transaction-service", path = "/api/transactions", configuration = FeignConfig.class)
public interface TransactionServiceClient {
    
    @GetMapping("/count/total")
    Long getTotalTransactionsCount();
    
    @GetMapping("/count/active")
    Long getActiveTransactionsCount();
    
    @GetMapping("/count/completed")
    Long getCompletedTransactionsCount();
    
    @GetMapping("/count/overdue")
    Long getOverdueTransactionsCount();
    
    @GetMapping("/count/today")
    Long getTransactionsTodayCount();
    
    @GetMapping("/count/this-week")
    Long getTransactionsThisWeekCount();
    
    @GetMapping("/count/this-month")
    Long getTransactionsThisMonthCount();
    
    @GetMapping("/stats/monthly")
    List<Object[]> getMonthlyTransactionStats();
    
    @GetMapping("/stats/most-borrowed")
    List<Object[]> getMostBorrowedBooks();
    
    @GetMapping("/stats/user-patterns")
    List<Object[]> getUserBorrowingPatterns();
}
