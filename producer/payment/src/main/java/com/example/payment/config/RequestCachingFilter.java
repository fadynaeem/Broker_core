package com.example.payment.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import java.io.IOException;
@Component
public class RequestCachingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest http) {
            chain.doFilter(new CachedBodyRequestWrapper(http), response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
