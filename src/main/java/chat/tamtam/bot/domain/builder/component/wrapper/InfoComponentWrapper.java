package chat.tamtam.bot.domain.builder.component.wrapper;

import java.util.Collections;

import chat.tamtam.bot.domain.builder.component.Component;
import chat.tamtam.botapi.model.NewMessageBody;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InfoComponentWrapper {
    private final Component component;

    public NewMessageBody getMessageBody() {
        return new NewMessageBody(component.getText(), Collections.emptyList());
    }
}
