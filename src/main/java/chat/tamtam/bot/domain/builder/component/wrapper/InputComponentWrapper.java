package chat.tamtam.bot.domain.builder.component.wrapper;

import chat.tamtam.bot.domain.builder.component.BuilderComponent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InputComponentWrapper {
    private final BuilderComponent builderComponent;
    // @todo #CC-141 Implement componentValidators and predicates invocation
}
