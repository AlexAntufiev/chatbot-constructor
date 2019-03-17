package chat.tamtam.bot.domain.response;

import javax.validation.constraints.NotNull;

import chat.tamtam.botapi.model.Chat;
import chat.tamtam.botapi.model.ChatType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TamChatResponse {
    private Long chatId;
    private ChatType type;
    private String title;
    private String iconUrl;
    private String link;
    private Object description;

    public TamChatResponse(final @NotNull Chat chat) {
        chatId = chat.getChatId();
        type = chat.getType();
        title = chat.getTitle();
        if (chat.getIcon() != null) {
            iconUrl = chat.getIcon().getUrl();
        } else {
            iconUrl = null;
        }
        link = chat.getLink();
        description = chat.getDescription();
    }
}
