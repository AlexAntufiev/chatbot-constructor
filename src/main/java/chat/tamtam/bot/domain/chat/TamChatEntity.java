package chat.tamtam.bot.domain.chat;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import chat.tamtam.botapi.model.Chat;
import chat.tamtam.botapi.model.ChatType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class TamChatEntity {
    @EmbeddedId
    private Id id;
    @Column(name = "type", nullable = false)
    private ChatType type;
    @Column(name = "title")
    private String title;
    @Column(name = "iconUrl")
    private String iconUrl;
    @Column(name = "link")
    private String link;

    public TamChatEntity(
            final @NotNull Integer botSchemeId,
            final @NotNull Long tamBotId,
            final @NotNull Chat chat
    ) {
        id = new Id(chat.getChatId(), botSchemeId, tamBotId);
        type = chat.getType();
        title = chat.getTitle();
        if (chat.getIcon() != null) {
            iconUrl = chat.getIcon().getUrl();
        } else {
            iconUrl = null;
        }
        link = chat.getLink();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class Id implements Serializable {
        @Column(name = "chatId", nullable = false)
        private Long chatId;
        @Column(name = "botSchemeId", nullable = false)
        private Integer botSchemeId;
        @Column(name = "tamBotId", nullable = false)
        private Long tamBotId;
    }
}
