package chat.tamtam.bot.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import chat.tamtam.bot.domain.builder.button.ButtonPayload;
import chat.tamtam.bot.domain.builder.component.Component;
import chat.tamtam.bot.domain.builder.validator.Validator;
import chat.tamtam.bot.domain.builder.validator.ValidatorType;
import chat.tamtam.bot.domain.builder.validator.wrapper.EqualTextValidatorWrapper;
import chat.tamtam.bot.domain.webhook.BotContext;
import chat.tamtam.bot.repository.ButtonsGroupRepository;
import chat.tamtam.bot.repository.ComponentValidatorRepository;
import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.AttachmentRequest;
import chat.tamtam.botapi.model.CallbackAnswer;
import chat.tamtam.botapi.model.InlineKeyboardAttachmentRequest;
import chat.tamtam.botapi.model.InlineKeyboardAttachmentRequestPayload;
import chat.tamtam.botapi.model.MessageCallbackUpdate;
import chat.tamtam.botapi.model.MessageCreatedUpdate;
import chat.tamtam.botapi.model.NewMessageBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class ComponentProcessorService {
    private final ComponentValidatorRepository validatorRepository;
    private final ButtonsGroupRepository buttonsRepository;

    /*
     * Process component with type INFO
     * */
    public void process(
            final BotContext context,
            final Component component,
            final TamTamBotAPI api
    ) {
        try {
            api.sendMessage(
                    new NewMessageBody(
                            component.getText(),
                            getAttachments(component.getId())
                    )
            ).userId(context.getId().getUserId())
                    .execute();
            context.setState(component.getNextComponent());
        } catch (APIException | ClientException e) {
            log.error(
                    String.format(
                            "Message sending produced exception(context=%s, component=%s)",
                            context, component
                    ),
                    e
            );
            context.setState(null);
        }
    }

    private List<AttachmentRequest> getAttachments(final Long componentId) {
        List<AttachmentRequest> attachments = new ArrayList<>();
        buttonsRepository
                .findByComponentId(componentId)
                .ifPresent(group ->
                        attachments.add(
                                new InlineKeyboardAttachmentRequest(
                                        new InlineKeyboardAttachmentRequestPayload(group.getTamButtons())
                                )
                        )
                );
        return attachments;
    }

    /*
     * Process component with type INPUT on MessageCreatedUpdate
     * */
    public void process(
            final MessageCreatedUpdate update,
            final BotContext context,
            final Component component,
            final TamTamBotAPI api
    ) {
        Iterable<Validator> validators = validatorRepository.findAllByComponentId(component.getId());
        for (Validator validator
                : validators) {
            switch (ValidatorType.getById(validator.getType())) {
                case EQUAL_TEXT:
                    EqualTextValidatorWrapper validatorWrapper = new EqualTextValidatorWrapper(validator);
                    if (!validatorWrapper.validate(update, context)) {
                        return;
                    }
                    break;
                default:
                    break;
            }
        }
        context.setState(component.getNextComponent());
    }

    /*
     * Process component with type INPUT on MessageCallbackUpdate
     * */
    public void process(
            final MessageCallbackUpdate update,
            final BotContext context,
            final Component component,
            final TamTamBotAPI api
    ) {
        try {
            ButtonPayload payload = ButtonPayload
                    .parseButtonPayload(update.getCallback().getPayload());
            api.answerOnCallback(
                    new CallbackAnswer()
                            .userId(context.getId().getUserId())
                            .message(new NewMessageBody(payload.getValue(), Collections.emptyList())),
                    update.getCallback().getCallbackId()
            ).execute();
            context.setState(payload.getNextState());
        } catch (ClientException | APIException e) {
            log.error(
                    String.format(
                            "Answer on callback produced exception(context=%s, component=%s, update=%s)",
                            context, component, update
                    ),
                    e
            );
            context.setState(null);
        } catch (IOException e) {
            // FIXME log exception
            context.setState(null);
        }
    }
}
