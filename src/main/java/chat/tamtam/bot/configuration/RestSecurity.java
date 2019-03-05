package chat.tamtam.bot.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import chat.tamtam.bot.controller.Endpoints;
import chat.tamtam.bot.repository.SessionRepository;
import chat.tamtam.bot.repository.UserRepository;
import chat.tamtam.bot.security.AuthenticationFilter;
import chat.tamtam.bot.security.AuthorizationFilter;
import chat.tamtam.bot.service.UserDetailsServiceImpl;
import lombok.AllArgsConstructor;

import static chat.tamtam.bot.security.SecurityConstants.SIGN_UP_URL;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class RestSecurity extends WebSecurityConfigurerAdapter {
    private final UserDetailsServiceImpl userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable().authorizeRequests()
                .antMatchers(HttpMethod.POST, SIGN_UP_URL, Endpoints.API_LOGIN).permitAll()
                .antMatchers(HttpMethod.GET, Endpoints.STATIC_INDEX, Endpoints.STATIC_RESOURCES).permitAll()
                .antMatchers(Endpoints.API_LOGIN, Endpoints.STATIC_INDEX, Endpoints.HEALTH).permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilter(jwtAuthenticationFilter())
                .addFilter(jwtAuthorizationFilter())
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    public void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }

    private AuthenticationFilter jwtAuthenticationFilter() throws Exception {
        return new AuthenticationFilter(authenticationManager(), sessionRepository, userDetailsService);
    }

    private AuthorizationFilter jwtAuthorizationFilter() throws Exception {
        AuthorizationFilter jwtAuthorizationFilter =
                new AuthorizationFilter(authenticationManager(), sessionRepository, userRepository);
        jwtAuthorizationFilter.setFilterProcessesUrl(Endpoints.API_LOGIN);
        return jwtAuthorizationFilter;
    }
}
