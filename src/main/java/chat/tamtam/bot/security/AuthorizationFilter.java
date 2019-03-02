package chat.tamtam.bot.security;

import chat.tamtam.bot.domain.SessionEntity;
import chat.tamtam.bot.domain.UserAuthEntity;
import chat.tamtam.bot.domain.UserEntity;
import chat.tamtam.bot.repository.SessionRepository;
import chat.tamtam.bot.repository.UserRepository;
import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;

import static chat.tamtam.bot.security.SecurityConstants.*;
import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

public class AuthorizationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public AuthorizationFilter(
            final AuthenticationManager authenticationManager,
            final SessionRepository sessionRepository,
            final UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest request,
                                                final HttpServletResponse response) throws AuthenticationException {
        try {
            UserAuthEntity userAuthEntity = new ObjectMapper()
                    .readValue(request.getInputStream(), UserAuthEntity.class);

            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userAuthEntity.getLogin(),
                            userAuthEntity.getPassword(),
                            Collections.emptyList()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(final HttpServletRequest request,
                                            final HttpServletResponse response,
                                            final FilterChain chain,
                                            final Authentication authResult) {
        Date expireDate = new Date(System.currentTimeMillis() + EXPIRATION_TIME);
        User principal = (User) authResult.getPrincipal();
        String token = TOKEN_PREFIX + JWT.create()
                .withSubject(principal.getUsername())
                .withExpiresAt(expireDate)
                .sign(HMAC512(SECRET.getBytes()));
        UserEntity user = userRepository.findByLogin(principal.getUsername());
        sessionRepository.save(new SessionEntity(token, user.getId(), user.getLogin(), expireDate));
        response.addHeader(HEADER_STRING, token);
    }
}
