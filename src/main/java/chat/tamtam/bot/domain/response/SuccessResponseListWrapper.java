package chat.tamtam.bot.domain.response;

import lombok.Getter;

public class SuccessResponseListWrapper<T> extends SuccessResponseWrapper<T> {
    @Getter
    private Long marker;

    public SuccessResponseListWrapper(T payload, final Long marker) {
        super(payload);
        this.marker = marker;
    }
}
