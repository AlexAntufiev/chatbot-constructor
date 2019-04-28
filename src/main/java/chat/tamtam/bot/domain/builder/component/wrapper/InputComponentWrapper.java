package chat.tamtam.bot.domain.builder.component.wrapper;

import chat.tamtam.bot.domain.builder.component.Component;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InputComponentWrapper {
    private final Component component;
    // @todo #CC-141 Implement validators and predicates invocation
}
