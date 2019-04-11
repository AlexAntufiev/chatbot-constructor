package chat.tamtam.bot.domain.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NotificationType {
    BROADCAST_MESSAGE_NOTIFICATION((byte) 0);

    @Getter
    private final byte type;
}
