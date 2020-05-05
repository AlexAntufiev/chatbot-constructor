package chat.tamtam.bot.configuration.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@ConditionalOnProperty(
        prefix = "tamtam.broadcast",
        name = "enabled",
        havingValue = "true"
)
public class BroadcastMessageExecutorConfiguration {
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
