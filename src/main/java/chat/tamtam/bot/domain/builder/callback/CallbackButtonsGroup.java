package chat.tamtam.bot.domain.builder.callback;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.ArrayUtils;

import chat.tamtam.botapi.model.CallbackButton;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "CallbackButtonsGroup")
public class CallbackButtonsGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private byte[] callbacks;
    @Column(nullable = false)
    private Long componentId;

    /*
     * Integer.BYTES - amount of Lists,
     * [
     *   Integer.BYTES - List size(amount of callbacks),
     *   [
     *       Integer.BYTES - length of payload in bytes
     *       payload.length - serialized payload of callback button
     *       Integer.BYTES - length of text in bytes
     *       text.length - serialized text of callback button
     *   ]
     * ]
     * */
    public CallbackButtonsGroup(final long componentId, final List<List<CallbackButton>> group) {
        this.componentId = componentId;
        ArrayList<Byte> serialized = new ArrayList<>();
        serialized.addAll(asCollectionOfBytes(group.size()));
        for (List<CallbackButton> callbacks
                : group) {
            serialized.addAll(asCollectionOfBytes(callbacks.size()));
            for (CallbackButton button
                    : callbacks) {
                byte[] payloadBytes = button.getPayload().getBytes();
                serialized.addAll(asCollectionOfBytes(payloadBytes.length));
                serialized.addAll(asCollectionOfBytes(payloadBytes));
                byte[] textBytes = button.getText().getBytes();
                serialized.addAll(asCollectionOfBytes(textBytes.length));
                serialized.addAll(asCollectionOfBytes(textBytes));
            }
        }
        callbacks = ArrayUtils.toPrimitive(((Byte[]) serialized.toArray()));
    }

    public List<List<CallbackButton>> getCallbackButtons() {
        List<List<CallbackButton>> group = Collections.emptyList();
        ByteBuffer byteBuffer = ByteBuffer.wrap(callbacks);
        for (int listIdx = 0; listIdx < byteBuffer.getInt(); listIdx++) {
            List<CallbackButton> buttons = Collections.emptyList();
            for (int buttonIdx = 0; buttonIdx < byteBuffer.getInt(); buttonIdx++) {
                int payloadLength = byteBuffer.getInt();
                final String payload =
                        new String(getBytesFromByteBuffer(byteBuffer, byteBuffer.position(), payloadLength));
                int textLength = byteBuffer.getInt();
                final String text =
                        new String(getBytesFromByteBuffer(byteBuffer, byteBuffer.position(), textLength));
                buttons.add(new CallbackButton(payload, text, null));
            }
            group.add(buttons);
        }
        return group;
    }

    private static byte[] getBytesFromByteBuffer(final ByteBuffer byteBuffer, final int index, final int length) {
        byte[] dst = new byte[length];
        byteBuffer.get(dst, index, length);
        return dst;
    }

    private static Collection<Byte> asCollectionOfBytes(final Integer value) {
        return Arrays.asList(ArrayUtils.toObject(ByteBuffer.allocate(Integer.BYTES).putInt(value).array()));
    }

    private static Collection<Byte> asCollectionOfBytes(final byte[] bytes) {
        return Arrays.asList(ArrayUtils.toObject(bytes));
    }
}
