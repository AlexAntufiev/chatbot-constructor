package chat.tamtam.bot.domain.builder.button;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.ArrayUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import chat.tamtam.botapi.model.CallbackButton;
import chat.tamtam.botapi.model.Intent;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table
public class ButtonsGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false)
    private Byte[] buttons;
    private Long componentId;

    /*
     * Integer.BYTES - amount of Lists,
     * [
     *   Integer.BYTES - List size(amount of callbacks),
     *   [
     *       Button
     *   ]
     * ]
     * */
    public ButtonsGroup(
            final long componentId,
            final ButtonsGroupUpdate update
    ) throws JsonProcessingException {
        id = update.getId();
        this.componentId = componentId;
        ArrayList<Byte> serialized = new ArrayList<>();
        serialized.addAll(asCollectionOfBytes(update.getButtons().size()));
        for (List<Button> list
                : update.getButtons()) {
            serialized.addAll(asCollectionOfBytes(list.size()));
            for (Button button
                    : list) {
                serialized.addAll(asCollectionOfBytes(button.getBytes()));
            }
        }
        buttons = serialized.toArray(new Byte[serialized.size()]);
    }

    public List<List<chat.tamtam.botapi.model.Button>> getTamButtons() {
        ByteBuffer byteBuffer = ByteBuffer.wrap(ArrayUtils.toPrimitive(buttons));
        int groupSize = byteBuffer.getInt();
        List<List<chat.tamtam.botapi.model.Button>> group = new ArrayList<>(groupSize);
        for (int listIdx = 0; listIdx < groupSize; listIdx++) {
            int buttonsSize = byteBuffer.getInt();
            List<chat.tamtam.botapi.model.Button> buttons = new ArrayList<>(buttonsSize);
            for (int buttonIdx = 0; buttonIdx < buttonsSize; buttonIdx++) {
                int payloadLength = byteBuffer.getInt();
                final String payload =
                        new String(getBytesFromByteBuffer(byteBuffer, payloadLength));
                int textLength = byteBuffer.getInt();
                final String text =
                        new String(getBytesFromByteBuffer(byteBuffer, textLength));
                int intentLength = byteBuffer.getInt();
                final String intent = new String(getBytesFromByteBuffer(byteBuffer, intentLength));

                buttons.add(new CallbackButton(payload, text, Intent.create(intent)));
            }
            group.add(buttons);
        }
        return group;
    }

    private static byte[] getBytesFromByteBuffer(final ByteBuffer byteBuffer, final int length) {
        byte[] dst = new byte[length];
        byteBuffer.get(dst, 0, length);
        return dst;
    }

    private static Collection<Byte> asCollectionOfBytes(final Integer value) {
        return Arrays.asList(
                ArrayUtils.toObject(
                        ByteBuffer
                                .allocate(Integer.BYTES)
                                .putInt(value)
                                .array()
                )
        );
    }

    private static Collection<Byte> asCollectionOfBytes(final byte[] bytes) {
        return Arrays.asList(
                ArrayUtils.toObject(bytes)
        );
    }
}
