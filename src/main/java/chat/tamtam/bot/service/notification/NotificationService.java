package chat.tamtam.bot.service.notification;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import chat.tamtam.bot.configuration.websocket.SimpleBrokerDestinationPrefix;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    public void notifyUser(final long userId, final Object message) {
        simpMessagingTemplate.convertAndSendToUser(
                Long.toString(userId),
                SimpleBrokerDestinationPrefix.QUEUE + NotificationEndpoint.UPDATES_TOPIC,
                message
        );
    }
}
