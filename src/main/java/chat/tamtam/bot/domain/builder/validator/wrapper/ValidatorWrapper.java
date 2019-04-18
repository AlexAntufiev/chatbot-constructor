package chat.tamtam.bot.domain.builder.validator.wrapper;

import chat.tamtam.bot.domain.builder.validator.Validator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class ValidatorWrapper {
    private final Validator validator;
}
