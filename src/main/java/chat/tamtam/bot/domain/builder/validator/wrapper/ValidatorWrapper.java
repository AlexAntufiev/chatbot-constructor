package chat.tamtam.bot.domain.builder.validator.wrapper;

import chat.tamtam.bot.domain.builder.validator.ComponentValidator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class ValidatorWrapper {
    // CHECKSTYLE_OFF: ALMOST_ALL
    protected final ComponentValidator componentValidator;
    // CHECKSTYLE_ON: ALMOST_ALL
}
