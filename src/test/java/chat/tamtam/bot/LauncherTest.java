package chat.tamtam.bot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LauncherTest extends RunnableTestContext {

    @Test
    void testRun() {
        Assertions.assertDoesNotThrow(() -> Launcher.main(new String[]{}));
    }
}
