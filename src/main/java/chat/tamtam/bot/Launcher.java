package chat.tamtam.bot;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import chat.tamtam.bot.configuration.Profiles;
import chat.tamtam.bot.domain.user.UserAuthEntity;
import chat.tamtam.bot.service.UserService;
import lombok.AllArgsConstructor;

@SpringBootApplication
@AllArgsConstructor
@EnableScheduling
@EnableAsync
public class Launcher {

    private final UserService userService;

    public static void main(final String[] args) {
        SpringApplication springApplication = new SpringApplication(Launcher.class);
        springApplication.addListeners(new ApplicationPidFileWriter("chatbot-constructor.pid"));
        springApplication.run(args);
    }

    @Bean
    @Profile(Profiles.DEVELOPMENT)
    InitializingBean populateDatabase() {
        return () -> userService.addUser(new UserAuthEntity("admin", "admin"));
    }
}
