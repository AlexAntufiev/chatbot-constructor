package chat.tamtam.bot.domain.broadcast.message.attachment;

import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

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
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Entity
@NoArgsConstructor
@Table
public class BroadcastMessageAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Setter(value = AccessLevel.NONE)
    @Column(name = "type", nullable = false)
    private byte type;
    @Setter(value = AccessLevel.NONE)
    @Column(name = "attachmentIdentifier", nullable = false)
    private byte[] attachmentIdentifier;
    @Column(name = "broadcastMessageId")
    private Long broadcastMessageId;
    @Column(name = "title")
    private String title;

    public BroadcastMessageAttachment(
            final @NotNull String type,
            final @NotNull String identifier,
            final long broadcastMessageId,
            final String title
    ) {
        if (StringUtils.isEmpty(type)) {
            throw new UpdateBroadcastMessageException(
                    "Attachment type is null",
                    Error.ATTACHMENT_TYPE_ILLEGAL
            );
        }

        UploadType uploadType = getUploadType(type);

        switch (uploadType) {
            case PHOTO:
                attachmentIdentifier = identifier.getBytes();
                this.type = TYPE_TO_BYTE.get(uploadType);
                break;
            case VIDEO:
                attachmentIdentifier = identifierAsLongBytes(identifier);
                this.type = TYPE_TO_BYTE.get(uploadType);
                break;
            case FILE:
                attachmentIdentifier = identifierAsLongBytes(identifier);
                this.type = TYPE_TO_BYTE.get(uploadType);
                break;
            case AUDIO:
                attachmentIdentifier = identifierAsLongBytes(identifier);
                this.type = TYPE_TO_BYTE.get(uploadType);
                break;
            default:
                throw new UpdateBroadcastMessageException(
                        String.format("Attachment has illegal type=%s", uploadType),
                        Error.ATTACHMENT_TYPE_ILLEGAL
                );
        }

        this.broadcastMessageId = broadcastMessageId;
        this.title = title;
    }

    public UploadType getUploadType() {
        UploadType uploadType = BYTE_TO_TYPE.get(type);
        if (uploadType == null) {
            throw new IllegalStateException(
                    String.format(
                            "No type matches to id=",
                            id
                    )
            );
        }
        return uploadType;
    }

    private static @NotNull UploadType getUploadType(final String type) {
        try {
            return UploadType.create(type);
        } catch (IllegalArgumentException e) {
            throw new UpdateBroadcastMessageException(
                    String.format("Can't convert attachment type because %s", e.getLocalizedMessage()),
                    Error.ATTACHMENT_TYPE_ILLEGAL
            );
        }
    }

    private static @NotNull byte[] identifierAsLongBytes(final @NotNull String identifier) {
        try {
            return ByteBuffer
                    .allocate(Long.BYTES)
                    .putLong(Long.parseLong(identifier))
                    .array();
        } catch (NumberFormatException e) {
            throw new UpdateBroadcastMessageException(
                    String.format(
                            "Can't convert identifier=%s to long",
                            identifier
                    ),
                    Error.ATTACHMENT_IDENTIFIER_IS_NOT_VALID
            );
        }
    }

    private static final Map<UploadType, Byte> TYPE_TO_BYTE;
    private static final Map<Byte, UploadType> BYTE_TO_TYPE;

    static {
        TYPE_TO_BYTE = new EnumMap<>(UploadType.class);
        BYTE_TO_TYPE = new HashMap<>();
        byte typeNumber = 0;
        for (UploadType type
                : UploadType.values()) {
            if (TYPE_TO_BYTE.containsKey(type)) {
                throw new IllegalStateException(
                        String.format(
                                "TYPE_TO_BYTE already contains %s",
                                type.toString()
                        )
                );
            }
            if (BYTE_TO_TYPE.containsKey(typeNumber)) {
                throw new IllegalStateException(
                        String.format(
                                "BYTE_TO_TYPE already contains %d",
                                typeNumber
                        )
                );
            }
            TYPE_TO_BYTE.put(type, typeNumber);
            BYTE_TO_TYPE.put(typeNumber, type);
            typeNumber++;
        }
    }
}
