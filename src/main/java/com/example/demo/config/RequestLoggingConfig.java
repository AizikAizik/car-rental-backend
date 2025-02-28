package com.example.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class RequestLoggingConfig extends HttpFilter {

  private static final Logger logger = LoggerFactory.getLogger(RequestLoggingConfig.class);

  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH.mm");

  @Override
  protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
          throws IOException, ServletException {
    String currentTime = LocalDateTime.now().format(formatter);

    // Log the request details in the desired format
    logger.info("Request URL: {} Method: {}, Time: {}", request.getRequestURL(), request.getMethod(), currentTime);

    chain.doFilter(request, response);
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Initialization logic if needed
  }

  @Override
  public void destroy() {
    // Cleanup logic if needed
  }
}
