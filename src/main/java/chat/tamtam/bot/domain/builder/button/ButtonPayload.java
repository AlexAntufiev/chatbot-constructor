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

    public static ButtonPayload parseButtonPayload(final String payload) throws IOException {
        return new ObjectMapper().readValue(payload, ButtonPayload.class);
    }
}
