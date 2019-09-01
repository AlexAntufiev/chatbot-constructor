package chat.tamtam.bot.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import chat.tamtam.bot.domain.builder.button.ButtonPayload;
import chat.tamtam.bot.domain.builder.component.ComponentType;
import chat.tamtam.bot.domain.builder.component.SchemeComponent;
import chat.tamtam.bot.domain.webhook.BotContext;
import chat.tamtam.bot.repository.ButtonsGroupRepository;
import chat.tamtam.bot.repository.ComponentRepository;
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
import chat.tamtam.botapi.model.SendMessageResult;
import chat.tamtam.botapi.model.SimpleQueryResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class ComponentProcessorService {
    private final ComponentValidatorRepository validatorRepository;
    private final ButtonsGroupRepository buttonsRepository;
    private final ComponentRepository componentRepository;
    private final ActionProcessorService actionProcessor;

    /*
     * Process сomponent with type INFO
     * */
    public void process(
            final BotContext context,
            final SchemeComponent component,
            final TamTamBotAPI api
    ) {
        try {
            SendMessageResult result =
                    api.sendMessage(
                            new NewMessageBody(
                                    component.getText(),
                                    getAttachments(component.getId(), true)
                            )
                    ).userId(context.getId().getUserId())
                            .execute();
            context.setLastMessageId(result.getMessage().getBody().getSeq().toString());
            context.setState(component.getNextState());

            actionProcessor.perform(component, context, null);

        } catch (APIException | ClientException e) {
            log.error(
                    String.format(
                            "Message sending produced exception(%s, %s, %s)",
                            context, component, api
                    ),
                    e
            );
            context.setState(null);
        }
    }

    private List<AttachmentRequest> getAttachments(final Long componentId, final boolean withCallbacks) {
        List<AttachmentRequest> attachments = new ArrayList<>();
        if (withCallbacks) {
            buttonsRepository
                    .findByComponentId(componentId)
                    .ifPresent(group ->
                            attachments.add(
                                    new InlineKeyboardAttachmentRequest(
                                            new InlineKeyboardAttachmentRequestPayload(group.getTamButtons())
                                    )
                            )
                    );
        }
        return attachments;
    }

    /*
     * Process сomponent with type INPUT on MessageCreatedUpdate
     * */
    public void process(
            final MessageCreatedUpdate update,
            final BotContext context,
            final SchemeComponent component,
            final TamTamBotAPI api
    ) {
        /*Iterable<ComponentValidator> validators = validatorRepository.findAllByComponentId(component.getId());
        for (ComponentValidator componentValidator
                : validators) {
            switch (ValidatorType.getById(componentValidator.getType())) {
                case EQUAL_TEXT:
                    EqualTextValidatorWrapper validatorWrapper = new EqualTextValidatorWrapper(componentValidator);
                    if (!validatorWrapper.validate(update, context)) {
                        return;
                    }
                    break;
                default:
                    break;
            }
        }*/

        context.setUsername(update.getMessage().getSender().getName());

        context.setState(component.getNextState());

        actionProcessor.perform(component, context, update);
    }

    /*
     * Process сomponent with type INPUT on MessageCallbackUpdate
     * */
    public void process(
            final MessageCallbackUpdate update,
            final BotContext context,
            final SchemeComponent component,
            final TamTamBotAPI api
    ) {
        try {
            if (!update.getMessage().getBody().getSeq().toString().equals(context.getLastMessageId())) {

                CallbackAnswer answer = new CallbackAnswer();
                answer.message(new NewMessageBody(update.getMessage().getBody().getText(), null));
                answer.setUserId(update.getCallback().getUser().getUserId());

                answerOnCallback(
                        answer,
                        update.getCallback().getCallbackId(),
                        context,
                        api
                );
                return;
            }

            context.setUsername(update.getCallback().getUser().getName());

            actionProcessor.perform(component, context, update);

            ButtonPayload payload = new ButtonPayload(update.getCallback().getPayload());
            componentRepository.findById(payload.getNextState())
                    .ifPresentOrElse(
                            foundComponent -> {
                                // check type of next сomponent
                                if (ComponentType.getById(foundComponent.getType()) == ComponentType.INFO) {
                                    // answer on callback with next сomponent
                                    final boolean success =
                                            answerOnCallback(
                                                    new CallbackAnswer()
                                                            .userId(context.getId().getUserId())
                                                            .message(new NewMessageBody(
                                                                    foundComponent.getText(),
                                                                    getAttachments(foundComponent.getId(), true)
                                                            )),
                                                    update.getCallback().getCallbackId(),
                                                    context,
                                                    api
                                            ).isSuccess();
                                    if (!success) {
                                        log.error(
                                                String.format(
                                                        "%s update to %s with %s: callback response has success:false",
                                                        update, component, context
                                                )
                                        );
                                        context.setState(null);
                                        context.setSchemeUpdate(null);
                                        return;

                                    }
                                    context.setState(foundComponent.getNextState());

                                    actionProcessor.perform(foundComponent, context, null);

                                } else {
                                    // just notification
                                    final boolean success =
                                            answerOnCallback(
                                                    new CallbackAnswer()
                                                            .userId(context.getId().getUserId())
                                                            .notification(payload.getValue()),
                                                    update.getCallback().getCallbackId(),
                                                    context,
                                                    api
                                            ).isSuccess();
                                }
                            },
                            () -> {
                                log.error(
                                        String.format(
                                                "Component is not presented for %s with %s",
                                                update, context
                                        )
                                );
                                context.setSchemeUpdate(null);
                                context.setState(null);
                            }
                    );
        } catch (RuntimeException e) {
            log.error(
                    String.format(
                            "MessageCallback processing produced exception(%s, %s, %s, %s)",
                            update, context, component, api
                    ),
                    e
            );
            context.setState(null);
        }
    }

    private SimpleQueryResult answerOnCallback(
            final CallbackAnswer answer,
            final String callbackId,
            final BotContext context,
            final TamTamBotAPI api
    ) {
        try {
            return api
                    .answerOnCallback(answer, callbackId)
                    .execute();
        } catch (ClientException | APIException e) {
            log.error(
                    String.format(
                            "Answer on callback produced exception(%s, callbackId=%s %s, %s)",
                            answer, callbackId, context, api
                    ),
                    e
            );
            context.setSchemeUpdate(null);
            context.setState(null);
        }
        return new SimpleQueryResult(false);
    }
}
