package chat.tamtam.bot.domain;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.jetbrains.annotations.NotNull;

import chat.tamtam.botapi.model.UserWithPhoto;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "TamBot")
@NoArgsConstructor
public class TamBotEntity {
    @EmbeddedId
    private TamBotId id;
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
            final @NotNull Integer userId,
            final @NotNull String token,
            final @NotNull UserWithPhoto userWithPhoto
    ) {
        id = new TamBotId(userWithPhoto.getUserId(), userId);
        this.token = token;
        name = userWithPhoto.getName();
        username = userWithPhoto.getUsername();
        avatarUrl = userWithPhoto.getAvatarUrl();
        fullAvatarUrl = userWithPhoto.getFullAvatarUrl();
    }
}
