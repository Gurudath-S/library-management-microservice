package com.library.user.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class LoggingConfig {

    @Autowired
    private RequestResponseLoggingFilter requestResponseLoggingFilter;

    @Bean
    public FilterRegistrationBean<RequestResponseLoggingFilter> loggingFilter() {
        FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(requestResponseLoggingFilter);
        registrationBean.addUrlPatterns("/api/*"); // Only log API endpoints
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1); // High priority but after security
        registrationBean.setName("UserServiceRequestResponseLoggingFilter");
        return registrationBean;
    }
}
