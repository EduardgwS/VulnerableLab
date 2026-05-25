package br.unipar.frameworks.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter{
    private static final int MAX_REQUESTS = 10;
    private static final long TIME_WINDOW = 60000;

    private final Map<String, RequestData> requests = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String ip = request.getRemoteAddr();

        RequestData data = requests.getOrDefault(ip,
                new RequestData(0, Instant.now().toEpochMilli()));

        long now = Instant.now().toEpochMilli();

        if (now - data.timestamp > TIME_WINDOW) {
            data = new RequestData(0, now);
        }

        if (data.count >= MAX_REQUESTS) {

            response.setStatus(429);
            response.getWriter().write("Limite de requisições excedido.");
            return;
        }

        data.count++;

        requests.put(ip, data);

        filterChain.doFilter(request, response);
    }

    private static class RequestData {

        int count;
        long timestamp;

        public RequestData(int count, long timestamp) {
            this.count = count;
            this.timestamp = timestamp;
        }
    }
}
