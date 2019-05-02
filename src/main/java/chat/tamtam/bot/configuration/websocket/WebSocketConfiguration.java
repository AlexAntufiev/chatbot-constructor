package chat.tamtam.bot.configuration.websocket;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import chat.tamtam.bot.domain.session.SessionEntity;
import chat.tamtam.bot.repository.SessionRepository;
import chat.tamtam.bot.security.SecurityConstants;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {
    private final SessionRepository sessionRepository;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry
                .enableSimpleBroker(
                        SimpleBrokerDestinationPrefix.QUEUE,
                        SimpleBrokerDestinationPrefix.USER
                );
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint(WebSocketEndpoint.WEB_SOCKET)
                .setHandshakeHandler(new WebSocketHandshakeHandler())
                .setAllowedOrigins("*")
                .withSockJS();
    }

    private class WebSocketHandshakeHandler extends DefaultHandshakeHandler {
        @Override
        protected Principal determineUser(
                ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes
        ) {
            ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
            HttpServletRequest httpServletRequest = servletServerHttpRequest.getServletRequest();
            if (httpServletRequest.getCookies() == null) {
                return null;
            }
            Optional<Cookie> authCookie =
                    List.of(httpServletRequest.getCookies())
                            .stream()
                            .filter(e -> e.getName().equals(SecurityConstants.COOKIE_AUTH))
                            .findFirst();
            if (authCookie.isEmpty()) {
                return null;
            }
            return getPrincipal(authCookie.get());
        }

        private Principal getPrincipal(final Cookie authCookie) {
            SessionEntity session = sessionRepository.findByToken(authCookie.getValue());
            if (session == null) {
                return null;
            }
            return new WebSocketPrincipal(session.getUserId().toString());
        }
    }
}
