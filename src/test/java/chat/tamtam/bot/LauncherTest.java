package chat.tamtam.bot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LauncherTest {

    @Test
    void testRun() {
        Launcher.main(new String[]{});
    }
}