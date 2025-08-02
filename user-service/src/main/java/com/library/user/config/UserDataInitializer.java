package com.library.user.config;

import com.library.user.entity.User;
import com.library.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class UserDataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(UserDataInitializer.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private final Random random = new Random();
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting User Service data initialization...");
        initializeUsers();
        logDataSummary();
        logger.info("User Service data initialization completed!");
    }
    
    private void initializeUsers() {
        if (userRepository.count() > 0) {
            logger.info("Users already exist, skipping user initialization");
            return;
        }
        
        logger.info("Creating test users...");
        
        // Create admin user
        User admin = createUser("admin", "admin123", "admin@library.com", 
                               "Library", "Administrator", "555-0001", 
                               "123 Admin Street", User.Role.ADMIN);
        userRepository.save(admin);
        
        // Create librarian users
        User librarian1 = createUser("librarian", "librarian123", "librarian@library.com", 
                                   "John", "Librarian", "555-0002", 
                                   "456 Library Ave", User.Role.LIBRARIAN);
        userRepository.save(librarian1);
        
        User librarian2 = createUser("sarah.jones", "librarian123", "sarah.jones@library.com", 
                                   "Sarah", "Jones", "555-0003", 
                                   "789 Book Boulevard", User.Role.LIBRARIAN);
        userRepository.save(librarian2);
        
        // Create regular users with diverse data for analytics
        String[] firstNames = {"Alice", "Bob", "Charlie", "Diana", "Edward", "Fiona", "George", "Hannah", 
                              "Ian", "Julia", "Kevin", "Laura", "Michael", "Nancy", "Oscar", "Patricia",
                              "Quinn", "Rachel", "Samuel", "Teresa", "Ulysses", "Victoria", "William", "Xena",
                              "Yusuf", "Zoe", "Aaron", "Bella", "Carlos", "Deborah", "Elena", "Frank",
                              "Grace", "Henry", "Iris", "Jack", "Karen", "Luis", "Maria", "Nathan"};
        
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
                             "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas",
                             "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson", "White",
                             "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson", "Walker", "Young",
                             "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores"};
        
        List<User> users = new ArrayList<>();
        
        for (int i = 0; i < 45; i++) { // Create 45 regular users + 3 system users = 48 total
            String firstName = firstNames[i % firstNames.length];
            String lastName = lastNames[i % lastNames.length];
            String username = (firstName + "." + lastName + (i + 1)).toLowerCase();
            String email = username + "@email.com";
            String phone = "555-" + String.format("%04d", 1000 + i);
            String address = (100 + i) + " " + firstName + " Street";
            
            // Set creation dates over the past 12 months for growth analytics
            LocalDateTime createdAt = LocalDateTime.now().minusMonths(random.nextInt(12))
                                                        .minusDays(random.nextInt(30))
                                                        .minusHours(random.nextInt(24));
            
            User user = createUser(username, "user123", email, firstName, lastName, phone, address, User.Role.USER);
            user.setCreatedAt(createdAt);
            user.setUpdatedAt(createdAt);
            
            // Randomly disable some users for analytics
            if (random.nextDouble() < 0.05) { // 5% disabled users
                user.setEnabled(false);
            }
            
            users.add(user);
        }
        
        userRepository.saveAll(users);
        logger.info("Created {} users successfully", users.size() + 3);
    }
    
    private User createUser(String username, String password, String email, String firstName, 
                           String lastName, String phone, String address, User.Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(phone);
        user.setAddress(address);
        user.setRole(role);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
    
    private void logDataSummary() {
        long userCount = userRepository.count();
        long adminCount = userRepository.countByRole(User.Role.ADMIN);
        long librarianCount = userRepository.countByRole(User.Role.LIBRARIAN);
        long memberCount = userRepository.countByRole(User.Role.USER);
        
        logger.info("=== USER SERVICE DATA SUMMARY ===");
        logger.info("Total users created: {}", userCount);
        logger.info("Admins: {}", adminCount);
        logger.info("Librarians: {}", librarianCount);
        logger.info("Members: {}", memberCount);
        logger.info("==================================");
        
        // Log sample login credentials
        logger.info("Sample login credentials:");
        logger.info("Admin: username=admin, password=admin123");
        logger.info("Librarian: username=librarian, password=librarian123");
        logger.info("Regular User: username=alice.smith1, password=user123");
    }
}
