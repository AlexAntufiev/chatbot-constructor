package chat.tamtam.bot.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RefreshScope
@Configuration
public class DiscoveryProperties {

    @Getter
    @Value("${tamtam.webhook.eLama.ids:}")
    private String enabledIds;
}
