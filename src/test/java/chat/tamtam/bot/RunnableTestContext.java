package chat.tamtam.bot;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import chat.tamtam.bot.configuration.Profiles;

@ActiveProfiles(Profiles.TEST)
@SpringBootTest
public class RunnableTestContext extends TestContext {

}
