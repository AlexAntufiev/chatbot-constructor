package chat.tamtam.bot.configuration.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ConditionalOnProperty(
        prefix = "tamtam.rss",
        name = "enabled",
        havingValue = "true"
)
public class BroadcastFeedExecutorConfiguration {
    @Value("${tamtam.rss.executor.corePoolSize:1}")
    private int corePoolSize;
    @Value("${tamtam.rss.executor.maxPoolSize:1}")
    private int maxPoolSize;

    @Bean
    public ThreadPoolTaskExecutor broadcastFeedExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setThreadNamePrefix("ScheduledBroadcastFeedProcess-");
        executor.initialize();
        return executor;
    }
}
