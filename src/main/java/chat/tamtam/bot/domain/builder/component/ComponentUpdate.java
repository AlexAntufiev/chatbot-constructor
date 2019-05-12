package chat.tamtam.bot.domain.builder.component;

import java.util.Collections;
import java.util.List;

import chat.tamtam.bot.domain.builder.button.ButtonsGroupUpdate;
import chat.tamtam.bot.domain.builder.validator.ComponentValidator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComponentUpdate {
    private SchemeComponent component;
    private List<ComponentValidator> validators = Collections.emptyList();
    private ButtonsGroupUpdate buttonsGroup;
}
