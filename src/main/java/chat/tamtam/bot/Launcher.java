package chat.tamtam.bot;

import java.time.ZoneOffset;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.AllArgsConstructor;

@EnableDiscoveryClient
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

    @PostConstruct
    public void setDefaultTimeZoneToUTC() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC.normalized()));
    }
}
