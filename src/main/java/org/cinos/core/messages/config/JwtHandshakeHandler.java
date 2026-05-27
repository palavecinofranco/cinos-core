package org.cinos.core.messages.config;

import jakarta.servlet.http.HttpServletRequest;
import org.cinos.core.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@RequiredArgsConstructor
public class JwtHandshakeHandler extends DefaultHandshakeHandler {

    private final JwtService jwtService;

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String token = extractToken(httpRequest);

            if (token != null) {
                try {
                    String username = jwtService.extractUsername(token);
                    return () -> username; // Devuelve un Principal con el username como nombre
                } catch (Exception e) {
                    System.out.println("Token inválido en el WebSocket handshake ❌");
                }
            }
        }
        return null; // No se puede autenticar
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}


