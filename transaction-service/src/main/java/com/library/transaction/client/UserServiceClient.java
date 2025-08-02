package com.library.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "user-service", path = "/api/users")
public interface UserServiceClient {
    
    @GetMapping("/{id}")
    UserDto getUserById(@PathVariable Long id);
    
    @GetMapping("/{id}/simple")
    UserSimpleDto getUserSimpleById(@PathVariable Long id);
    
    @GetMapping("/count")
    Long getUserCount();
    
    // DTO classes
    class UserDto {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
    
    class UserSimpleDto {
        private Long id;
        private String email;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
