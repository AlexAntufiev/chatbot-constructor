package chat.tamtam.bot.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@RefreshScope
@Configuration
public class DiscoveryProperties {

    @Getter
    @Value("${tamtam.webhook.elama.ids:}")
    private String elamaEnabledIds;

    @Getter
    @Value("${tamtam.webhook.elama.schemeId:}")
    private Long elamaSchemeId;

}
