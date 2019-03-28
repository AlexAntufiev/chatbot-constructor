package chat.tamtam.bot.domain.broadcast.message.attachment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;

import chat.tamtam.bot.domain.exception.UpdateBroadcastMessageException;
import chat.tamtam.bot.service.Error;
import chat.tamtam.botapi.model.UploadType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "BroadcastMessageAttachment")
public class BroadcastMessageAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "type", nullable = false)
    private String type;
    @Column(name = "token", nullable = false)
    private String token;
    @Column(name = "broadcastMessageId", nullable = true)
    private Long broadcastMessageId;

    public BroadcastMessageAttachment(
            final @NotNull String type,
            final @NotNull String token,
            final long broadcastMessageId
    ) {
        this.type = validateType(type);
        this.token = token;
        this.broadcastMessageId = broadcastMessageId;
    }

    private static String validateType(final String type) {
        try {
            if (StringUtils.isEmpty(type)) {
                throw new UpdateBroadcastMessageException(
                        "Attachment type is null",
                        Error.ATTACHMENT_TYPE_ILLEGAL
                );
            }
            UploadType.create(type);
            return type;
        } catch (IllegalArgumentException e) {
            throw new UpdateBroadcastMessageException(
                    String.format("Attachment has illegal type=%s", type),
                    Error.ATTACHMENT_TYPE_ILLEGAL
            );
        }
    }
}
