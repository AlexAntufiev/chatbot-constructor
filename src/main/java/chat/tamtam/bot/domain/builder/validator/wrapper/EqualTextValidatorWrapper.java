package chat.tamtam.bot.domain.builder.validator.wrapper;

import chat.tamtam.bot.domain.builder.validator.ComponentValidator;
import chat.tamtam.bot.domain.webhook.BotContext;
import chat.tamtam.botapi.model.MessageCreatedUpdate;

public class EqualTextValidatorWrapper extends ValidatorWrapper {
    private final String textToCompare;

    public EqualTextValidatorWrapper(final ComponentValidator componentValidator) {
        super(componentValidator);
        textToCompare = new String(componentValidator.getBytes());
    }

    public boolean validate(final MessageCreatedUpdate update, final BotContext context) {
        String text = update.getMessage().getBody().getText();
        if (text == null) {
            context.setState(componentValidator.getFailState());
            return false;
        }
        return text.equals(textToCompare);
    }
}
