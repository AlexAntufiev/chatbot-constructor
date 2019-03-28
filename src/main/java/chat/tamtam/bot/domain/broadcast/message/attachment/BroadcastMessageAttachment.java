package chat.tamtam.bot.domain.broadcast.message.attachment;

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

@Data
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "BroadcastMessageAttachment")
public class BroadcastMessageAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "type", nullable = false)
    private @NonNull String type;
    @Column(name = "token", nullable = false)
    private @NonNull String token;
    @Column(name = "broadcastMessageId", nullable = false)
    private @NonNull Long broadcastMessageId;

    public void setType(final String type) {
        System.out.println("TYPE: " + type);
        this.type = type;
    }
}
