package com.library.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RequestResponseLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
    private static final String REQUEST_ID = "requestId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Skip logging for actuator endpoints
        String path = request.getPath().value();
        if (shouldSkipLogging(path)) {
            return chain.filter(exchange);
        }

        // Generate unique request ID
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(REQUEST_ID, requestId);

        long startTime = System.currentTimeMillis();

        // Log incoming request
        logRequest(request, requestId);

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    // Log response
                    ServerHttpResponse response = exchange.getResponse();
                    logResponse(response, requestId, System.currentTimeMillis() - startTime);
                    MDC.clear();
                });
    }

    private boolean shouldSkipLogging(String path) {
        return path.contains("/actuator") || 
               path.contains("/health") ||
               path.contains("/metrics") ||
               path.contains("/favicon.ico") ||
               path.contains("/webjars") ||
               path.contains("/swagger") ||
               path.contains("/v3/api-docs");
    }

    private void logRequest(ServerHttpRequest request, String requestId) {
        try {
            String method = request.getMethod().toString();
            String uri = request.getURI().toString();
            String path = request.getPath().value();
            String queryParams = request.getURI().getQuery();
            
            logger.info("🌐 [{}] API-GATEWAY INCOMING REQUEST - {} {} {}",
                    requestId, 
                    method, 
                    path,
                    queryParams != null ? "?" + queryParams : "");
            
            // Log headers (mask sensitive ones)
            request.getHeaders().forEach((headerName, headerValues) -> {
                String headerValue = String.join(", ", headerValues);
                if ("authorization".equalsIgnoreCase(headerName) && headerValue != null) {
                    headerValue = "Bearer ***" + headerValue.substring(Math.max(0, headerValue.length() - 10));
                }
                logger.info("🌐 [{}] HEADER {}: {}", requestId, headerName, headerValue);
            });
            
            if (queryParams != null && !queryParams.trim().isEmpty()) {
                logger.info("🌐 [{}] QUERY PARAMS: {}", requestId, queryParams);
            }
            
        } catch (Exception e) {
            logger.warn("Error logging request for requestId: {}", requestId, e);
        }
    }

    private void logResponse(ServerHttpResponse response, String requestId, long duration) {
        try {
            int status = response.getStatusCode() != null ? response.getStatusCode().value() : 0;
            
            // Determine log level based on status code
            if (status >= 200 && status < 300) {
                logger.info("✅ [{}] API-GATEWAY RESPONSE - Status: {} | Duration: {}ms", requestId, status, duration);
            } else if (status >= 400 && status < 500) {
                logger.warn("⚠️ [{}] API-GATEWAY CLIENT ERROR - Status: {} | Duration: {}ms", requestId, status, duration);
            } else if (status >= 500) {
                logger.error("❌ [{}] API-GATEWAY SERVER ERROR - Status: {} | Duration: {}ms", requestId, status, duration);
            } else {
                logger.info("ℹ️ [{}] API-GATEWAY RESPONSE - Status: {} | Duration: {}ms", requestId, status, duration);
            }
            
            // Log response headers (if any important ones)
            response.getHeaders().forEach((headerName, headerValues) -> {
                if ("location".equalsIgnoreCase(headerName) || "content-type".equalsIgnoreCase(headerName)) {
                    logger.info("✅ [{}] RESPONSE HEADER {}: {}", requestId, headerName, String.join(", ", headerValues));
                }
            });
            
        } catch (Exception e) {
            logger.warn("Error logging response for requestId: {}", requestId, e);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1; // High priority but after security
    }
}
