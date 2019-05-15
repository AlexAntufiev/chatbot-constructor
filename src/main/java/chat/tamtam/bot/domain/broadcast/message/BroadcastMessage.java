package chat.tamtam.bot.domain.broadcast.message;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(indexes = {
        @Index(columnList = "firingTime"),
        @Index(columnList = "erasingTime"),
        @Index(columnList = "state")
})
public class BroadcastMessage {
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
    @Column(name = "firingTime")
    private Instant firingTime;
    @Column(name = "erasingTime")
    private Instant erasingTime;
    @Column(name = "mid")
    private String messageId;
    @Column(name = "text", columnDefinition = "text")
    private String text;
    @Column(name = "state")
    private Byte state;
    @Column(name = "error")
    private String error;
    @JsonIgnore
    @Column(name = "weight", nullable = false)
    private byte weight = 0;

    public void setState(final BroadcastMessageState state) {
        this.state = state.getValue();
    }
}
