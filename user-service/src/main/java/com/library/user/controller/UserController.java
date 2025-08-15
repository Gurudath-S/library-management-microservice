package com.library.user.controller;

import com.library.user.entity.User;
import com.library.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/profile")
    public ResponseEntity<User> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        
        return userService.findById(currentUser.getId())
                .map(user -> ResponseEntity.ok().body(user))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/profile")
    public ResponseEntity<?> updateCurrentUserProfile(@RequestBody User updatedUser) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();
            
            User user = userService.updateUser(currentUser.getId(), updatedUser);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String searchTerm) {
        List<User> users = userService.searchUsers(searchTerm);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> ResponseEntity.ok().body(user))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        try {
            User user = userService.updateUser(id, updatedUser);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestParam User.Role role) {
        try {
            User user = userService.updateUserRole(id, role);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> enableUser(@PathVariable Long id) {
        try {
            userService.enableUser(id);
            return ResponseEntity.ok().body("User enabled successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> disableUser(@PathVariable Long id) {
        try {
            userService.disableUser(id);
            return ResponseEntity.ok().body("User disabled successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().body("User deleted successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // Analytics endpoints for other services
    @GetMapping("/count")
    public ResponseEntity<Long> getUserCount() {
        return ResponseEntity.ok(userService.getUserCount());
    }
    
    @GetMapping("/count/active")
    public ResponseEntity<Long> getActiveUsersCount() {
        return ResponseEntity.ok(userService.getActiveUsersCount());
    }
    
    @GetMapping("/count/new-this-month")
    public ResponseEntity<Long> getNewUsersThisMonth() {
        return ResponseEntity.ok(userService.getNewUsersThisMonth());
    }
    
    @GetMapping("/count-by-role")
    public ResponseEntity<List<java.util.Map<String, Object>>> getUserCountByRole() {
        return ResponseEntity.ok(userService.getUserCountByRole());
    }
    
    @GetMapping("/stats/growth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<Object[]>> getUserGrowthStats() {
        return ResponseEntity.ok(userService.getUserGrowthStats());
    }
    
    @GetMapping("/stats/top-borrowers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<Object[]>> getTopBorrowers() {
        return ResponseEntity.ok(userService.getTopBorrowers());
    }
    
    @GetMapping("/{id}/simple")
    public ResponseEntity<java.util.Map<String, Object>> getUserSimple(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> {
                    java.util.Map<String, Object> result = new java.util.HashMap<>();
                    result.put("id", user.getId());
                    result.put("email", user.getEmail());
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/count/role/{role}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<Long> getUserCountByRole(@PathVariable User.Role role) {
        return ResponseEntity.ok(userService.getUsersByRoleCount(role));
    }
}
