package chat.tamtam.bot.domain.response;

import lombok.Getter;

public class FailResponseWrapper<T> extends FailResponse {
    @Getter
    private T payload;

    public FailResponseWrapper(final String error, final T payload) {
        super(error);
        this.payload = payload;
    }
}
