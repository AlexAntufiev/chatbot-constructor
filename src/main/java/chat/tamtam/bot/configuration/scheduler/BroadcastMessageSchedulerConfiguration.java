package chat.tamtam.bot.configuration.scheduler;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class BroadcastMessageSchedulerConfiguration {
    @Value("${tamtam.broadcast.executor.corePoolSize:1}")
    private int corePoolSize;
    @Value("${tamtam.broadcast.executor.maxPoolSize:1}")
    private int maxPoolSize;

    @Bean
    public Executor scheduledMessagesExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setThreadNamePrefix("ScheduledBroadcastMessageProcess-");
        executor.initialize();
        return executor;
    }
}
