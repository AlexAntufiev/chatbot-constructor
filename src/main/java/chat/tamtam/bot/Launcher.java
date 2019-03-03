package chat.tamtam.bot;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import chat.tamtam.bot.domain.UserAuthEntity;
import chat.tamtam.bot.service.UserService;
import lombok.AllArgsConstructor;

/**
 * @author Aleksey.Antufev
 * @since 1.0 19/02/2019
 */
@SpringBootApplication
@AllArgsConstructor
public class Launcher {

    private final UserService userService;

    public static void main(final String[] args) {
        SpringApplication.run(Launcher.class, args);
    }

    @Bean
    @Profile("dev")
    InitializingBean populateDatabase() {
        return () -> userService.addUser(new UserAuthEntity("admin", "admin"));
    }
}
