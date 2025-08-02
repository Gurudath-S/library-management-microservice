package com.library.analytics.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsDataInitializer.class);

    @Autowired
    private AnalyticsService analyticsService;

    @Override
    public void run(String... args) throws Exception {
        logger.info("=== Analytics Service Data Initializer Started ===");
        
        // Wait a moment for other services to start up
        Thread.sleep(5000);
        
        try {
            // Test connectivity to other services by generating a dashboard report
            logger.info("Testing inter-service connectivity...");
            
            var dashboard = analyticsService.generateDashboard();
            logger.info("Successfully connected to other services!");
            logger.info("Analytics dashboard generated successfully with {} sections", 
                       (dashboard.getUserAnalytics() != null ? 1 : 0) +
                       (dashboard.getBookAnalytics() != null ? 1 : 0) +
                       (dashboard.getTransactionAnalytics() != null ? 1 : 0) +
                       (dashboard.getInventoryAnalytics() != null ? 1 : 0));
            
            // Log basic analytics availability
            if (dashboard.getUserAnalytics() != null) {
                logger.info("User analytics data: Available");
            }
            
            if (dashboard.getBookAnalytics() != null) {
                logger.info("Book analytics data: Available");
            }
            
            if (dashboard.getTransactionAnalytics() != null) {
                logger.info("Transaction analytics data: Available");
            }
            
            logger.info("Analytics Service successfully initialized with sample data from other services!");
            
        } catch (Exception e) {
            logger.warn("Could not initialize analytics data - other services may not be ready yet: {}", 
                       e.getMessage());
            logger.info("Analytics service will be available once other services are running");
        }
        
        logger.info("=== Analytics Service Data Initializer Completed ===");
    }
}
