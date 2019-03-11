package chat.tamtam.bot.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
                .antMatchers(HttpMethod.POST, Endpoints.API_REGISTRATION, Endpoints.API_LOGIN)
                .permitAll()
                .antMatchers(HttpMethod.GET, Endpoints.STATIC_INDEX, Endpoints.STATIC_RESOURCES, Endpoints.HEALTH)
                .permitAll()
                .antMatchers(SwaggerConfig.SWAGGER_URLS)
                .permitAll()
                .antMatchers(Endpoints.TAM_BOT_WEBHOOK)
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .loginPage(Endpoints.STATIC_INDEX)
                .failureForwardUrl(Endpoints.STATIC_INDEX)
                .and()
                .logout()
                .addLogoutHandler(new CookieClearingLogoutHandler(
                        SecurityConstants.COOKIE_AUTH,
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
        AuthorizationFilter jwtAuthorizationFilter =
                new AuthorizationFilter(authenticationManager(), sessionRepository, userRepository);
        jwtAuthorizationFilter.setFilterProcessesUrl(Endpoints.API_LOGIN);
        return jwtAuthorizationFilter;
    }
}
