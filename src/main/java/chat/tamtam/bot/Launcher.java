package chat.tamtam.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.AllArgsConstructor;

@SpringBootApplication
@AllArgsConstructor
@EnableScheduling
@EnableAsync
public class Launcher {

    public static void main(final String[] args) {
        SpringApplication springApplication = new SpringApplication(Launcher.class);
        springApplication.addListeners(new ApplicationPidFileWriter("chatbot-constructor.pid"));
        springApplication.run(args);
    }
}
