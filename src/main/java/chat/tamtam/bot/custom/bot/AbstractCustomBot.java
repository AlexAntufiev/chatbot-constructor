package chat.tamtam.bot.custom.bot;

import java.util.List;

import chat.tamtam.botapi.model.AttachmentRequest;
import chat.tamtam.botapi.model.NewMessageBody;
import chat.tamtam.botapi.model.Update;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractCustomBot {
    protected final String id;
    protected final String token;
    protected final String host;

    public void process(Update update) {
        throw new UnsupportedOperationException("from " + getType());
    }

    public String getId() {
        throw new UnsupportedOperationException("from " + getType());
    }

    public abstract BotType getType();

    public String getToken() {
        throw new UnsupportedOperationException("from " + getType());
    }

    protected static NewMessageBody messageOf(String message) {
        return new NewMessageBody(message, null);
    }

    protected static NewMessageBody messageOf(String message, List<AttachmentRequest> attachments) {
        return new NewMessageBody(message, attachments);
    }
}
