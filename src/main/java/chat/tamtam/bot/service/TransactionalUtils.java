package chat.tamtam.bot.service;

import java.util.concurrent.Callable;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
public class TransactionalUtils {
    public void invokeRunnable(final Runnable runnable) {
        runnable.run();
    }

    public Object invokeCallable(Callable<?> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
