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
import chat.tamtam.bot.configuration.websocket.WebSocketEndpoint;
import chat.tamtam.bot.controller.Endpoint;
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
                .antMatchers(HttpMethod.POST,
                        Endpoint.API_REGISTRATION,
                        Endpoint.API_LOGIN,
                        Endpoint.TAM_CUSTOM_BOT_WEBHOOK + Endpoint.ID,
                        Endpoint.TAM_BOT + Endpoint.ID
                )
                .permitAll()
                .antMatchers(HttpMethod.GET, Endpoint.STATIC_INDEX, Endpoint.STATIC_RESOURCES, Endpoint.ACTUATOR)
                .permitAll()
                .antMatchers(SwaggerConfig.SWAGGER_URLS)
                .permitAll()
                .antMatchers(HttpMethod.GET, WebSocketEndpoint.WEB_SOCKET + "/**")
                .permitAll()
                .antMatchers(HttpMethod.GET, Endpoint.RESOURCES + Endpoint.REGISTRATION_BOT_URL)
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .loginPage(Endpoint.STATIC_INDEX)
                .failureForwardUrl(Endpoint.STATIC_INDEX)
                .and()
                .logout()
                .addLogoutHandler(new CookieClearingLogoutHandler(
                        SecurityConstants.COOKIE_AUTH,
                        SecurityConstants.COOKIE_USER_ID
                ))
                .logoutSuccessUrl(Endpoint.STATIC_INDEX)
                .logoutUrl(Endpoint.API_LOGOUT)
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
        jwtAuthorizationFilter.setFilterProcessesUrl(Endpoint.API_LOGIN);
        return jwtAuthorizationFilter;
    }
}
