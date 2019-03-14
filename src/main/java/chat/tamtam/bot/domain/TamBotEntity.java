package chat.tamtam.bot.domain;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

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
    @Column(name = "token")
    private String token;
    @Column(name = "name")
    private String name;
    @Column(name = "username")
    private String username;
    @Column(name = "avatarUrl")
    private String avatarUrl;
    @Column(name = "fullAvatarUrl")
    private String fullAvatarUrl;

    public TamBotEntity(final Integer userId, final String token, final UserWithPhoto userWithPhoto) {
        id = new TamBotId(userWithPhoto.getUserId(), userId);
        this.token = token;
        name = userWithPhoto.getName();
        username = userWithPhoto.getUsername();
        avatarUrl = userWithPhoto.getAvatarUrl();
        fullAvatarUrl = userWithPhoto.getFullAvatarUrl();
    }
}
