package chat.tamtam.bot.service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import chat.tamtam.bot.domain.builder.button.ButtonPayload;
import chat.tamtam.bot.domain.builder.component.ComponentType;
import chat.tamtam.bot.domain.builder.component.SchemeComponent;
import chat.tamtam.bot.domain.builder.validator.ComponentValidator;
import chat.tamtam.bot.domain.builder.validator.ValidatorType;
import chat.tamtam.bot.domain.builder.validator.wrapper.EqualTextValidatorWrapper;
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

    /*
     * Process builderComponent with type INFO
     * */
    public void process(
            final BotContext context,
            final SchemeComponent schemeComponent,
            final TamTamBotAPI api
    ) {
        try {
            SendMessageResult result =
                    api.sendMessage(
                            new NewMessageBody(
                                    schemeComponent.getText(),
                                    getAttachments(schemeComponent.getId(), true)
                            )
                    ).userId(context.getId().getUserId())
                            .execute();
            if (schemeComponent.isHasCallbacks()) {
                final String mid = result.getMessage().getBody().getMid();
                context.setPendingMessage(
                        ByteBuffer.allocate(Long.BYTES + mid.length())
                                .putLong(schemeComponent.getId())
                                .put(mid.getBytes())
                                .array()
                );
            }
            context.setState(schemeComponent.getNextState());
        } catch (APIException | ClientException e) {
            log.error(
                    String.format(
                            "Message sending produced exception(%s, %s, %s)",
                            context, schemeComponent, api
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
     * Process builderComponent with type INPUT on MessageCreatedUpdate
     * */
    public void process(
            final MessageCreatedUpdate update,
            final BotContext context,
            final SchemeComponent schemeComponent,
            final TamTamBotAPI api
    ) {
        // update pending message
        updatePendingMessage(context, api);

        Iterable<ComponentValidator> validators = validatorRepository.findAllByComponentId(schemeComponent.getId());
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
        }
        context.setState(schemeComponent.getNextState());
    }

    /*
     * Process builderComponent with type INPUT on MessageCallbackUpdate
     * */
    public void process(
            final MessageCallbackUpdate update,
            final BotContext context,
            final SchemeComponent schemeComponent,
            final TamTamBotAPI api
    ) {
        try {
            ButtonPayload payload = new ButtonPayload(update.getCallback().getPayload());
            componentRepository.findById(payload.getNextState())
                    .ifPresentOrElse(
                            foundComponent -> {
                                // check type of next builderComponent
                                if (ComponentType.getById(foundComponent.getType()) == ComponentType.INFO) {
                                    // answer on callback with next builderComponent
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
                                        context.setState(null);
                                        return;

                                    }
                                    context.setState(foundComponent.getNextState());
                                    if (foundComponent.isHasCallbacks()) {
                                        context.setPendingMessage(
                                                ByteBuffer
                                                        .wrap(context.getPendingMessage())
                                                        .putLong(foundComponent.getId())
                                                        .array()
                                        );
                                    } else {
                                        context.setPendingMessage(null);
                                    }

                                } else {
                                    // just notification
                                    final ButtonPayload buttonPayload
                                            = new ButtonPayload(update.getCallback().getPayload());
                                    final boolean success =
                                            answerOnCallback(
                                                    new CallbackAnswer()
                                                            .userId(context.getId().getUserId())
                                                            .notification(buttonPayload.getValue()),
                                                    update.getCallback().getCallbackId(),
                                                    context,
                                                    api
                                            ).isSuccess();
                                    updatePendingMessage(context, api);
                                }
                            },
                            () -> {
                                context.setState(null);
                            }
                    );
        } catch (RuntimeException e) {
            log.error(
                    String.format(
                            "MessageCallback processing produced exception(%s, %s, %s, %s)",
                            update, context, schemeComponent, api
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
        }
        return new SimpleQueryResult(false);
    }

    public void updatePendingMessage(final BotContext context, final TamTamBotAPI api) {
        if (context.getPendingMessage() == null) {
            return;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(context.getPendingMessage());
        final Long componentId = byteBuffer.getLong();
        byte[] midBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(midBytes);
        final String mid = new String(midBytes);
        componentRepository.findById(componentId)
                .ifPresentOrElse(
                        component -> {
                            try {
                                boolean success =
                                        api.editMessage(
                                                new NewMessageBody(
                                                        component.getText(),
                                                        getAttachments(componentId, false)
                                                ),
                                                mid
                                        ).execute().isSuccess();
                                context.setPendingMessage(null);
                            } catch (ClientException | APIException e) {
                                log.error(
                                        String.format(
                                                "Pending message update failed(%s, %s, mid=%s, %s)",
                                                context, api, mid, component
                                        ),
                                        e
                                );
                            }
                        },
                        () -> {
                            // Maybe delete message if cannot update
                        }
                );
    }
}
