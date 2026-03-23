package dev.crabcritic.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**

 * Because this is a fun project with one endpoint.
 * A ConcurrentHashMap with a counter is the right tool.
 *
 * Limits: 10 roasts per IP per minute.
 * (I ain't that rich dude)
 * At $0.004/roast, 10/min = $0.04/min worst case = $2.40/hour.
 * (It will stop when the total amount reach 2 dollars anyway, I ain't Jeff Bezouze y'all)
 *
 *if this ever gets real traffic, swap to
 * Bucket4j or a reverse proxy rate limiter. (what is a reverse proxy again ?)
 * But don't, until you have the problem.
 */
@Component
public class RateLimitFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final int MAX_REQUESTS_PER_WINDOW = 10;
    private static final long WINDOW_MS = 60_000;

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        // Only rate-limit the roast endpoint
        if (!"/api/roast".equals(req.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(req);
        WindowCounter counter = counters.compute(ip, (k, existing) -> {
            long now = System.currentTimeMillis();
            if (existing == null || now - existing.windowStart > WINDOW_MS) {
                return new WindowCounter(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        if (counter.count.get() > MAX_REQUESTS_PER_WINDOW) {
            log.warn("Rate limit hit for IP: {}", ip);
            HttpServletResponse res = (HttpServletResponse) response;
            res.setStatus(429);
            res.setContentType("application/json");
            res.getWriter().write(
                    "{\"error\":\"The crab is either fried for a minute. or the wallet is empty\"}"
            );
            return;
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record WindowCounter(long windowStart, AtomicInteger count) {}
}
