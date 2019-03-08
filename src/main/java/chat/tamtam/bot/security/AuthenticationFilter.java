package chat.tamtam.bot.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import chat.tamtam.bot.controller.Endpoints;
import chat.tamtam.bot.domain.SessionEntity;
import chat.tamtam.bot.repository.SessionRepository;
import chat.tamtam.bot.service.UserDetailsServiceImpl;

import static chat.tamtam.bot.security.SecurityConstants.TOKEN_PREFIX;

public class AuthenticationFilter extends BasicAuthenticationFilter {
    private final SessionRepository sessionRepository;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthenticationFilter(
            final AuthenticationManager authManager,
            final SessionRepository sessionRepository,
            final UserDetailsServiceImpl userDetailsService
    ) {
        super(authManager);
        this.sessionRepository = sessionRepository;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain chain
    ) throws IOException, ServletException {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token == null || !token.startsWith(TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        if (request.getCookies() != null) {
            SessionEntity session = sessionRepository.findByToken(token);
            Optional<Cookie> authCookie = Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals(SecurityConstants.COOKIE_AUTH))
                    .findFirst();
            if (session == null && authCookie.isPresent()) {
                response.sendRedirect(Endpoints.API_LOGOUT);
                return;
            }
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(final HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (token == null) {
            return null;
        }
        SessionEntity session = sessionRepository.findByToken(token);
        if (session == null) {
            return null;
        }
        UserDetails user = userDetailsService.loadUserByUsername(session.getLogin());
        return new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    }
}
