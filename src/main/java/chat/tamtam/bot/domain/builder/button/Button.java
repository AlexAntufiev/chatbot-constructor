package chat.tamtam.bot.domain.builder.button;

import java.nio.ByteBuffer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Button {
    private static final ObjectWriter OBJECT_WRITER = new ObjectMapper().writerWithDefaultPrettyPrinter();

    private String value;
    private String text;
    private String intent;
    private Long nextState;

    @JsonIgnore
    public byte[] getBytes() throws JsonProcessingException {
        final byte[] payload = OBJECT_WRITER.writeValueAsString(new ButtonPayload(nextState, value)).getBytes();
        final byte[] text = this.text.getBytes();
        int bytesLength =
                Integer.BYTES
                        + payload.length
                        + Integer.BYTES
                        + text.length;
        byte[] bytes = new byte[bytesLength];
        return ByteBuffer
                .wrap(bytes)
                .putInt(payload.length)
                .put(payload)
                .putInt(text.length)
                .put(text)
                .array();
    }
}
