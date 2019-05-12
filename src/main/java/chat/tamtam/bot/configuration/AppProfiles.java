package chat.tamtam.bot.configuration;

import org.springframework.core.env.Profiles;

public interface AppProfiles {
    String DEVELOPMENT = "dev";
    String TEST = "test";
    String PRODUCTION = "prod";

    static Profiles noDevelopmentProfiles() {
        return Profiles.of(TEST, PRODUCTION);
    }
}
