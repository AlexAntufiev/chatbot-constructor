package chat.tamtam.bot.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import chat.tamtam.bot.domain.bot.BotSchemeEntity;
import chat.tamtam.bot.domain.builder.component.Component;
import chat.tamtam.bot.domain.builder.component.Update;
import chat.tamtam.bot.domain.builder.validator.Validator;
import chat.tamtam.bot.domain.exception.ChatBotConstructorException;
import chat.tamtam.bot.domain.response.SuccessResponse;
import chat.tamtam.bot.domain.response.SuccessResponseWrapper;
import chat.tamtam.bot.repository.BotSchemeRepository;
import chat.tamtam.bot.repository.ComponentRepository;
import chat.tamtam.bot.repository.ComponentValidatorRepository;
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
            final List<Update> updates
    ) {
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        // @todo #CC-141 Add component validation(check that current component id was reserved for this bot scheme)

        Object updatedComponents =
                transactionalUtils.invokeCallable(() -> {
                    List<Update> updated = new ArrayList<>();
                    for (Update update
                            : updates) {

                        /*if(!componentRepository
                                .existByIdAndSchemeId(
                                        update.getComponent().getId(),
                                        update.getComponent().getSchemeId()
                                )
                        ) {
                            throw new NotFoundEntityException(
                                    String.format(
                                            "Reserved component with id=%d and botSchemeId=%d not found",
                                            update.getComponent().getId(),
                                            update.getComponent().getSchemeId()
                                    ),
                                    Error.SERVICE_NO_ENTITY
                            );
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
                        updated.add(new Update(component, validators));
                    }
                    botScheme.setSchema(updates.stream().findFirst().orElseThrow().getComponent().getId());
                    botSchemaRepository.save(botScheme);
                    return updated;
                });

        return new SuccessResponseWrapper<>(updatedComponents);
    }
}
