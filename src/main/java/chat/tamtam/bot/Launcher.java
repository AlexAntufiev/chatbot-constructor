package chat.tamtam.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;

@SpringBootApplication
public class Launcher {

    public static void main(final String[] args) {
        SpringApplication springApplication = new SpringApplication(Launcher.class);
        springApplication.addListeners(new ApplicationPidFileWriter("chatbot-constructor.pid"));
        springApplication.run(args);
    }
}
