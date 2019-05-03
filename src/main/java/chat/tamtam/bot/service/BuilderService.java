package chat.tamtam.bot.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;

import chat.tamtam.bot.domain.bot.BotSchemeEntity;
import chat.tamtam.bot.domain.builder.button.Button;
import chat.tamtam.bot.domain.builder.button.ButtonsGroup;
import chat.tamtam.bot.domain.builder.button.ButtonsGroupUpdate;
import chat.tamtam.bot.domain.builder.component.Component;
import chat.tamtam.bot.domain.builder.component.ComponentType;
import chat.tamtam.bot.domain.builder.component.ComponentUpdate;
import chat.tamtam.bot.domain.builder.validator.Validator;
import chat.tamtam.bot.domain.exception.ChatBotConstructorException;
import chat.tamtam.bot.domain.response.SuccessResponse;
import chat.tamtam.bot.domain.response.SuccessResponseWrapper;
import chat.tamtam.bot.repository.BotSchemeRepository;
import chat.tamtam.bot.repository.ButtonsGroupRepository;
import chat.tamtam.bot.repository.ComponentRepository;
import chat.tamtam.bot.repository.ComponentValidatorRepository;
import chat.tamtam.bot.utils.TransactionalUtils;
import chat.tamtam.botapi.model.Intent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BuilderService {
    private final ComponentRepository componentRepository;

    private final ComponentValidatorRepository validatorRepository;

    private final ButtonsGroupRepository buttonsGroupRepository;

    private final BotSchemeService botSchemeService;
    private final BotSchemeRepository botSchemaRepository;

    private final TransactionalUtils transactionalUtils;

    public SuccessResponse getNewComponentId(final String authToken, final int botSchemeId) {
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        Component newComponent = new Component();
        return new SuccessResponseWrapper<>(new Object() {
            @Getter
            private final Long componentId = componentRepository.save(newComponent).getId();
        });
    }

    public SuccessResponse getBotScheme(final String authToken, final int botSchemeId) {
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        List<ComponentUpdate> components = new ArrayList<>();
        componentRepository
                .findAllBySchemeId(botScheme.getId())
                .forEach(component -> {
                    ComponentUpdate update = new ComponentUpdate();
                    update.setComponent(component);

                    switch (ComponentType.getById(component.getType())) {
                        case INFO:
                            buttonsGroupRepository
                                    .findByComponentId(component.getId())
                                    .ifPresent(group -> update.setButtonsGroup(new ButtonsGroupUpdate(group)));
                            // @todo #CC-185 Fetch and add other attachments
                        case INPUT:
                            // @todo #CC-185 Fetch and add validators, actions etc.
                        default:
                            break;
                    }
                    components.add(update);
                });
        return new SuccessResponseWrapper<>(components);
    }

    public SuccessResponse saveBotScheme(
            final String authToken,
            final int botSchemeId,
            final List<ComponentUpdate> componentUpdates
    ) {
        // @todo #CC-163 Split logic by component type(e.g. if type is INPUT then ignore buttonsGroup)
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);

        Object updatedComponents =
                transactionalUtils.invokeCallable(() -> {
                    List<ComponentUpdate> updated = new ArrayList<>();
                    for (ComponentUpdate update
                            : componentUpdates) {
                        // @todo #CC-141 Enable reserved component check
                        /*if(!componentRepository
                                .existByIdAndSchemeId(
                                        update.getComponent().getId(),
                                        update.getComponent().getSchemeId()
                                )
                        ) {
                            throw new NotFoundEntityException(
                                    String.format(
                                            "Reserved component(id=%d, botSchemeId=%d) was not found",
                                            update.getComponent().getId(),
                                            update.getComponent().getSchemeId()
                                    ),
                                    Error.SERVICE_NO_ENTITY
                            );
                        }*/
                        // @todo #CC-141 Enable check for next component existence
                        /*if (update.getComponent().getNextState() != null) {
                            if (!componentRepository.existsByIdAndAndSchemeId(
                                    update.getComponent().getNextState(),
                                    update.getComponent().getSchemeId()
                            )) {
                                throw new NotFoundEntityException(
                                        String.format(
                                                "Next component(id=%d, botSchemeId=%d) " +
                                                        "for component(id=%d, botSchemeId=%d) was not found",
                                                update.getComponent().getNextState(),
                                                update.getComponent().getSchemeId(),
                                                update.getComponent().getId(),
                                                update.getComponent().getSchemeId()
                                                ),
                                        Error.SERVICE_NO_ENTITY
                                );
                            }
                        }*/

                        Component component = null;
                        ButtonsGroupUpdate buttonsGroupUpdate = null;
                        List<Validator> validators = null;

                        switch (ComponentType.getById(update.getComponent().getType())) {
                            case INFO:
                                buttonsGroupUpdate = updateButtonsGroup(update, botSchemeId);
                            case INPUT:
                                validators = updateValidators(update);
                            default:
                                update.getComponent().setSchemeId(botScheme.getId());
                                component = componentRepository.save(update.getComponent());
                        }

                        updated.add(new ComponentUpdate(component, validators, buttonsGroupUpdate));
                    }
                    botScheme.setSchema(componentUpdates.stream().findFirst().orElseThrow().getComponent().getId());
                    botSchemaRepository.save(botScheme);
                    return updated;
                });

        return new SuccessResponseWrapper<>(updatedComponents);
    }

    private List<Validator> updateValidators(final ComponentUpdate update) {
        /*
         * Check if all validators belong to this component(validator.getComponentId == component.getId)
         * */
        for (Validator validator
                : update.getValidators()) {
            if (!update.getComponent().getId().equals(validator.getComponentId())) {
                throw new ChatBotConstructorException(
                        String.format(
                                "Invalid validator componentId=%d(should be %d)",
                                validator.getComponentId(),
                                update.getComponent().getId()
                        ),
                        Error.SCHEME_BUILDER_INVALID_VALIDATOR
                );
            }
        }

        return Lists.newArrayList(validatorRepository.saveAll(update.getValidators()));
    }

    private ButtonsGroupUpdate updateButtonsGroup(
            final ComponentUpdate update,
            final int botSchemeId
    ) throws IOException {
        if (update.getButtonsGroup() == null) {
            buttonsGroupRepository
                    .findByComponentId(update.getComponent().getId())
                    .ifPresent(group -> {
                        group.setComponentId(null);
                        buttonsGroupRepository.save(group);
                    });
            return null;
        }

        /*
         * Check if buttons group will update group that belongs to this component
         * */
        if (update.getButtonsGroup().getId() != null) {
            if (!buttonsGroupRepository.existsByIdAndAndComponentId(
                    update.getButtonsGroup().getId(),
                    update.getComponent().getId()
            )) {
                throw new ChatBotConstructorException(
                        String.format(
                                "Buttons group id(%d) "
                                        + "does not belong to this component(id=%d, botSchemeId=%d)",
                                update.getButtonsGroup().getId(),
                                update.getComponent().getId(),
                                botSchemeId
                        ),
                        Error.SCHEME_BUILDER_BUTTONS_UPDATE_BY_ID
                );
            }
        } else {
            /*
             * Check if this component has no buttonsGroup yet,
             * else new group should inherit id of previous(to rewrite it)
             * */
            buttonsGroupRepository
                    .findByComponentId(update.getComponent().getId())
                    .ifPresent(g -> update.getButtonsGroup().setId(g.getId()));
        }

        /*
         * Check if buttons group is not empty
         * */
        if (update.getButtonsGroup().getButtons().isEmpty()) {
            throw new ChatBotConstructorException(
                    String.format(
                            "Buttons group(id=%d, componentId=%d, botSchemeId=%d) is empty",
                            update.getButtonsGroup().getId(),
                            update.getComponent().getId(),
                            botSchemeId
                    ),
                    Error.SCHEME_BUILDER_BUTTONS_GROUP_IS_EMPTY
            );
        }

        /*
         * Check if all buttons in group are not malformed
         * (e.g. text is not empty, payload is not empty, intent is not malformed)
         * */
        // @todo #CC-163 Add check for intent value
        for (List<Button> buttons
                : update.getButtonsGroup().getButtons()) {
            if (buttons.isEmpty()) {
                throw new ChatBotConstructorException(
                        String.format(
                                "Buttons group(id=%d, componentId=%d, botSchemeId=%d) has empty list",
                                update.getButtonsGroup().getId(),
                                update.getComponent().getId(),
                                botSchemeId
                        ),
                        Error.SCHEME_BUILDER_BUTTONS_GROUP_IS_EMPTY
                );
            }
            for (Button button
                    : buttons) {
                if (StringUtils.isEmpty(button.getValue()) || StringUtils.isEmpty(button.getText())) {
                    throw new ChatBotConstructorException(
                            String.format(
                                    "Button(%s) has empty text or payload"
                                            + "(botSchemeId=%d, componentId=%d)",
                                    button, botSchemeId, update.getComponent().getId()
                            ),
                            Error.SCHEME_BUILDER_BUTTONS_EMPTY_FIELDS
                    );
                }

                try {
                    Optional
                            .ofNullable(Intent.create(button.getIntent()))
                            .ifPresentOrElse(
                                    intent -> button.setIntent(intent.getValue()),
                                    () -> button.setIntent(Intent.DEFAULT.getValue())
                            );
                } catch (IllegalArgumentException e) {
                    throw new ChatBotConstructorException(
                            String.format(
                                    "Malformed intent(%s, botSchemeId=%d, componentId=%d)",
                                    update, botSchemeId, update.getComponent().getId()
                            ),
                            Error.SCHEME_BUILDER_BUTTONS_GROUP_INTENT_MALFORMED,
                            e
                    );
                }
            }
        }

        ButtonsGroup group = buttonsGroupRepository.save(
                new ButtonsGroup(
                        update.getComponent().getId(),
                        update.getButtonsGroup()
                )
        );

        update.getComponent().setHasCallbacks(true);
        return new ButtonsGroupUpdate(group);
    }
}
