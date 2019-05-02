package chat.tamtam.bot.domain.builder.button;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import chat.tamtam.botapi.model.CallbackButton;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ButtonsGroupUpdate {
    private Long id;
    private List<List<Button>> buttons;

    public ButtonsGroupUpdate(final ButtonsGroup group) throws IOException {
        id = group.getId();
        buttons = new ArrayList<>();

        for (List<chat.tamtam.botapi.model.Button> list
                : group.getTamButtons()) {
            List<Button> buttonList = new ArrayList<>();
            for (chat.tamtam.botapi.model.Button callbackButton
                    : list) {
                ButtonPayload payload
                        = ButtonPayload.parseButtonPayload(((CallbackButton) callbackButton).getPayload());
                Button button = new Button(
                        payload.getValue(),
                        callbackButton.getText(),
                        null,
                        payload.getNextState()
                );
                if (callbackButton.getIntent() != null) {
                    button.setIntent(callbackButton.getIntent().getValue());
                }
                buttonList.add(button);
            }
            buttons.add(buttonList);
        }
    }
}
