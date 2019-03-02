package chat.tamtam.bot.security;

import chat.tamtam.bot.domain.SessionEntity;
import chat.tamtam.bot.repository.SessionRepository;
import chat.tamtam.bot.service.UserDetailsServiceImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static chat.tamtam.bot.security.SecurityConstants.HEADER_STRING;
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
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader(HEADER_STRING);

        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(final HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);
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
