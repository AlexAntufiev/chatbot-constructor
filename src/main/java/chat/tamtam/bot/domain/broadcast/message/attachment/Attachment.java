package chat.tamtam.bot.domain.broadcast.message.attachment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "BroadcastAttachment")
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "setIdentifier", nullable = false)
    private Long setIdentifier;
    @Column(name = "type", nullable = false)
    private String type;
    @Column(name = "token", nullable = false)
    private String token;
}
