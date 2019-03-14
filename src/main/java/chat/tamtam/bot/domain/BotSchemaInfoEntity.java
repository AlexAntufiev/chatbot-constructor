package chat.tamtam.bot.domain;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import chat.tamtam.botapi.model.UserWithPhoto;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "BotSchemaInfo")
@NoArgsConstructor
public class BotSchemaInfoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "botId")
    private Long botId;
    @Column(name = "name")
    private String name;
    @Column(name = "username")
    private String username;
    @Column(name = "avatarUrl")
    private String avatarUrl;
    @Column(name = "fullAvatarUrl")
    private String fullAvatarUrl;

    public BotSchemaInfoEntity(final UserWithPhoto userWithPhoto) {
        botId = userWithPhoto.getUserId();
        name = userWithPhoto.getName();
        username = userWithPhoto.getUsername();
        avatarUrl = userWithPhoto.getAvatarUrl();
        fullAvatarUrl = userWithPhoto.getFullAvatarUrl();
    }

    public void update(final UserWithPhoto userWithPhoto) {
        if (!Objects.equals(botId, userWithPhoto.getUserId())) {
            botId = userWithPhoto.getUserId();
        }
        if (!Objects.equals(name, userWithPhoto.getName())) {
            name = userWithPhoto.getName();
        }
        if (!Objects.equals(username, userWithPhoto.getUsername())) {
            username = userWithPhoto.getUsername();
        }
        if (!Objects.equals(avatarUrl, userWithPhoto.getAvatarUrl())) {
            avatarUrl = userWithPhoto.getAvatarUrl();
        }
        if (!Objects.equals(fullAvatarUrl, userWithPhoto.getFullAvatarUrl())) {
            fullAvatarUrl = userWithPhoto.getFullAvatarUrl();
        }
    }
}
