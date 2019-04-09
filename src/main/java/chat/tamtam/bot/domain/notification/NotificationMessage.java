package chat.tamtam.bot.domain.notification;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotificationMessage {
    private byte type;
    private Object payload;

    public NotificationMessage(final NotificationType type, final Object payload) {
        this.type = type.getType();
        this.payload = payload;
    }
}
