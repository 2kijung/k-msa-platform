package com.kimdevops.portfolio.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * RateLimitingFilter — Rate Limiting 필터 (초당 요청 수 제한).
 *
 * <p>Guava RateLimiter로 IP별 요청 수를 제한해 DDoS·크롤링을 방어한다.
 * MSA 환경에서는 이 역할이 API 게이트웨이(Nginx rate_limit 또는 Kong)로 이동하는 것이 맞지만,
 * 전환 기간 동안 monolith에서 1차 방어선으로 유지한다.</p>
 *
 * @FROM  K-portfolio 원본 보안 필터
 * @RISK  Nginx 게이트웨이로 이동하면 이 필터는 제거 대상
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);
    
    // 초당 100 요청 제한
    private static final double REQUESTS_PER_SECOND = 100.0;
    
    private final LoadingCache<String, RateLimiter> limiters = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, RateLimiter>() {
                @Override
                public RateLimiter load(String key) {
                    return RateLimiter.create(REQUESTS_PER_SECOND);
                }
            });

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, java.io.IOException {
        
        String clientId = getClientId(request);
        
        try {
            RateLimiter rateLimiter = limiters.get(clientId);
            
            if (rateLimiter.tryAcquire()) {
                // 요청 허용
                filterChain.doFilter(request, response);
            } else {
                // 요청 거부
                logger.warn("Rate limit exceeded for client: {}", clientId);
                response.setStatus(429); // Too Many Requests
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"Rate limit exceeded\"}");
            }
        } catch (ExecutionException e) {
            logger.error("Error in rate limiting filter", e);
            response.setStatus(500);
        }
    }

    private String getClientId(HttpServletRequest request) {
        // X-Forwarded-For 헤더 확인 (프록시 뒤에 있을 경우)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        // 기본적으로 원격 IP 사용
        return request.getRemoteAddr();
    }
}
