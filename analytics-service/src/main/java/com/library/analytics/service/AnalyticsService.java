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
        
        // Sequential data collection from all services with individual error handling
        // This ensures JWT tokens are properly propagated in Feign client calls
        UserAnalyticsDto userAnalytics = null;
        BookAnalyticsDto bookAnalytics = null;
        TransactionAnalyticsDto transactionAnalytics = null;
        SystemHealthDto systemHealth = null;
        
        // Fetch user analytics with individual error handling
        try {
            logger.debug("Fetching user analytics...");
            userAnalytics = getUserAnalytics();
        } catch (Exception e) {
            logger.error("Failed to fetch user analytics, using fallback", e);
            userAnalytics = getFallbackUserAnalytics();
        }
        
        // Fetch book analytics with individual error handling
        try {
            logger.debug("Fetching book analytics...");
            bookAnalytics = getBookAnalytics();
        } catch (Exception e) {
            logger.error("Failed to fetch book analytics, using fallback", e);
            bookAnalytics = getFallbackBookAnalytics();
        }
        
        // Fetch transaction analytics with individual error handling
        try {
            logger.debug("Fetching transaction analytics...");
            transactionAnalytics = getTransactionAnalytics();
        } catch (Exception e) {
            logger.error("Failed to fetch transaction analytics, using fallback", e);
            transactionAnalytics = getFallbackTransactionAnalytics();
        }
        
        // Fetch system health with individual error handling
        try {
            logger.debug("Fetching system health...");
            systemHealth = getSystemHealth();
        } catch (Exception e) {
            logger.error("Failed to fetch system health, using fallback", e);
            systemHealth = getFallbackSystemHealth();
        }
        
        // Set results (even if some are fallback values)
        dashboard.setUserAnalytics(userAnalytics);
        dashboard.setBookAnalytics(bookAnalytics);
        dashboard.setTransactionAnalytics(transactionAnalytics);
        dashboard.setSystemHealth(systemHealth);
        
        // Generate inventory analytics (handles null values gracefully)
        try {
            dashboard.setInventoryAnalytics(generateInventoryAnalytics(
                bookAnalytics, 
                transactionAnalytics
            ));
        } catch (Exception e) {
            logger.error("Failed to generate inventory analytics, using empty analytics", e);
            dashboard.setInventoryAnalytics(new InventoryAnalyticsDto());
        }
        
        logger.info("Analytics dashboard generated successfully (some data may be from fallbacks)");
        return dashboard;
    }
    
    @CircuitBreaker(name = "user-analytics", fallbackMethod = "fallbackGetUserAnalytics")
    public UserAnalyticsDto getUserAnalytics() {
        UserAnalyticsDto analytics = new UserAnalyticsDto();
        
        // Get total users count with individual error handling
        try {
            analytics.setTotalUsers(userServiceClient.getTotalUsersCount());
        } catch (Exception e) {
            logger.warn("Failed to fetch total users count: {}", e.getMessage());
            analytics.setTotalUsers(0);
        }
        
        // Get active users count with individual error handling
        try {
            analytics.setActiveUsers(userServiceClient.getActiveUsersCount());
        } catch (Exception e) {
            logger.warn("Failed to fetch active users count: {}", e.getMessage());
            analytics.setActiveUsers(0);
        }
        
        // Get new users this month with individual error handling
        try {
            analytics.setNewUsersThisMonth(userServiceClient.getNewUsersThisMonth());
        } catch (Exception e) {
            logger.warn("Failed to fetch new users this month: {}", e.getMessage());
            analytics.setNewUsersThisMonth(0);
        }
        
        // Get user count by role with individual error handling
        try {
            List<Map<String, Object>> roleData = userServiceClient.getUserCountByRole();
            Map<String, Long> usersByRole = roleData.stream()
                .collect(Collectors.toMap(
                    map -> (String) map.get("role"),
                    map -> ((Number) map.get("count")).longValue()
                ));
            analytics.setUsersByRole(usersByRole);
        } catch (Exception e) {
            logger.warn("Failed to fetch user count by role: {}", e.getMessage());
            analytics.setUsersByRole(new HashMap<>());
        }
        
        // Get user growth stats with individual error handling
        try {
            analytics.setUserGrowthStats(userServiceClient.getUserGrowthStats());
        } catch (Exception e) {
            logger.warn("Failed to fetch user growth stats: {}", e.getMessage());
            analytics.setUserGrowthStats(null);
        }
        
        // Get top borrowers with individual error handling
        try {
            analytics.setTopBorrowers(userServiceClient.getTopBorrowers());
        } catch (Exception e) {
            logger.warn("Failed to fetch top borrowers: {}", e.getMessage());
            analytics.setTopBorrowers(null);
        }
        
        return analytics;
    }
    
    @CircuitBreaker(name = "book-analytics", fallbackMethod = "fallbackGetBookAnalytics")
    public BookAnalyticsDto getBookAnalytics() {
        BookAnalyticsDto analytics = new BookAnalyticsDto();
        
        // Get total books count with individual error handling
        try {
            analytics.setTotalBooks(bookServiceClient.getTotalBooksCount());
        } catch (Exception e) {
            logger.warn("Failed to fetch total books count: {}", e.getMessage());
            analytics.setTotalBooks(0);
        }
        
        // Get total copies with individual error handling
        try {
            analytics.setTotalCopies(bookServiceClient.getTotalCopies());
        } catch (Exception e) {
            logger.warn("Failed to fetch total copies: {}", e.getMessage());
            analytics.setTotalCopies(0);
        }
        
        // Get total available copies with individual error handling
        try {
            analytics.setTotalAvailableCopies(bookServiceClient.getTotalAvailableCopies());
            analytics.setAvailableBooks(analytics.getTotalAvailableCopies() > 0 ? 1 : 0); // Simplified
        } catch (Exception e) {
            logger.warn("Failed to fetch total available copies: {}", e.getMessage());
            analytics.setTotalAvailableCopies(0);
            analytics.setAvailableBooks(0);
        }
        
        // Get book count by category with individual error handling
        try {
            List<Map<String, Object>> categoryData = bookServiceClient.getBookCountByCategory();
            analytics.setBooksByCategory(categoryData.stream()
                .map(map -> new Object[]{map.get("category"), map.get("count")})
                .collect(Collectors.toList()));
        } catch (Exception e) {
            logger.warn("Failed to fetch book count by category: {}", e.getMessage());
            analytics.setBooksByCategory(null);
        }
        
        // Get low stock books with individual error handling
        try {
            List<BookServiceClient.BookDto> lowStockBooksDto = bookServiceClient.getLowStockBooks(5);
            analytics.setLowStockBooks(lowStockBooksDto.stream()
                .map(book -> new Object[]{book.getId(), book.getTitle(), book.getAuthor(), book.getAvailableCopies()})
                .collect(Collectors.toList()));
        } catch (Exception e) {
            logger.warn("Failed to fetch low stock books: {}", e.getMessage());
            analytics.setLowStockBooks(null);
        }
        
        // Get popular books with individual error handling
        try {
            List<BookServiceClient.BookStatsDto> popularBooksDto = bookServiceClient.getPopularBooks();
            analytics.setPopularBooks(popularBooksDto.stream()
                .map(book -> new Object[]{book.getId(), book.getTitle(), book.getAuthor(), book.getBorrowedCount()})
                .collect(Collectors.toList()));
        } catch (Exception e) {
            logger.warn("Failed to fetch popular books: {}", e.getMessage());
            analytics.setPopularBooks(null);
        }
        
        // Get recently added books with individual error handling
        try {
            List<BookServiceClient.BookStatsDto> recentBooksDto = bookServiceClient.getRecentlyAddedBooks();
            analytics.setRecentlyAddedBooks(recentBooksDto.stream()
                .map(book -> new Object[]{book.getId(), book.getTitle(), book.getAuthor(), book.getCreatedAt()})
                .collect(Collectors.toList()));
        } catch (Exception e) {
            logger.warn("Failed to fetch recently added books: {}", e.getMessage());
            analytics.setRecentlyAddedBooks(null);
        }
        
        return analytics;
    }
    
    @CircuitBreaker(name = "transaction-analytics", fallbackMethod = "fallbackGetTransactionAnalytics")
    public TransactionAnalyticsDto getTransactionAnalytics() {
        TransactionAnalyticsDto analytics = new TransactionAnalyticsDto();
        
        // Get total transactions count with individual error handling
        try {
            analytics.setTotalTransactions(transactionServiceClient.getTotalTransactionsCount());
        } catch (Exception e) {
            logger.warn("Failed to fetch total transactions count: {}", e.getMessage());
            analytics.setTotalTransactions(0);
        }
        
        // Get active transactions count with individual error handling
        try {
            analytics.setActiveTransactions(transactionServiceClient.getActiveTransactionsCount());
        } catch (Exception e) {
            logger.warn("Failed to fetch active transactions count: {}", e.getMessage());
            analytics.setActiveTransactions(0);
        }
        
        // Get completed transactions count with individual error handling
        try {
            analytics.setCompletedTransactions(transactionServiceClient.getCompletedTransactionsCount());
        } catch (Exception e) {
            logger.warn("Failed to fetch completed transactions count: {}", e.getMessage());
            analytics.setCompletedTransactions(0);
        }
        
        // Get overdue transactions count with individual error handling
        try {
            analytics.setOverdueTransactions(transactionServiceClient.getOverdueTransactionsCount());
        } catch (Exception e) {
            logger.warn("Failed to fetch overdue transactions count: {}", e.getMessage());
            analytics.setOverdueTransactions(0);
        }
        
        // Get monthly stats with individual error handling
        try {
            analytics.setMonthlyStats(transactionServiceClient.getMonthlyTransactionStats());
        } catch (Exception e) {
            logger.warn("Failed to fetch monthly transaction stats: {}", e.getMessage());
            analytics.setMonthlyStats(null);
        }
        
        // Get most borrowed books with individual error handling
        try {
            analytics.setMostBorrowedBooks(transactionServiceClient.getMostBorrowedBooks());
        } catch (Exception e) {
            logger.warn("Failed to fetch most borrowed books: {}", e.getMessage());
            analytics.setMostBorrowedBooks(null);
        }
        
        // Get user borrowing patterns with individual error handling
        try {
            analytics.setUserBorrowingPatterns(transactionServiceClient.getUserBorrowingPatterns());
        } catch (Exception e) {
            logger.warn("Failed to fetch user borrowing patterns: {}", e.getMessage());
            analytics.setUserBorrowingPatterns(null);
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
        
        // Safely get book analytics data with null checks
        inventory.setTotalBooks(bookAnalytics != null ? bookAnalytics.getTotalBooks() : 0);
        inventory.setTotalCopies(bookAnalytics != null ? bookAnalytics.getTotalCopies() : 0);
        inventory.setAvailableCopies(bookAnalytics != null ? bookAnalytics.getTotalAvailableCopies() : 0);
        
        // Safely get transaction analytics data with null checks
        inventory.setBorrowedCopies(transactionAnalytics != null ? transactionAnalytics.getActiveTransactions() : 0);
        
        // Calculate utilization rate with safe division
        if (inventory.getTotalCopies() > 0) {
            double utilizationRate = (double) inventory.getBorrowedCopies() / inventory.getTotalCopies() * 100;
            inventory.setUtilizationRate(Math.round(utilizationRate * 100.0) / 100.0);
        } else {
            inventory.setUtilizationRate(0.0);
        }
        
        // Safely calculate low stock count
        if (bookAnalytics != null && bookAnalytics.getLowStockBooks() != null) {
            inventory.setLowStockCount(bookAnalytics.getLowStockBooks().size());
        } else {
            inventory.setLowStockCount(0);
        }
        
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
