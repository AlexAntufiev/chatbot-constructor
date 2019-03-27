package chat.tamtam.bot.domain.chatchannel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import chat.tamtam.botapi.model.Chat;
import chat.tamtam.botapi.model.ChatAdminPermission;
import chat.tamtam.botapi.model.ChatMember;
import chat.tamtam.botapi.model.ChatType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class ChatChannelEntity {
    @EmbeddedId
    private Id id;
    @Column(name = "options", nullable = false)
    private int options;
    @Column(name = "title")
    private String title;
    @Column(name = "iconUrl")
    private String iconUrl;
    @Column(name = "link")
    private String link;
    @Column(name = "description")
    private String description;

    public ChatChannelEntity(
            final @NotNull Integer botSchemeId,
            final @NotNull Long tamBotId,
            final @NotNull Chat chat
    ) {
        id = new Id(chat.getChatId(), botSchemeId, tamBotId);
        options = 0;
        title = chat.getTitle();
        if (chat.getIcon() != null) {
            iconUrl = chat.getIcon().getUrl();
        } else {
            iconUrl = null;
        }
        if (chat.getDescription() != null) {
            description = chat.getDescription().toString();
        } else {
            description = null;
        }
        link = chat.getLink();
    }

    public void setOptions(final Chat chat, final ChatMember chatMember) {
        int opts = 0;
        opts = ChatChannelOption.setOption(opts, chat.getType() == ChatType.CHANNEL, ChatChannelOption.CHANNEL);
        if (chatMember.getPermissions() != null) {
            opts = ChatChannelOption.setOption(
                    opts,
                    chatMember.getPermissions().contains(ChatAdminPermission.WRITE),
                    ChatChannelOption.WRITABLE
            );
        }
        options = opts;
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
