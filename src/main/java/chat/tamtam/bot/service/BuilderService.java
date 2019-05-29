package chat.tamtam.bot.service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;

import chat.tamtam.bot.domain.bot.BotScheme;
import chat.tamtam.bot.domain.builder.action.SchemeAction;
import chat.tamtam.bot.domain.builder.action.SchemeActionType;
import chat.tamtam.bot.domain.builder.button.Button;
import chat.tamtam.bot.domain.builder.button.ButtonsGroup;
import chat.tamtam.bot.domain.builder.button.ButtonsGroupUpdate;
import chat.tamtam.bot.domain.builder.component.ComponentType;
import chat.tamtam.bot.domain.builder.component.ComponentUpdate;
import chat.tamtam.bot.domain.builder.component.SchemeComponent;
import chat.tamtam.bot.domain.builder.component.group.GroupType;
import chat.tamtam.bot.domain.builder.component.group.SchemeComponentGroup;
import chat.tamtam.bot.domain.builder.validator.ComponentValidator;
import chat.tamtam.bot.domain.exception.ChatBotConstructorException;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.domain.response.SuccessResponse;
import chat.tamtam.bot.domain.response.SuccessResponseWrapper;
import chat.tamtam.bot.repository.BotSchemeRepository;
import chat.tamtam.bot.repository.ButtonsGroupRepository;
import chat.tamtam.bot.repository.ComponentGroupRepository;
import chat.tamtam.bot.repository.ComponentRepository;
import chat.tamtam.bot.repository.ComponentValidatorRepository;
import chat.tamtam.bot.repository.SchemeActionRepository;
import chat.tamtam.bot.utils.SchemeComponentUtils;
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

    private final ComponentGroupRepository componentGroupRepository;

    private final TransactionalUtils transactionalUtils;
    private final SchemeActionRepository actionRepository;

    public SuccessResponse getNewComponentId(final String authToken, final int botSchemeId) {
        BotScheme botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        SchemeComponent newSchemeComponent = new SchemeComponent();
        newSchemeComponent.setSchemeId(botSchemeId);
        return new SuccessResponseWrapper<>(new Object() {
            @Getter
            private final Long componentId = componentRepository.save(newSchemeComponent).getId();
        });
    }

    public SuccessResponse getBotScheme(final String authToken, final int botSchemeId) {
        BotScheme botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);

        List<ComponentUpdate> updates = new ArrayList<>();

        componentRepository
                .findAllBySchemeIdAndSavedOrderBySequence(botScheme.getId(), true)
                .forEach(component -> {
                    ComponentUpdate update = new ComponentUpdate();
                    update.setComponent(component);

                    if (component.getType() == null) {
                        updates.add(update);
                        return;
                    }

                    switch (ComponentType.getById(component.getType())) {
                        case INFO:
                            buttonsGroupRepository
                                    .findByComponentId(component.getId())
                                    .ifPresent(group -> update.setButtonsGroup(new ButtonsGroupUpdate(group)));
                            // @todo #CC-185 Fetch and add other attachments
                            break;

                        case INPUT:
                            // @todo #CC-185 Fetch and add validators, actions etc.
                            break;

                        default:
                            throw new ChatBotConstructorException(
                                    String.format("Component(%s) has illegal type", update.getComponent()),
                                    Error.SCHEME_BUILDER_COMPONENT_TYPE_IS_ILLEGAL
                            );
                    }

                    List<SchemeAction> actions =
                            Lists.newArrayList(
                                    actionRepository.findAllByComponentIdOrderBySequence(component.getId())
                            );
                    update.setActions(actions);

                    updates.add(update);
                });

        return new SuccessResponseWrapper<>(updates);
    }

    public SuccessResponse saveBotScheme(
            final String authToken,
            final int botSchemeId,
            final List<ComponentUpdate> components
    ) {
        BotScheme botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        // Removes entry point of scheme in case of update is empty
        // aka disable further graph execution until next update
        if (components.isEmpty()) {
            botScheme.setSchemeEnterState(null);
            botScheme.setUpdate(Instant.now());
            botSchemeRepository.save(botScheme);
            return new SuccessResponseWrapper<>(Collections.emptyList());
        }

        /*
         * Check that graph is non-cyclicality.
         * In case if false -> throws exception.
         * */
        if (!SchemeComponentUtils.isGraphIsNonCyclic(components)) {
            throw new ChatBotConstructorException(
                    String.format("Components graph(%s) is cyclic(botScheme=%s)", components, botScheme),
                    Error.SCHEME_BUILDER_COMPONENT_GRAPH_IS_CYCLIC
            );
        }

        AtomicInteger sequence = new AtomicInteger(Integer.MIN_VALUE);
        final int sequenceDelta = 1;

        Set<Long> ids = new HashSet<>();

        Object updatedComponents =
                transactionalUtils.invokeCallable(() -> {
                    List<ComponentUpdate> updated = new ArrayList<>();
                    for (ComponentUpdate update
                            : components) {
                        // Checks that component was reserved
                        if (!componentRepository
                                .existsByIdAndSchemeId(
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
                        }
                        // Checks that next component was reserved too
                        if (update.getComponent().getNextState() != null) {
                            if (!componentRepository.existsByIdAndSchemeId(
                                    update.getComponent().getNextState(),
                                    update.getComponent().getSchemeId()
                            )) {
                                throw new NotFoundEntityException(
                                        String.format(
                                                "Next component(id=%d, botSchemeId=%d) "
                                                        + "for builderComponent(id=%d, botSchemeId=%d) was not found",
                                                update.getComponent().getNextState(),
                                                update.getComponent().getSchemeId(),
                                                update.getComponent().getId(),
                                                update.getComponent().getSchemeId()
                                                ),
                                        Error.SERVICE_NO_ENTITY
                                );
                            }
                        }

                        //Check if specified group belongs to this bot scheme
                        if (update.getComponent().getGroupId() != null) {
                            getGroup(botScheme, update.getComponent().getGroupId());
                        }

                        SchemeComponent schemeComponent = null;
                        ButtonsGroupUpdate buttonsGroupUpdate = null;
                        List<ComponentValidator> componentValidators = null;
                        List<SchemeAction> actions = null;

                        //Check that component has type
                        if (update.getComponent().getType() == null) {
                            throw new ChatBotConstructorException(
                                    String.format("Component(%s) has null in type", update.getComponent()),
                                    Error.SCHEME_BUILDER_COMPONENT_TYPE_IS_NULL
                            );
                        }

                        switch (ComponentType.getById(update.getComponent().getType())) {
                            case INFO:
                                String text = update.getComponent().getText();
                                if (StringUtils.isEmpty(update.getComponent().getText())
                                        || update.getComponent().getText().trim().isEmpty()) {
                                    throw new ChatBotConstructorException(
                                            String.format(
                                                    "Update(%s) has empty text(botSchemeId=%d)",
                                                    update.getComponent(), botSchemeId
                                            ),
                                            Error.SCHEME_BUILDER_COMPONENT_TEXT_IS_EMPTY
                                    );
                                }
                                buttonsGroupUpdate = updateButtonsGroup(update, botSchemeId);
                                break;

                            case INPUT:
                                componentValidators = updateValidators(update);
                                break;

                            default:
                                throw new ChatBotConstructorException(
                                        String.format("Component(%s) has illegal type", update.getComponent()),
                                        Error.SCHEME_BUILDER_COMPONENT_TYPE_IS_ILLEGAL
                                );
                        }

                        actions = updateActions(update, botScheme.getId());

                        update.getComponent().setSchemeId(botScheme.getId());
                        update.getComponent().setSequence(sequence.addAndGet(sequenceDelta));
                        update.getComponent().setSaved(true);
                        schemeComponent = componentRepository.save(update.getComponent());
                        ids.add(update.getComponent().getId());

                        updated.add(new ComponentUpdate(
                                schemeComponent,
                                componentValidators,
                                actions,
                                buttonsGroupUpdate
                        ));
                    }

                    botScheme.setSchemeEnterState(
                            components
                                    .stream()
                                    .findFirst()
                                    .orElseThrow()
                                    .getComponent()
                                    .getId()
                    );
                    removeComponentsExcept(ids, botScheme.getId());
                    botScheme.setUpdate(Instant.now());
                    botSchemeRepository.save(botScheme);
                    return updated;
                });
        return new SuccessResponseWrapper<>(updatedComponents);
    }

    private void removeComponentsExcept(final Set<Long> ids, final int schemeId) {
        List<SchemeComponent> list =
                StreamSupport.stream(componentRepository.findAllBySchemeId(schemeId).spliterator(), false)
                        .filter(component -> !ids.contains(component.getId()))
                        .peek(component -> {
                            component.setSchemeId(null);
                            component.setSaved(false);
                        })
                        .collect(Collectors.toList());
        componentRepository.saveAll(list);
    }

    private List<ComponentValidator> updateValidators(final ComponentUpdate update) {
        /*
         * Check if all validators belong to this
         * builderComponent(componentValidator.getComponentId == builderComponent.getId)
         * */
        for (ComponentValidator componentValidator
                : update.getValidators()) {
            if (!update.getComponent().getId().equals(componentValidator.getComponentId())) {
                throw new ChatBotConstructorException(
                        String.format(
                                "Invalid componentValidator componentId=%d(should be %d)",
                                componentValidator.getComponentId(),
                                update.getComponent().getId()
                        ),
                        Error.SCHEME_BUILDER_INVALID_VALIDATOR
                );
            }
        }

        return Lists.newArrayList(validatorRepository.saveAll(update.getValidators()));
    }

    private List<SchemeAction> updateActions(
            final ComponentUpdate update,
            final int botSchemeId
    ) {
        // Removes all actions in case of empty update
        if (update.getActions() == null || update.getActions().isEmpty()) {
            List<SchemeAction> actions =
                    StreamSupport
                            .stream(
                                    actionRepository
                                            .findAllByComponentId(update.getComponent().getId())
                                            .spliterator(),
                                    false
                            )
                            .peek(action -> action.setComponentId(null))
                            .collect(Collectors.toList());
            actionRepository.saveAll(actions);
            return null;
        }

        SchemeComponent component = update.getComponent();

        AtomicInteger sequence = new AtomicInteger(Integer.MIN_VALUE);
        final int sequenceDelta = 1;

        Set<Long> ids = new HashSet<>();

        // Prepare actions to store
        update.getActions().forEach(action -> {

            if (action.getComponentId() != null && !component.getId().equals(action.getComponentId())) {
                throw new ChatBotConstructorException(
                        String.format(
                                "Action(%s) does not belong to component(%s)",
                                action, component
                        ),
                        Error.SCHEME_BUILDER_COMPONENT_ACTION_DOES_NOT_BELONG_TO_COMPONENT
                );
            }

            if (action.getId() != null) {
                actionRepository
                        .findByIdAndComponentId(action.getId(), action.getComponentId())
                        .orElseThrow(
                                () -> new ChatBotConstructorException(
                                        String.format(
                                                "Cant't find action(%s) belongs to component(%s)",
                                                action, component
                                        ),
                                        Error.SCHEME_BUILDER_COMPONENT_ACTION_IS_NOT_FOUND
                                )
                        );
            }

            action.setComponentId(component.getId());

            SchemeActionType type = SchemeActionType.getById(action.getType());

            if (type == null) {
                throw new ChatBotConstructorException(
                        String.format(
                                "Action(%s) belongs to component(%s) has illegal type",
                                action, component
                        ),
                        Error.SCHEME_BUILDER_COMPONENT_ACTION_HAS_ILLEGAL_TYPE
                );
            }

            action.setSequence(sequence.addAndGet(sequenceDelta));
        });

        List<SchemeAction> actions = Lists.newArrayList(actionRepository.saveAll(update.getActions()));

        actions.forEach(action -> ids.add(action.getId()));

        removeActionsExcept(ids, update.getComponent().getId());

        return actions;
    }

    private void removeActionsExcept(final Set<Long> actions, final long componentId) {
        Iterable<SchemeAction> allActions = actionRepository.findAllByComponentId(componentId);
        allActions.forEach(action -> {
            if (!actions.contains(action.getId())) {
                action.setComponentId(null);
            }
        });
        actionRepository.saveAll(allActions);
    }

    private ButtonsGroupUpdate updateButtonsGroup(
            final ComponentUpdate update,
            final int botSchemeId
    ) throws IOException {
        /*
         * Check if buttons are presented in update
         * */
        if (update.getButtonsGroup() == null
                || update.getButtonsGroup().getButtons() == null
                || update.getButtonsGroup().getButtons().isEmpty()) {
            buttonsGroupRepository
                    .findByComponentId(update.getComponent().getId())
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
                    update.getComponent().getId()
            )) {
                throw new ChatBotConstructorException(
                        String.format(
                                "Buttons group id(%d) "
                                        + "does not belong to this builderComponent(id=%d, botSchemeId=%d)",
                                update.getButtonsGroup().getId(),
                                update.getComponent().getId(),
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

    public SuccessResponse addSchemeGroup(
            final String authToken,
            final int schemeId,
            final SchemeComponentGroup group
    ) {
        botSchemeService.getBotScheme(authToken, schemeId);

        group.setId(null);
        group.setSchemeId(schemeId);

        if (StringUtils.isEmpty(group.getTitle())) {
            throw new ChatBotConstructorException(
                    String.format("New component group(%s) has empty title", group),
                    Error.SCHEME_BUILDER_COMPONENT_GROUP_HAS_EMPTY_TITLE
            );
        }

        checkGroupType(group);

        return new SuccessResponseWrapper<>(componentGroupRepository.save(group));
    }

    public SuccessResponse updateSchemeGroup(
            final String authToken,
            final int schemeId,
            final SchemeComponentGroup group
    ) {
        getGroup(botSchemeService.getBotScheme(authToken, group.getSchemeId()), group.getId());

        if (StringUtils.isEmpty(group.getTitle())) {
            throw new ChatBotConstructorException(
                    String.format("New component group(%s) has empty title", group),
                    Error.SCHEME_BUILDER_COMPONENT_GROUP_HAS_EMPTY_TITLE
            );
        }

        checkGroupType(group);

        return new SuccessResponseWrapper<>(componentGroupRepository.save(group));
    }

    private void checkGroupType(final SchemeComponentGroup group) {
        if (group.getType() == null || GroupType.getById(group.getType()) == null) {
            throw new ChatBotConstructorException(
                    String.format(
                            "Group(%s, botSchemeId=%d) has illegal type = %d",
                            group, group.getSchemeId(), group.getType()
                    ),
                    Error.SCHEME_BUILDER_COMPONENT_GROUP_TYPE_IS_ILLEGAL
            );
        }
    }

    public SuccessResponse getSchemeGroup(final String authToken, final int schemeId, final long groupId) {
        SchemeComponentGroup group = getGroup(
                botSchemeService.getBotScheme(authToken, schemeId),
                groupId
        );
        return new SuccessResponseWrapper<>(group);
    }

    public SuccessResponse getSchemeGroups(final String authToken, final int schemeId) {
        botSchemeService.getBotScheme(authToken, schemeId);
        Iterable<SchemeComponentGroup> groups =
                componentGroupRepository
                        .findAllBySchemeId(schemeId);
        return new SuccessResponseWrapper<>(groups);
    }

    public SuccessResponse removeSchemeGroup(final String authToken, final int schemeId, final long groupId) {
        SchemeComponentGroup group = getGroup(
                botSchemeService.getBotScheme(authToken, schemeId),
                groupId
        );
        group.setSchemeId(null);
        componentGroupRepository.save(group);
        return new SuccessResponse();
    }

    private SchemeComponentGroup getGroup(final BotScheme botScheme, final Long groupId) {
        return componentGroupRepository
                .findByIdAndSchemeId(groupId, botScheme.getId())
                .orElseThrow(
                        () -> new NotFoundEntityException(
                                String.format(
                                        "Component group(id=%d, botSchemeId=%d) is not found",
                                        groupId, botScheme.getId()
                                ),
                                Error.SCHEME_BUILDER_COMPONENT_GROUP_IS_NOT_FOUND
                        )
                );
    }
}
