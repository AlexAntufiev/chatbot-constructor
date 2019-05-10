package chat.tamtam.bot.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import chat.tamtam.bot.domain.builder.component.ComponentType;
import chat.tamtam.bot.domain.builder.component.ComponentUpdate;
import chat.tamtam.bot.domain.builder.component.SchemeComponent;
import chat.tamtam.bot.domain.exception.ChatBotConstructorException;
import chat.tamtam.bot.service.Error;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@Log4j2
@UtilityClass
public class SchemeComponentUtils {
    public boolean isGraphIsNonCyclic(final List<ComponentUpdate> updates) {
        System.out.println("TEST");
        System.out.println(updates);
        Set<Long> metComponents = new HashSet<>();
        Map<Long, SchemeComponent> components = new HashMap<>();
        if (updates.isEmpty()) {
            return true;
        }
        for (ComponentUpdate update
                : updates) {
            SchemeComponent component = update.getComponent();

            if (component.getId() == null) {
                throw new ChatBotConstructorException(
                        String.format(
                                "Can't check component graph(%s) for cyclicality, because component(%s) id is null",
                                update, component
                        ),
                        Error.SCHEME_BUILDER_COMPONENT_ID_IS_NULL
                );
            }

            if (components.containsKey(component.getId())) {
                throw new ChatBotConstructorException(
                        String.format(
                                "Can't check component graph(%s) for cyclicality, "
                                        + "because component(%s) with id=%d already exist",
                                updates, component, component.getId()
                        ),
                        Error.SCHEME_BUILDER_COMPONENT_DUPLICATION
                );
            }

            components.put(component.getId(), component);
        }

        while (true) {
            Optional<Long> optionalId =
                    components
                            .keySet()
                            .stream()
                            .filter(key -> !metComponents.contains(key))
                            .findFirst();
            if (optionalId.isEmpty()) {
                break;
            }
            Long id = optionalId.get();
            try {
                metComponents.addAll(checkGraph(id, components));
            } catch (IllegalStateException e) {
                log.error(e);
                return false;
            }
        }

        return true;
    }

    private Set<Long> checkGraph(final long id, final Map<Long, SchemeComponent> components) {
        Set<Long> metComponents = new HashSet<>();
        long currentId = id;
        while (true) {
            SchemeComponent component = components.get(currentId);
            if (component == null) {
                throw new ChatBotConstructorException(
                        String.format(
                                "Component by id=%d does not exist",
                                currentId
                        ),
                        Error.SCHEME_BUILDER_COMPONENT_DOES_NOT_EXIST
                );
            }
            switch (ComponentType.getById(component.getType())) {
                case INFO:
                    if (metComponents.contains(component.getId())) {
                        throw new IllegalStateException(
                                String.format("Graph has cycle at component %s", component)
                        );
                    }
                    metComponents.add(component.getId());
                    break;

                default:
                    metComponents.add(component.getId());
                    return metComponents;
            }
            if (component.getNextState() == null) {
                return metComponents;
            }
            currentId = component.getNextState();
        }
    }
}
