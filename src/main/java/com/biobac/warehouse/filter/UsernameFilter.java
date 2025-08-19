package com.biobac.warehouse.filter;

import com.biobac.warehouse.utils.AuthUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UsernameFilter implements Filter {
    private final AuthUtil authHelper;

    public UsernameFilter(AuthUtil authHelper) {
        this.authHelper = authHelper;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        try {
            String username = authHelper.getUsernameFromRequest(httpRequest);
            httpRequest.setAttribute("username", username);
        } catch (Exception ignored) {
        }
        chain.doFilter(request, response);
    }
}