package com.library.book.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - no authentication required
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                
                // Actuator endpoints - allow monitoring without auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/metrics").permitAll()
                .requestMatchers("/actuator/metrics/**").permitAll()
                .requestMatchers("/actuator/prometheus").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                
                // Public read endpoints for books (browsing)
                .requestMatchers(HttpMethod.GET, "/api/books").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books/category/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books/author/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books/categories").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books/authors").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books/publishers").permitAll()
                
                // Analytics endpoints - allow for inter-service communication
                .requestMatchers(HttpMethod.GET, "/api/books/count").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books/total-copies").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books/available-copies").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books/count-by-category").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books/stats/**").permitAll()
                
                // Internal service endpoints for transaction service
                .requestMatchers(HttpMethod.PUT, "/api/books/*/borrow").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/books/*/return").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/books/*/simple").authenticated()
                
                // Admin/Librarian management endpoints
                .requestMatchers(HttpMethod.POST, "/api/books").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/books/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/books/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/books/upload").authenticated()
                .requestMatchers("/api/books/low-stock").authenticated()
                .requestMatchers("/api/books/out-of-stock").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions().disable()); // For H2 Console

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
