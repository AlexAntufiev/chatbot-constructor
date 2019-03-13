package chat.tamtam.bot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;

import chat.tamtam.bot.configuration.swagger.SwaggerConfig;
import chat.tamtam.bot.controller.Endpoints;
import chat.tamtam.bot.repository.SessionRepository;
import chat.tamtam.bot.repository.UserRepository;
import chat.tamtam.bot.security.AuthenticationFilter;
import chat.tamtam.bot.security.AuthorizationFilter;
import chat.tamtam.bot.security.SecurityConstants;
import chat.tamtam.bot.service.UserDetailsServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        http.cors()
                .and()
                .csrf()
                .disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST,
                        Endpoints.API_REGISTRATION,
                        Endpoints.API_LOGIN,
                        Endpoints.TAM_BOT_WEBHOOK + Endpoints.ID
                )
                .permitAll()
                .antMatchers(HttpMethod.GET, Endpoints.STATIC_INDEX, Endpoints.STATIC_RESOURCES, Endpoints.HEALTH)
                .permitAll()
                .antMatchers(SwaggerConfig.SWAGGER_URLS)
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint())
                .and()
                .logout()
                .addLogoutHandler(new CookieClearingLogoutHandler(SecurityConstants.COOKIE_AUTH,
                        SecurityConstants.COOKIE_USER_ID
                ))
                .logoutSuccessUrl(Endpoints.STATIC_INDEX)
                .logoutUrl(Endpoints.API_LOGOUT)
                .and()
                .addFilter(jwtAuthenticationFilter())
                .addFilter(jwtAuthorizationFilter())
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    public void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }

    private AuthenticationFilter jwtAuthenticationFilter() throws Exception {
        return new AuthenticationFilter(authenticationManager(), sessionRepository, userDetailsService);
    }

    private AuthorizationFilter jwtAuthorizationFilter() throws Exception {
        AuthorizationFilter jwtAuthorizationFilter = new AuthorizationFilter(authenticationManager(),
                sessionRepository,
                userRepository
        );
        jwtAuthorizationFilter.setFilterProcessesUrl(Endpoints.API_LOGIN);
        return jwtAuthorizationFilter;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.OK.value());
            response.sendRedirect(Endpoints.STATIC_INDEX);
        };
    }
}
