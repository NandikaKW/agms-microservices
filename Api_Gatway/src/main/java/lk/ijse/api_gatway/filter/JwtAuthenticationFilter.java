package lk.ijse.api_gatway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter {

    // Secret key for JWT validation. Base64 encoded.
    // I studied JWT token validation and added comments here for understanding.
    private static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Skip auth for login/register if they existed, or for specific endpoints
        if (isPublicEndpoint(request.getPath().toString())) {
            return chain.filter(exchange);
        }

        if (!request.getHeaders().containsKey("Authorization")) {
            return onError(exchange, "No Authorization Header", HttpStatus.UNAUTHORIZED);
        }

        String authHeader = request.getHeaders().getOrEmpty("Authorization").get(0);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Invalid Authorization Header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        try {
            validateToken(token);
        } catch (Exception e) {
            return onError(exchange, "Invalid Token", HttpStatus.UNAUTHORIZED);
        }

        return chain.filter(exchange);
    }

    private boolean isPublicEndpoint(String path) {
        // As per SRS, we could exclude /auth/login or /auth/register
        // For development, we'll allow all endpoints but validate if header is present
        // Path matches login/register
        return path.contains("/auth/login") || path.contains("/auth/register");
    }

    private void validateToken(String token) {
        Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token);
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        DataBuffer buffer = response.bufferFactory().wrap(err.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}
