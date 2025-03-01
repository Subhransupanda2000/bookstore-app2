package com.example.bookstoreapp.filters;

import com.example.bookstoreapp.auth.AuthService;
import com.example.bookstoreapp.exceptions.AppRuntimeException;
import com.example.bookstoreapp.utils.AppUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;

@Slf4j
public class AuthenticationFilter implements Filter {

  private static final String[] PUBLIC_URI_PATTERNS = {"/api/**", "/user/login", "/user/signup", "/health"};

  public static final String X_AUTH_TOKEN = "X-AUTH-TOKEN";

  private AuthService authService;

  public AuthenticationFilter(AuthService authService) {
    this.authService = authService;
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    log.info("filter executed for url: {}", request.getRequestURL());
    log.info("filter executed for uri: {}", request.getRequestURI());
    if (isPublicUri(request.getRequestURI())) {
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    boolean userContext = setUserContext(request, response);
    if(userContext) {
      filterChain.doFilter(servletRequest, servletResponse);
    }

  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    Filter.super.init(filterConfig);
  }

  @Override
  public void destroy() {
    Filter.super.destroy();
  }

  private boolean isPublicUri(String requestUri) {
    for (String pattern : PUBLIC_URI_PATTERNS) {
      boolean matched = AppUtils.matchesPattern(pattern, requestUri);
      if (matched) {
        return true;
      }
    }
    return false;
  }

  private boolean setUserContext(HttpServletRequest request, HttpServletResponse response) {
    String token = request.getHeader(X_AUTH_TOKEN);
    try {
      authService.setUserContext(token);
      return true;
    } catch (AppRuntimeException e) {
      log.error("error while setting user context: ", e);
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
    }
    return false;
  }
}
