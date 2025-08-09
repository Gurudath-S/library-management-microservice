package com.library.analytics.client;

import com.library.analytics.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service", path = "/api/users", configuration = FeignConfig.class)
public interface UserServiceClient {
    
    @GetMapping("/count")
    Long getTotalUsersCount();
    
    @GetMapping("/count/active")
    Long getActiveUsersCount();
    
    @GetMapping("/count/new-this-month")
    Long getNewUsersThisMonth();
    
    @GetMapping("/count-by-role")
    List<Map<String, Object>> getUserCountByRole();
    
    @GetMapping("/stats/growth")
    List<Object[]> getUserGrowthStats();
    
    @GetMapping("/stats/top-borrowers")
    List<Object[]> getTopBorrowers();
}
