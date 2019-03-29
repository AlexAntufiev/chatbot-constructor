package chat.tamtam.bot.domain.broadcast.message.attachment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum BroadcastMessageWeight {
    PHOTO_ATTACHMENT_WEIGHT((byte) 1),
    VIDEO_ATTACHMENT_WEIGHT((byte) 1),
    AUDIO_ATTACHMENT_WEIGHT((byte) 10),
    FILE_ATTACHMENT_WEIGHT((byte) 10),
    MAX_MESSAGE_WEIGHT((byte) 10),
    ;

    @Getter
    private final byte weight;
}
