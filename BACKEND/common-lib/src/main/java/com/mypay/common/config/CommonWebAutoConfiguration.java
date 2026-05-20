package com.mypay.common.config;

import com.mypay.common.context.RequestContextFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CommonWebAutoConfiguration {

    @Bean
    public FilterRegistrationBean<RequestContextFilter> requestContextFilterRegistration() {
        FilterRegistrationBean<RequestContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestContextFilter());
        registration.setOrder(Integer.MIN_VALUE);
        return registration;
    }
}
