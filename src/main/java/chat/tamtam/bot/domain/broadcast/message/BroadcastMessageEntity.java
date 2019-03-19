package chat.tamtam.bot.domain.broadcast.message;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "BroadcastMessage")
public class BroadcastMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "botSchemeId", nullable = false)
    private Integer botSchemeId;
    @Column(name = "tamBotId", nullable = false)
    private Long tamBotId;
    @Column(name = "chatChannelId", nullable = false)
    private Long chatChannelId;
    @Column(name = "firingTime", nullable = false)
    private LocalDateTime firingTime;
    @Column(name = "text")
    private String text;
    @Column(name = "state")
    private Byte state;
    @Column(name = "error")
    private String error;
}
