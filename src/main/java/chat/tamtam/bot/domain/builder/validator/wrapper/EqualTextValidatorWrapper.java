package chat.tamtam.bot.domain.builder.validator.wrapper;

import chat.tamtam.bot.domain.builder.validator.Validator;
import chat.tamtam.botapi.model.MessageCreatedUpdate;

public class EqualTextValidatorWrapper extends ValidatorWrapper {
    private final String textToCompare;

    public EqualTextValidatorWrapper(final Validator validator) {
        super(validator);
        textToCompare = new String(validator.getBytes());
    }

    public boolean validate(MessageCreatedUpdate update) {
        String text = update.getMessage().getBody().getText();
        if (text == null) {
            return false;
        }
        return text.equals(textToCompare);
    }
}
