package chat.tamtam.bot.domain.builder.component.wrapper;

import java.util.Collections;

import chat.tamtam.bot.domain.builder.component.BuilderComponent;
import chat.tamtam.botapi.model.NewMessageBody;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InfoComponentWrapper {
    private final BuilderComponent builderComponent;

    public NewMessageBody getMessageBody() {
        return new NewMessageBody(builderComponent.getText(), Collections.emptyList());
    }
}
