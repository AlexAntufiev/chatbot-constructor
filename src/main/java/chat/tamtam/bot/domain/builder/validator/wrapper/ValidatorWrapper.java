package chat.tamtam.bot.domain.builder.validator.wrapper;

import chat.tamtam.bot.domain.builder.validator.Validator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class ValidatorWrapper {
    // CHECKSTYLE_OFF: ALMOST_ALL
    protected final Validator validator;
    // CHECKSTYLE_ON: ALMOST_ALL
}
