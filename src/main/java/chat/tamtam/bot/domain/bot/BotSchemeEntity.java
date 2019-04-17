package chat.tamtam.bot.domain.bot;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "BotSchema")
@NoArgsConstructor
@Data
@RequiredArgsConstructor
public class BotSchemeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "userId")
    private @NonNull Long userId;

    @Column(name = "botId")
    private Long botId;

    @Column(name = "name")
    private @NonNull String name;

    @Column(name = "schema")
    private Long schema;
}
