package chat.tamtam.bot.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;

import chat.tamtam.bot.domain.bot.BotScheme;
import chat.tamtam.bot.domain.builder.button.Button;
import chat.tamtam.bot.domain.builder.button.ButtonsGroup;
import chat.tamtam.bot.domain.builder.button.ButtonsGroupUpdate;
import chat.tamtam.bot.domain.builder.component.ComponentType;
import chat.tamtam.bot.domain.builder.component.ComponentUpdate;
import chat.tamtam.bot.domain.builder.component.SchemeComponent;
import chat.tamtam.bot.domain.builder.validator.ComponentValidator;
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
    private final BotSchemeRepository botSchemeRepository;

    private final TransactionalUtils transactionalUtils;

    public SuccessResponse getNewComponentId(final String authToken, final int botSchemeId) {
        BotScheme botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        SchemeComponent newSchemeComponent = new SchemeComponent();
        return new SuccessResponseWrapper<>(new Object() {
            @Getter
            private final Long componentId = componentRepository.save(newSchemeComponent).getId();
        });
    }

    public SuccessResponse getBotScheme(final String authToken, final int botSchemeId) {
        BotScheme botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        List<ComponentUpdate> components = new ArrayList<>();
        componentRepository
                .findAllBySchemeId(botScheme.getId())
                .forEach(component -> {
                    ComponentUpdate update = new ComponentUpdate();
                    update.setSchemeComponent(component);

                    switch (ComponentType.getById(component.getType())) {
                        case INFO:
                            buttonsGroupRepository
                                    .findByComponentId(component.getId())
                                    .ifPresent(group -> update.setButtonsGroup(new ButtonsGroupUpdate(group)));
                            // @todo #CC-185 Fetch and add other attachments
                        case INPUT:
                            // @todo #CC-185 Fetch and add componentValidators, actions etc.
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
            final List<ComponentUpdate> components
    ) {
        // @todo #CC-163 Split logic by builderComponent type(e.g. if type is INPUT then ignore buttonsGroup)
        BotScheme botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);

        Object updatedComponents =
                transactionalUtils.invokeCallable(() -> {
                    List<ComponentUpdate> updated = new ArrayList<>();
                    for (ComponentUpdate update
                            : components) {
                        // @todo #CC-141 Enable reserved builderComponent check
                        /*if(!componentRepository
                                .existByIdAndSchemeId(
                                        update.getBuilderComponent().getId(),
                                        update.getBuilderComponent().getSchemeId()
                                )
                        ) {
                            throw new NotFoundEntityException(
                                    String.format(
                                            "Reserved builderComponent(id=%d, botSchemeId=%d) was not found",
                                            update.getBuilderComponent().getId(),
                                            update.getBuilderComponent().getSchemeId()
                                    ),
                                    Error.SERVICE_NO_ENTITY
                            );
                        }*/
                        // @todo #CC-141 Enable check for next builderComponent existence
                        /*if (update.getBuilderComponent().getNextState() != null) {
                            if (!componentRepository.existsByIdAndAndSchemeId(
                                    update.getBuilderComponent().getNextState(),
                                    update.getBuilderComponent().getSchemeId()
                            )) {
                                throw new NotFoundEntityException(
                                        String.format(
                                                "Next builderComponent(id=%d, botSchemeId=%d) " +
                                                        "for builderComponent(id=%d, botSchemeId=%d) was not found",
                                                update.getBuilderComponent().getNextState(),
                                                update.getBuilderComponent().getSchemeId(),
                                                update.getBuilderComponent().getId(),
                                                update.getBuilderComponent().getSchemeId()
                                                ),
                                        Error.SERVICE_NO_ENTITY
                                );
                            }
                        }*/

                        SchemeComponent schemeComponent = null;
                        ButtonsGroupUpdate buttonsGroupUpdate = null;
                        List<ComponentValidator> componentValidators = null;

                        switch (ComponentType.getById(update.getSchemeComponent().getType())) {
                            case INFO:
                                buttonsGroupUpdate = updateButtonsGroup(update, botSchemeId);
                            case INPUT:
                                componentValidators = updateValidators(update);
                            default:
                                update.getSchemeComponent().setSchemeId(botScheme.getId());
                                schemeComponent = componentRepository.save(update.getSchemeComponent());
                        }

                        updated.add(new ComponentUpdate(schemeComponent, componentValidators, buttonsGroupUpdate));
                    }
                    botScheme.setSchemeEnterState(
                            components
                                    .stream()
                                    .findFirst()
                                    .orElseThrow()
                                    .getSchemeComponent()
                                    .getId()
                    );
                    botSchemeRepository.save(botScheme);
                    return updated;
                });

        return new SuccessResponseWrapper<>(updatedComponents);
    }

    private List<ComponentValidator> updateValidators(final ComponentUpdate update) {
        /*
         * Check if all componentValidators belong to this
         * builderComponent(componentValidator.getComponentId == builderComponent.getId)
         * */
        for (ComponentValidator componentValidator
                : update.getComponentValidators()) {
            if (!update.getSchemeComponent().getId().equals(componentValidator.getComponentId())) {
                throw new ChatBotConstructorException(
                        String.format(
                                "Invalid componentValidator componentId=%d(should be %d)",
                                componentValidator.getComponentId(),
                                update.getSchemeComponent().getId()
                        ),
                        Error.SCHEME_BUILDER_INVALID_VALIDATOR
                );
            }
        }

        return Lists.newArrayList(validatorRepository.saveAll(update.getComponentValidators()));
    }

    private ButtonsGroupUpdate updateButtonsGroup(
            final ComponentUpdate update,
            final int botSchemeId
    ) throws IOException {
        if (update.getButtonsGroup() == null) {
            buttonsGroupRepository
                    .findByComponentId(update.getSchemeComponent().getId())
                    .ifPresent(group -> {
                        group.setComponentId(null);
                        buttonsGroupRepository.save(group);
                    });
            return null;
        }

        /*
         * Check if buttons group will update group that belongs to this builderComponent
         * */
        if (update.getButtonsGroup().getId() != null) {
            if (!buttonsGroupRepository.existsByIdAndAndComponentId(
                    update.getButtonsGroup().getId(),
                    update.getSchemeComponent().getId()
            )) {
                throw new ChatBotConstructorException(
                        String.format(
                                "Buttons group id(%d) "
                                        + "does not belong to this builderComponent(id=%d, botSchemeId=%d)",
                                update.getButtonsGroup().getId(),
                                update.getSchemeComponent().getId(),
                                botSchemeId
                        ),
                        Error.SCHEME_BUILDER_BUTTONS_UPDATE_BY_ID
                );
            }
        } else {
            /*
             * Check if this builderComponent has no buttonsGroup yet,
             * else new group should inherit id of previous(to rewrite it)
             * */
            buttonsGroupRepository
                    .findByComponentId(update.getSchemeComponent().getId())
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
                            update.getSchemeComponent().getId(),
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
                                update.getSchemeComponent().getId(),
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
                                    button, botSchemeId, update.getSchemeComponent().getId()
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
                                    update, botSchemeId, update.getSchemeComponent().getId()
                            ),
                            Error.SCHEME_BUILDER_BUTTONS_GROUP_INTENT_MALFORMED,
                            e
                    );
                }
            }
        }

        ButtonsGroup group = buttonsGroupRepository.save(
                new ButtonsGroup(
                        update.getSchemeComponent().getId(),
                        update.getButtonsGroup()
                )
        );

        update.getSchemeComponent().setHasCallbacks(true);
        return new ButtonsGroupUpdate(group);
    }
}
