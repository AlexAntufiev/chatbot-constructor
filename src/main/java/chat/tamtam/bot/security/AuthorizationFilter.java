package chat.tamtam.bot.security;

import chat.tamtam.bot.domain.SessionEntity;
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
import java.util.ArrayList;
import java.util.Date;

import static chat.tamtam.bot.security.SecurityConstants.EXPIRATION_TIME;
import static chat.tamtam.bot.security.SecurityConstants.HEADER_STRING;
import static chat.tamtam.bot.security.SecurityConstants.TOKEN_PREFIX;
import static chat.tamtam.bot.security.SecurityConstants.SECRET;
import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

public class AuthorizationFilter extends UsernamePasswordAuthenticationFilter {
    private AuthenticationManager authenticationManager;
    private SessionRepository sessionRepository;
    private UserRepository userRepository;

    public AuthorizationFilter(
            final AuthenticationManager authenticationManager,
            final SessionRepository sessionRepository,
            final UserRepository userRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest request,
                                                final HttpServletResponse response) throws AuthenticationException {
        try {
            UserEntity userEntity = new ObjectMapper()
                    .readValue(request.getInputStream(), UserEntity.class);

            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userEntity.getUsername(),
                            userEntity.getPassword(),
                            new ArrayList<>())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(final HttpServletRequest request,
                                            final HttpServletResponse response,
                                            final FilterChain chain,
                                            final Authentication auth) {

        Date expireDate = new Date(System.currentTimeMillis() + EXPIRATION_TIME);
        String token = TOKEN_PREFIX + JWT.create()
                .withSubject(((User) auth.getPrincipal()).getUsername())
                .withExpiresAt(expireDate)
                .sign(HMAC512(SECRET.getBytes()));
        UserEntity user = userRepository.findByUsername(((User) auth.getPrincipal()).getUsername());
        this.sessionRepository.save(new SessionEntity(token, user.getId(), user.getUsername(), expireDate));
        response.addHeader(HEADER_STRING, token);
    }
}
