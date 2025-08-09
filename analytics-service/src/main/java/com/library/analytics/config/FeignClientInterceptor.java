package com.library.analytics.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(FeignClientInterceptor.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                logger.info("🔗 Adding Authorization header to Feign request: {} - Token: Bearer ***{}",
                        template.url(), 
                        authorizationHeader.substring(Math.max(0, authorizationHeader.length() - 10)));
                template.header(AUTHORIZATION_HEADER, authorizationHeader);
            } else {
                logger.warn("⚠️ No valid Authorization header found in request context for Feign call to: {}. Available headers: {}", 
                        template.url(),
                        request.getHeaderNames() != null ? 
                            java.util.Collections.list(request.getHeaderNames()) : "none");
            }
        } else {
            logger.error("❌ No request context available for Feign call to: {}", template.url());
        }
    }
}
