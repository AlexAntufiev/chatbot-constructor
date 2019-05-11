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
@Table
@NoArgsConstructor
@Data
@RequiredArgsConstructor
public class BotScheme {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private @NonNull Long userId;

    private Long botId;

    private @NonNull String name;

    private Long schemeEnterState;
    /*@Column(nullable = false)
    private Long schemeResetState;*/
}
