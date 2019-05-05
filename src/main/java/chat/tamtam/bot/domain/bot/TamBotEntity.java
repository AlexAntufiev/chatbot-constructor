package chat.tamtam.bot.domain.bot;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.jetbrains.annotations.NotNull;

import chat.tamtam.botapi.model.UserWithPhoto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table
@NoArgsConstructor
public class TamBotEntity {
    @EmbeddedId
    private Id id;
    @Column(name = "token", nullable = false)
    private String token;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "username", nullable = false)
    private String username;
    @Column(name = "avatarUrl")
    private String avatarUrl;
    @Column(name = "fullAvatarUrl")
    private String fullAvatarUrl;

    public TamBotEntity(
            final @NotNull Long userId,
            final @NotNull String token,
            final @NotNull UserWithPhoto userWithPhoto
    ) {
        id = new Id(userWithPhoto.getUserId(), userId);
        this.token = token;
        name = userWithPhoto.getName();
        username = userWithPhoto.getUsername();
        avatarUrl = userWithPhoto.getAvatarUrl();
        fullAvatarUrl = userWithPhoto.getFullAvatarUrl();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class Id implements Serializable {
        @Column(name = "botId", nullable = false)
        private Long botId;
        @Column(name = "userId", nullable = false)
        private Long userId;
    }
}
