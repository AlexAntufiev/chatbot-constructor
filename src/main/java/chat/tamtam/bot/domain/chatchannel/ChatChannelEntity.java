package chat.tamtam.bot.domain.chatchannel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import chat.tamtam.botapi.model.Chat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class ChatChannelEntity {
    @EmbeddedId
    private Id id;
    @Column(name = "type", nullable = false)
    private int options;
    @Column(name = "title")
    private String title;
    @Column(name = "iconUrl")
    private String iconUrl;
    @Column(name = "link")
    private String link;

    public ChatChannelEntity(
            final @NotNull Integer botSchemeId,
            final @NotNull Long tamBotId,
            final @NotNull Chat chat
    ) {
        id = new Id(chat.getChatId(), botSchemeId, tamBotId);
        // @todo #CC-63 add chatchannel options mapping
        options = 0;
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
