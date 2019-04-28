package chat.tamtam.bot.domain.builder.component;

import java.util.Collections;
import java.util.List;

import chat.tamtam.bot.domain.builder.callback.CallbackButton;
import chat.tamtam.bot.domain.builder.validator.Validator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComponentUpdate {
    private Component component;
    private List<Validator> validators = Collections.emptyList();
    private List<List<CallbackButton>> callbacks = Collections.emptyList();
}
