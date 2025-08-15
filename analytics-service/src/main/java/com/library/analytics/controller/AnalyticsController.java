package com.library.analytics.controller;

import com.library.analytics.dto.*;
import com.library.analytics.service.AnalyticsService;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);
    
    @Autowired
    private AnalyticsService analyticsService;
    
    /**
     * Get comprehensive analytics dashboard
     * This endpoint demonstrates inter-service communication in microservices architecture
     * 
     * Access: ADMIN and LIBRARIAN roles only
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Timed(value = "analytics.dashboard", description = "Time taken to generate analytics dashboard")
    public ResponseEntity<?> getDashboard() {
        try {
            logger.info("Analytics dashboard requested");
            
            long startTime = System.currentTimeMillis();
            
            // This call coordinates data from multiple microservices:
            // - User Service (user statistics, growth rates)
            // - Book Service (inventory, categories, popularity)
            // - Transaction Service (borrowing patterns, overdue items)
            // - System monitoring (health, performance metrics)
            AnalyticsDashboardDto dashboard = analyticsService.generateDashboard();
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            logger.info("Analytics dashboard generated successfully in {} ms", executionTime);
            
            // Format the current timestamp in ISO format for metadata
            LocalDateTime now = LocalDateTime.now();
            String isoTimestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS"));
            
            // Build the response in the exact required format
            Map<String, Object> response = new HashMap<>();
            
            // Metadata section
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("executionTimeMs", executionTime);
            metadata.put("generatedAt", isoTimestamp);
            metadata.put("dataFreshness", "REAL_TIME");
            
            response.put("metadata", metadata);
            response.put("dashboard", dashboard);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating analytics dashboard", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Failed to generate analytics dashboard",
                    "message", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
                ));
        }
    }
    
    /**
     * Get user analytics only
     * Demonstrates isolated service access
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Timed(value = "analytics.users", description = "Time taken to generate user analytics")
    public ResponseEntity<?> getUserAnalytics() {
        try {
            logger.info("User analytics requested");
            
            UserAnalyticsDto userAnalytics = analyticsService.getUserAnalytics();
            
            return ResponseEntity.ok(Map.of(
                "userAnalytics", userAnalytics,
                "generatedAt", java.time.LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            logger.error("Error generating user analytics", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate user analytics"));
        }
    }
    
    /**
     * Get book analytics only
     * Demonstrates isolated service access
     */
    @GetMapping("/books")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Timed(value = "analytics.books", description = "Time taken to generate book analytics")
    public ResponseEntity<?> getBookAnalytics() {
        try {
            logger.info("Book analytics requested");
            
            BookAnalyticsDto bookAnalytics = analyticsService.getBookAnalytics();
            
            return ResponseEntity.ok(Map.of(
                "bookAnalytics", bookAnalytics,
                "generatedAt", java.time.LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            logger.error("Error generating book analytics", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate book analytics"));
        }
    }
    
    /**
     * Get transaction analytics only
     * Demonstrates isolated service access
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Timed(value = "analytics.transactions", description = "Time taken to generate transaction analytics")
    public ResponseEntity<?> getTransactionAnalytics() {
        try {
            logger.info("Transaction analytics requested");
            
            TransactionAnalyticsDto transactionAnalytics = analyticsService.getTransactionAnalytics();
            
            return ResponseEntity.ok(Map.of(
                "transactionAnalytics", transactionAnalytics,
                "generatedAt", java.time.LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            logger.error("Error generating transaction analytics", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate transaction analytics"));
        }
    }
    
    /**
     * Get inventory analytics only
     * Demonstrates cross-service data aggregation (Books + Transactions)
     */
    @GetMapping("/inventory")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Timed(value = "analytics.inventory", description = "Time taken to generate inventory analytics")
    public ResponseEntity<?> getInventoryAnalytics() {
        try {
            logger.info("Inventory analytics requested");
            
            AnalyticsDashboardDto dashboard = analyticsService.generateDashboard();
            
            return ResponseEntity.ok(Map.of(
                "inventoryAnalytics", dashboard.getInventoryAnalytics(),
                "generatedAt", dashboard.getGeneratedAt()
            ));
            
        } catch (Exception e) {
            logger.error("Error generating inventory analytics", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate inventory analytics"));
        }
    }
    
    /**
     * Get system health metrics
     * In microservices, this aggregates health from all services
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "analytics.health", description = "Time taken to generate system health metrics")
    public ResponseEntity<?> getSystemHealth() {
        try {
            logger.info("System health analytics requested");
            
            SystemHealthDto systemHealth = analyticsService.getSystemHealth();
            
            return ResponseEntity.ok(Map.of(
                "systemHealth", systemHealth,
                "generatedAt", java.time.LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            logger.error("Error generating system health analytics", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate system health analytics"));
        }
    }
    
    /**
     * Get analytics summary for public dashboard
     * Limited data for general users
     */
    @GetMapping("/summary")
    @Timed(value = "analytics.summary", description = "Time taken to generate analytics summary")
    public ResponseEntity<?> getAnalyticsSummary() {
        try {
            logger.info("Analytics summary requested");
            
            AnalyticsDashboardDto dashboard = analyticsService.generateDashboard();
            
            // Return limited public data
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalBooks", dashboard.getBookAnalytics().getTotalBooks());
            summary.put("availableBooks", dashboard.getBookAnalytics().getAvailableBooks());
            summary.put("totalUsers", dashboard.getUserAnalytics().getTotalUsers());
            summary.put("activeTransactions", dashboard.getTransactionAnalytics().getActiveTransactions());
            summary.put("systemStatus", dashboard.getSystemHealth().getStatus());
            summary.put("generatedAt", dashboard.getGeneratedAt());
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            logger.error("Error generating analytics summary", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate analytics summary"));
        }
    }
}
