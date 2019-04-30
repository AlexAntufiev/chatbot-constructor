package chat.tamtam.bot.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import chat.tamtam.bot.domain.bot.BotSchemeEntity;
import chat.tamtam.bot.domain.builder.component.Component;
import chat.tamtam.bot.domain.builder.component.ComponentUpdate;
import chat.tamtam.bot.domain.builder.validator.Validator;
import chat.tamtam.bot.domain.exception.ChatBotConstructorException;
import chat.tamtam.bot.domain.response.SuccessResponse;
import chat.tamtam.bot.domain.response.SuccessResponseWrapper;
import chat.tamtam.bot.repository.BotSchemeRepository;
import chat.tamtam.bot.repository.ComponentRepository;
import chat.tamtam.bot.repository.ComponentValidatorRepository;
import chat.tamtam.bot.utils.TransactionalUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BuilderService {
    private final ComponentRepository componentRepository;
    private final ComponentValidatorRepository validatorRepository;
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

    public SuccessResponse saveBotScheme(
            final String authToken,
            final int botSchemeId,
            final List<ComponentUpdate> componentUpdates
    ) {
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        // @todo #CC-141 Add component validation(check that current component id was reserved for this bot scheme)

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
                        /*if (update.getComponent().getNextComponent() != null) {
                            if (!componentRepository.existsByIdAndAndSchemeId(
                                    update.getComponent().getNextComponent(),
                                    update.getComponent().getSchemeId()
                            )) {
                                throw new NotFoundEntityException(
                                        String.format(
                                                "Next component(id=%d, botSchemeId=%d) " +
                                                        "for component(id=%d, botSchemeId=%d) was not found",
                                                update.getComponent().getNextComponent(),
                                                update.getComponent().getSchemeId(),
                                                update.getComponent().getId(),
                                                update.getComponent().getSchemeId()
                                                ),
                                        Error.SERVICE_NO_ENTITY
                                );
                            }
                        }*/

                        for (Validator validator
                                : update.getValidators()) {
                            if (!update.getComponent().getId().equals(validator.getComponentId())) {
                                throw new ChatBotConstructorException(
                                        String.format(
                                                "Invalid validator componentId=%d(should be %d)",
                                                validator.getComponentId(),
                                                update.getComponent().getId()
                                        ),
                                        Error.BOT_SCHEME_INVALID_VALIDATOR
                                );
                            }
                        }

                        update.getComponent().setSchemeId(botScheme.getId());
                        Component component = componentRepository.save(update.getComponent());
                        List<Validator> validators =
                                Lists.newArrayList(validatorRepository.saveAll(update.getValidators()));
                        updated.add(new ComponentUpdate(component, validators));
                    }
                    botScheme.setSchema(componentUpdates.stream().findFirst().orElseThrow().getComponent().getId());
                    botSchemaRepository.save(botScheme);
                    return updated;
                });

        return new SuccessResponseWrapper<>(updatedComponents);
    }
}
