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
import lombok.RequiredArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "BroadcastMessage")
public class MessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(name = "botSchemeId")
    private Integer botSchemeId;
    @Column(name = "tamBotId")
    private Long tamBotId;
    @Column(name = "chatChannelId")
    private Long chatChannelId;
    @Column(name = "firingTime")
    private LocalDateTime firingTime;
    @Column(name = "text")
    private String text;
    @Column(name = "payload")
    private Object payload;
    @Column(name = "state")
    private int state;
    @Column(name = "error")
    private String error;
}
