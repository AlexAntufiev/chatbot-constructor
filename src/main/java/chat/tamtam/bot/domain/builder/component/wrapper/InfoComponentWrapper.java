package chat.tamtam.bot.domain.builder.component.wrapper;

import java.util.Collections;

import chat.tamtam.bot.domain.builder.component.SchemeComponent;
import chat.tamtam.botapi.model.NewMessageBody;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InfoComponentWrapper {
    private final SchemeComponent schemeComponent;

    public NewMessageBody getMessageBody() {
        return new NewMessageBody(schemeComponent.getText(), Collections.emptyList());
    }
}
