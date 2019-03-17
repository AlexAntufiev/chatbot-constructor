package chat.tamtam.bot.domain.response;

import java.util.Collections;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TamBotChannelsFailResponse extends FailResponse {
    private List<Long> channels = Collections.emptyList();

    public TamBotChannelsFailResponse(final String errorKey, final List<Long> payload) {
        super(errorKey);
        channels = payload;
    }
}
