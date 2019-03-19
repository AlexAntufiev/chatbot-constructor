package chat.tamtam.bot.domain.chatchannel;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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
        link = chat.getLink();
    }

    public void setOptions(final Chat chat, final ChatMember chatMember) {
        int opts = 0;
        setOption(opts, chat.getType() == ChatType.CHANNEL, ChatChannelOption.CHANNEL);
        if (chatMember.getPermissions() != null) {
            setOption(
                    opts,
                    chatMember.getPermissions().contains(ChatAdminPermission.WRITE),
                    ChatChannelOption.WRITABLE
            );
        }
        options = opts;
    }

    public Set<ChatChannelOption> getOptions() {
        Set<ChatChannelOption> optionsSet = new HashSet<>();
        if (hasOption(ChatChannelOption.CHANNEL)) {
            optionsSet.add(ChatChannelOption.CHANNEL);
        }
        if (hasOption(ChatChannelOption.WRITABLE)) {
            optionsSet.add(ChatChannelOption.WRITABLE);
        }
        return optionsSet;
    }

    private static void setOption(int options, boolean condition, ChatChannelOption option) {
        options |= condition ? option.getValue() : 0;
    }

    private boolean hasOption(ChatChannelOption option) {
        return (options & option.getValue()) == 1;
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
