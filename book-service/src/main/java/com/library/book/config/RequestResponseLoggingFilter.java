package com.library.book.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class RequestResponseLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
    private static final String REQUEST_ID = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Skip logging for actuator endpoints and static resources
        String requestURI = httpRequest.getRequestURI();
        if (shouldSkipLogging(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // Generate unique request ID
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(REQUEST_ID, requestId);

        // Wrap request and response to capture content
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);

        long startTime = System.currentTimeMillis();

        try {
            // Log incoming request
            logRequest(wrappedRequest, requestId);

            // Process the request
            chain.doFilter(wrappedRequest, wrappedResponse);

            // Log response
            logResponse(wrappedResponse, requestId, System.currentTimeMillis() - startTime);

        } finally {
            // Copy response content back to original response
            wrappedResponse.copyBodyToResponse();
            MDC.clear();
        }
    }

    private boolean shouldSkipLogging(String requestURI) {
        return requestURI.contains("/actuator") || 
               requestURI.contains("/health") ||
               requestURI.contains("/metrics") ||
               requestURI.contains("/favicon.ico") ||
               requestURI.contains("/webjars") ||
               requestURI.contains("/swagger") ||
               requestURI.contains("/v3/api-docs");
    }

    private void logRequest(ContentCachingRequestWrapper request, String requestId) {
        try {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();
            String requestBody = getRequestBody(request);
            Map<String, String> headers = getHeaders(request);
            
            logger.info("📚 [{}] BOOK-SERVICE INCOMING REQUEST - {} {} {}",
                    requestId, 
                    method, 
                    uri, 
                    queryString != null ? "?" + queryString : "");
            
            logger.info("📚 [{}] REQUEST HEADERS: {}", requestId, headers);
            
            if (requestBody != null && !requestBody.trim().isEmpty()) {
                logger.info("📚 [{}] REQUEST BODY: {}", requestId, requestBody);
            }
            
            if (queryString != null && !queryString.trim().isEmpty()) {
                logger.info("📚 [{}] QUERY PARAMS: {}", requestId, queryString);
            }
            
        } catch (Exception e) {
            logger.warn("Error logging request for requestId: {}", requestId, e);
        }
    }

    private void logResponse(ContentCachingResponseWrapper response, String requestId, long duration) {
        try {
            int status = response.getStatus();
            String responseBody = getResponseBody(response);
            
            // Determine log level based on status code
            if (status >= 200 && status < 300) {
                logger.info("📗 [{}] BOOK-SERVICE RESPONSE - Status: {} | Duration: {}ms", requestId, status, duration);
            } else if (status >= 400 && status < 500) {
                logger.warn("📙 [{}] BOOK-SERVICE CLIENT ERROR - Status: {} | Duration: {}ms", requestId, status, duration);
            } else if (status >= 500) {
                logger.error("📕 [{}] BOOK-SERVICE SERVER ERROR - Status: {} | Duration: {}ms", requestId, status, duration);
            } else {
                logger.info("📘 [{}] BOOK-SERVICE RESPONSE - Status: {} | Duration: {}ms", requestId, status, duration);
            }
            
            if (responseBody != null && !responseBody.trim().isEmpty()) {
                // Limit response body size for logging (book lists can be large)
                String logBody = responseBody.length() > 2000 ? 
                    responseBody.substring(0, 2000) + "... [TRUNCATED - LARGE BOOK LIST]" : responseBody;
                logger.info("📗 [{}] BOOK-SERVICE RESPONSE BODY: {}", requestId, logBody);
            }
            
        } catch (Exception e) {
            logger.warn("Error logging response for requestId: {}", requestId, e);
        }
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        try {
            byte[] content = request.getContentAsByteArray();
            if (content.length > 0) {
                return new String(content, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            logger.warn("Could not read request body", e);
        }
        return null;
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        try {
            byte[] content = response.getContentAsByteArray();
            if (content.length > 0) {
                return new String(content, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            logger.warn("Could not read response body", e);
        }
        return null;
    }

    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        
        if (headerNames != null) {
            for (String headerName : Collections.list(headerNames)) {
                // Mask sensitive headers
                String headerValue = request.getHeader(headerName);
                if ("authorization".equalsIgnoreCase(headerName) && headerValue != null) {
                    headerValue = "Bearer ***" + headerValue.substring(Math.max(0, headerValue.length() - 10));
                }
                headers.put(headerName, headerValue);
            }
        }
        
        return headers;
    }
}
