package chat.tamtam.bot.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class SuccessResponseWrapper<T> extends SuccessResponse {
    @Getter
    private T payload;
}
