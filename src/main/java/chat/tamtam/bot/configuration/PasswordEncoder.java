package chat.tamtam.bot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordEncoder {
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        if (bCryptPasswordEncoder == null) {
            bCryptPasswordEncoder = new BCryptPasswordEncoder();
        }
        return bCryptPasswordEncoder;
    }
}
