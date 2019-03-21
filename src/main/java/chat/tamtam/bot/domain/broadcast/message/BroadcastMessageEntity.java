package chat.tamtam.bot.domain.broadcast.message;

import java.sql.Timestamp;

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
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "firingTime", nullable = false)
    private Timestamp firingTime;
    @Column(name = "erasingTime")
    private Timestamp erasingTime;
    @Column(name = "mid")
    private String messageId;
    @Column(name = "text")
    private String text;
    @Column(name = "state")
    private Byte state;
    @Column(name = "error")
    private String error;
    // @todo #CC-63 Add payload field into BroadcastMessageEntity

    public void setState(final BroadcastMessageState state) {
        this.state = state.getValue();
    }
}
