package chat.tamtam.bot.domain.builder.button;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ButtonPayload {
    private Long nextState;
    private String value;

    public ButtonPayload(final String payload) {
        try {
            ButtonPayload buttonPayload = new ObjectMapper().readValue(payload, ButtonPayload.class);
            nextState = buttonPayload.nextState;
            value = buttonPayload.value;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
