package chat.tamtam.bot.configuration;

import org.springframework.core.env.Profiles;

public interface AppProfiles {
    String DEVELOPMENT = "development";
    String TEST = "test";
    String PRODUCTION = "production";

    static Profiles noDevelopmentProfiles(){
        return Profiles.of(TEST, PRODUCTION);
    }
}
