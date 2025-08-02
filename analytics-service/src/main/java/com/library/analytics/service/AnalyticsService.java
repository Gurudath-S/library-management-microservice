package com.library.analytics.service;

import com.library.analytics.client.BookServiceClient;
import com.library.analytics.client.TransactionServiceClient;
import com.library.analytics.client.UserServiceClient;
import com.library.analytics.dto.*;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @Autowired
    private BookServiceClient bookServiceClient;
    
    @Autowired
    private TransactionServiceClient transactionServiceClient;
    
    @CircuitBreaker(name = "analytics-dashboard", fallbackMethod = "fallbackGenerateDashboard")
    public AnalyticsDashboardDto generateDashboard() {
        logger.info("Generating analytics dashboard");
        
        AnalyticsDashboardDto dashboard = new AnalyticsDashboardDto();
        
        try {
            // Parallel data collection from all services
            CompletableFuture<UserAnalyticsDto> userAnalyticsFuture = 
                CompletableFuture.supplyAsync(this::getUserAnalytics);
            
            CompletableFuture<BookAnalyticsDto> bookAnalyticsFuture = 
                CompletableFuture.supplyAsync(this::getBookAnalytics);
            
            CompletableFuture<TransactionAnalyticsDto> transactionAnalyticsFuture = 
                CompletableFuture.supplyAsync(this::getTransactionAnalytics);
            
            CompletableFuture<SystemHealthDto> systemHealthFuture = 
                CompletableFuture.supplyAsync(this::getSystemHealth);
            
            // Wait for all futures to complete
            CompletableFuture.allOf(
                userAnalyticsFuture, 
                bookAnalyticsFuture, 
                transactionAnalyticsFuture, 
                systemHealthFuture
            ).join();
            
            // Set results
            dashboard.setUserAnalytics(userAnalyticsFuture.get());
            dashboard.setBookAnalytics(bookAnalyticsFuture.get());
            dashboard.setTransactionAnalytics(transactionAnalyticsFuture.get());
            dashboard.setSystemHealth(systemHealthFuture.get());
            
            // Generate inventory analytics (requires book and transaction data)
            dashboard.setInventoryAnalytics(generateInventoryAnalytics(
                dashboard.getBookAnalytics(), 
                dashboard.getTransactionAnalytics()
            ));
            
            logger.info("Analytics dashboard generated successfully");
            return dashboard;
            
        } catch (Exception e) {
            logger.error("Error generating analytics dashboard", e);
            throw new RuntimeException("Failed to generate analytics dashboard", e);
        }
    }
    
    @CircuitBreaker(name = "user-analytics", fallbackMethod = "fallbackGetUserAnalytics")
    public UserAnalyticsDto getUserAnalytics() {
        UserAnalyticsDto analytics = new UserAnalyticsDto();
        
        try {
            analytics.setTotalUsers(userServiceClient.getTotalUsersCount());
            analytics.setActiveUsers(userServiceClient.getActiveUsersCount());
            analytics.setNewUsersThisMonth(userServiceClient.getNewUsersThisMonth());
            
            // Convert user count by role to Map
            List<Map<String, Object>> roleData = userServiceClient.getUserCountByRole();
            Map<String, Long> usersByRole = roleData.stream()
                .collect(Collectors.toMap(
                    map -> (String) map.get("role"),
                    map -> ((Number) map.get("count")).longValue()
                ));
            analytics.setUsersByRole(usersByRole);
            
            analytics.setUserGrowthStats(userServiceClient.getUserGrowthStats());
            analytics.setTopBorrowers(userServiceClient.getTopBorrowers());
            
        } catch (FeignException e) {
            logger.error("Error fetching user analytics from user-service", e);
            analytics = getFallbackUserAnalytics();
        }
        
        return analytics;
    }
    
    @CircuitBreaker(name = "book-analytics", fallbackMethod = "fallbackGetBookAnalytics")
    public BookAnalyticsDto getBookAnalytics() {
        BookAnalyticsDto analytics = new BookAnalyticsDto();
        
        try {
            analytics.setTotalBooks(bookServiceClient.getTotalBooksCount());
            analytics.setTotalCopies(bookServiceClient.getTotalCopies());
            analytics.setTotalAvailableCopies(bookServiceClient.getTotalAvailableCopies());
            analytics.setAvailableBooks(analytics.getTotalAvailableCopies() > 0 ? 1 : 0); // Simplified
            
            // Convert book count by category
            List<Map<String, Object>> categoryData = bookServiceClient.getBookCountByCategory();
            analytics.setBooksByCategory(categoryData.stream()
                .map(map -> new Object[]{map.get("category"), map.get("count")})
                .collect(Collectors.toList()));
            
            analytics.setLowStockBooks(bookServiceClient.getLowStockBooks());
            analytics.setPopularBooks(bookServiceClient.getPopularBooks());
            analytics.setRecentlyAddedBooks(bookServiceClient.getRecentlyAddedBooks());
            
        } catch (FeignException e) {
            logger.error("Error fetching book analytics from book-service", e);
            analytics = getFallbackBookAnalytics();
        }
        
        return analytics;
    }
    
    @CircuitBreaker(name = "transaction-analytics", fallbackMethod = "fallbackGetTransactionAnalytics")
    public TransactionAnalyticsDto getTransactionAnalytics() {
        TransactionAnalyticsDto analytics = new TransactionAnalyticsDto();
        
        try {
            analytics.setTotalTransactions(transactionServiceClient.getTotalTransactionsCount());
            analytics.setActiveTransactions(transactionServiceClient.getActiveTransactionsCount());
            analytics.setCompletedTransactions(transactionServiceClient.getCompletedTransactionsCount());
            analytics.setOverdueTransactions(transactionServiceClient.getOverdueTransactionsCount());
            
            analytics.setMonthlyStats(transactionServiceClient.getMonthlyTransactionStats());
            analytics.setMostBorrowedBooks(transactionServiceClient.getMostBorrowedBooks());
            analytics.setUserBorrowingPatterns(transactionServiceClient.getUserBorrowingPatterns());
            
        } catch (FeignException e) {
            logger.error("Error fetching transaction analytics from transaction-service", e);
            analytics = getFallbackTransactionAnalytics();
        }
        
        return analytics;
    }
    
    public SystemHealthDto getSystemHealth() {
        SystemHealthDto health = new SystemHealthDto();
        
        Map<String, String> serviceStatus = new HashMap<>();
        Map<String, Object> performanceMetrics = new HashMap<>();
        
        // Check each service health
        serviceStatus.put("user-service", checkServiceHealth(() -> {
            try { userServiceClient.getTotalUsersCount(); } catch (Exception e) { throw new RuntimeException(e); }
        }));
        serviceStatus.put("book-service", checkServiceHealth(() -> {
            try { bookServiceClient.getTotalBooksCount(); } catch (Exception e) { throw new RuntimeException(e); }
        }));
        serviceStatus.put("transaction-service", checkServiceHealth(() -> {
            try { transactionServiceClient.getTotalTransactionsCount(); } catch (Exception e) { throw new RuntimeException(e); }
        }));
        
        // Overall system status
        boolean allServicesUp = serviceStatus.values().stream()
            .allMatch(status -> "UP".equals(status));
        health.setStatus(allServicesUp ? "UP" : "DEGRADED");
        
        health.setServiceStatus(serviceStatus);
        
        // Add performance metrics
        performanceMetrics.put("responseTime", System.currentTimeMillis());
        performanceMetrics.put("servicesUp", serviceStatus.values().stream().mapToLong(s -> "UP".equals(s) ? 1 : 0).sum());
        performanceMetrics.put("totalServices", serviceStatus.size());
        
        health.setPerformanceMetrics(performanceMetrics);
        
        return health;
    }
    
    private String checkServiceHealth(Runnable healthCheck) {
        try {
            healthCheck.run();
            return "UP";
        } catch (Exception e) {
            logger.warn("Service health check failed", e);
            return "DOWN";
        }
    }
    
    private InventoryAnalyticsDto generateInventoryAnalytics(BookAnalyticsDto bookAnalytics, 
                                                            TransactionAnalyticsDto transactionAnalytics) {
        InventoryAnalyticsDto inventory = new InventoryAnalyticsDto();
        
        inventory.setTotalBooks(bookAnalytics.getTotalBooks());
        inventory.setTotalCopies(bookAnalytics.getTotalCopies());
        inventory.setAvailableCopies(bookAnalytics.getTotalAvailableCopies());
        inventory.setBorrowedCopies(transactionAnalytics.getActiveTransactions());
        
        // Calculate utilization rate
        if (inventory.getTotalCopies() > 0) {
            double utilizationRate = (double) inventory.getBorrowedCopies() / inventory.getTotalCopies() * 100;
            inventory.setUtilizationRate(Math.round(utilizationRate * 100.0) / 100.0);
        }
        
        inventory.setLowStockCount(bookAnalytics.getLowStockBooks().size());
        inventory.setOutOfStockCount(0); // Will be calculated from book service data
        
        return inventory;
    }
    
    // Fallback methods for circuit breaker
    public AnalyticsDashboardDto fallbackGenerateDashboard(Exception ex) {
        logger.warn("Using fallback for dashboard generation: {}", ex.getMessage());
        
        AnalyticsDashboardDto dashboard = new AnalyticsDashboardDto();
        dashboard.setUserAnalytics(getFallbackUserAnalytics());
        dashboard.setBookAnalytics(getFallbackBookAnalytics());
        dashboard.setTransactionAnalytics(getFallbackTransactionAnalytics());
        dashboard.setSystemHealth(getFallbackSystemHealth());
        dashboard.setInventoryAnalytics(new InventoryAnalyticsDto());
        
        return dashboard;
    }
    
    public UserAnalyticsDto fallbackGetUserAnalytics(Exception ex) {
        return getFallbackUserAnalytics();
    }
    
    public BookAnalyticsDto fallbackGetBookAnalytics(Exception ex) {
        return getFallbackBookAnalytics();
    }
    
    public TransactionAnalyticsDto fallbackGetTransactionAnalytics(Exception ex) {
        return getFallbackTransactionAnalytics();
    }
    
    private UserAnalyticsDto getFallbackUserAnalytics() {
        UserAnalyticsDto analytics = new UserAnalyticsDto();
        analytics.setTotalUsers(0);
        analytics.setActiveUsers(0);
        analytics.setNewUsersThisMonth(0);
        analytics.setUsersByRole(new HashMap<>());
        return analytics;
    }
    
    private BookAnalyticsDto getFallbackBookAnalytics() {
        BookAnalyticsDto analytics = new BookAnalyticsDto();
        analytics.setTotalBooks(0);
        analytics.setAvailableBooks(0);
        analytics.setTotalCopies(0);
        analytics.setTotalAvailableCopies(0);
        return analytics;
    }
    
    private TransactionAnalyticsDto getFallbackTransactionAnalytics() {
        TransactionAnalyticsDto analytics = new TransactionAnalyticsDto();
        analytics.setTotalTransactions(0);
        analytics.setActiveTransactions(0);
        analytics.setCompletedTransactions(0);
        analytics.setOverdueTransactions(0);
        return analytics;
    }
    
    private SystemHealthDto getFallbackSystemHealth() {
        SystemHealthDto health = new SystemHealthDto();
        health.setStatus("UNKNOWN");
        health.setServiceStatus(new HashMap<>());
        health.setPerformanceMetrics(new HashMap<>());
        return health;
    }
}
