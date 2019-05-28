package chat.tamtam.bot.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import chat.tamtam.bot.domain.builder.action.SchemeAction;
import chat.tamtam.bot.domain.builder.action.SchemeActionType;
import chat.tamtam.bot.domain.builder.button.ButtonPayload;
import chat.tamtam.bot.domain.builder.component.ComponentType;
import chat.tamtam.bot.domain.builder.component.SchemeComponent;
import chat.tamtam.bot.domain.vote.BotVote;
import chat.tamtam.bot.domain.vote.VoteEntry;
import chat.tamtam.bot.domain.webhook.BotContext;
import chat.tamtam.bot.repository.BotVoteRepository;
import chat.tamtam.bot.repository.SchemeActionRepository;
import chat.tamtam.botapi.model.Attachment;
import chat.tamtam.botapi.model.AudioAttachment;
import chat.tamtam.botapi.model.ContactAttachment;
import chat.tamtam.botapi.model.FileAttachment;
import chat.tamtam.botapi.model.InlineKeyboardAttachment;
import chat.tamtam.botapi.model.LocationAttachment;
import chat.tamtam.botapi.model.MessageCallbackUpdate;
import chat.tamtam.botapi.model.MessageCreatedUpdate;
import chat.tamtam.botapi.model.PhotoAttachment;
import chat.tamtam.botapi.model.ShareAttachment;
import chat.tamtam.botapi.model.StickerAttachment;
import chat.tamtam.botapi.model.Update;
import chat.tamtam.botapi.model.VideoAttachment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class ActionProcessorService {
    private final SchemeActionRepository actionRepository;
    private final BotVoteRepository voteRepository;

    public void perform(
            final SchemeComponent component,
            final BotContext context,
            final Update update
    ) {
        Iterable<SchemeAction> actions = actionRepository.findAllByComponentIdOrderBySequence(component.getId());

        for (SchemeAction action
                : actions) {
            SchemeActionType type = SchemeActionType.getById(action.getType());
            if (type == null) {
                throw new RuntimeException(
                        String.format(
                                "Component(%s) action(%s) type is null(context=%s)",
                                component, action, context
                        )
                );
            }

            log.info(
                    String.format(
                            "Action(%s type=%s) performs on component(%s) with context(%s)",
                            action, type.name(), component, context
                    )
            );

            switch (type) {
                case STORE_VOTE_ENTRY:
                    storeVoteEntry(component, context, update);
                    break;

                case PERSIST_VOTE_TO_TABLE:
                    persistVote(component, context);
                    break;

                default:
                    throw new RuntimeException(
                            String.format(
                                    "Component(%s) action(%s) has illegal type(context=%s)",
                                    component, action, context
                            )
                    );
            }
        }
    }

    private void storeVoteEntry(final SchemeComponent component, final BotContext context, final Update update) {
        switch (ComponentType.getById(component.getType())) {
            case INFO:
                innerInfoStore(component, context);
                break;
            case INPUT:
                innerInputStore(update, context);
                break;
            default:
                throw new RuntimeException(
                        String.format(
                                "Can't store vote entry, because component(%s) has illegal type(context=%s)",
                                component, context
                        )
                );
        }
    }

    private void innerInfoStore(final SchemeComponent component, final BotContext context) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            List<VoteEntry> entries = mapper.readValue(context.getVoteData(), new TypeReference<List<VoteEntry>>() { });
            VoteEntry voteEntry = new VoteEntry(component.getTitle(), null);
            entries.add(voteEntry);

            context.setVoteData(mapper.writeValueAsBytes(entries));
        } catch (IOException e) {
            log.error(String.format("Can't parse context(%s) vote data", context), e);
        }
    }

    private void innerInputStore(final Update update, final BotContext context) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<VoteEntry> entries = mapper.readValue(context.getVoteData(), new TypeReference<List<VoteEntry>>() { });

            if (entries.isEmpty()) {
                throw new RuntimeException(
                        String.format(
                                "Can't store user input into vote entry, no entries found(update=%s,context=%s)",
                                update, context
                        )
                );
            }

            VoteEntry voteEntry = entries.get(entries.size() - 1);

            if (update instanceof MessageCreatedUpdate) {
                MessageCreatedUpdate message = ((MessageCreatedUpdate) update);

                StringBuilder valueBuilder = new StringBuilder();

                if (message.getMessage().getBody().getText() != null) {
                    valueBuilder.append(message.getMessage().getBody().getText());
                }

                List<Attachment> attachments = message.getMessage().getBody().getAttachments();

                if (attachments != null && !attachments.isEmpty()) {
                    for (Attachment attachment
                            : attachments) {
                        final String attachmentUrl = getAttachmentUrl(attachment);
                        if (StringUtils.isEmpty(attachmentUrl)) {
                            log.error(
                                    String.format(
                                            "Update with attachments(%s) contains unsupported attachment(%s),"
                                                    + " context(%s)",
                                            attachments, attachment, context
                                    )
                            );
                            continue;
                        }
                        if (valueBuilder.length() != 0) {
                            valueBuilder.append("\n");
                        }
                        valueBuilder.append(attachmentUrl);
                    }
                }

                voteEntry.setValue(valueBuilder.toString());
            }

            if (update instanceof MessageCallbackUpdate) {
                MessageCallbackUpdate callback = ((MessageCallbackUpdate) update);
                ButtonPayload payload = new ButtonPayload(callback.getCallback().getPayload());
                voteEntry.setValue(payload.getValue());
            }

            context.setVoteData(mapper.writeValueAsBytes(entries));
        } catch (IOException e) {
            log.error(String.format("Can't parse context(%s) vote data", context), e);
        }
    }

    private String getAttachmentUrl(Attachment attachment) {
        return new AttachmentVisitor(attachment).getUrl();
    }

    private void persistVote(final SchemeComponent component, final BotContext context) {
        BotVote botVote = new BotVote(
                context.getId().getBotSchemeId(),
                context.getId().getUserId(),
                component.getGroupId(),
                context.getVoteData()
        );

        botVote.setUsername(context.getUsername());

        voteRepository.save(botVote);
        context.setVoteData("[]".getBytes());
    }

    private static class AttachmentVisitor implements Attachment.Visitor {
        @Getter
        private String url;

        public AttachmentVisitor(final Attachment attachment) {
            switch (attachment.getType()) {

                case Attachment.IMAGE:
                    visit(((PhotoAttachment) attachment));
                    break;

                case Attachment.VIDEO:
                    visit(((VideoAttachment) attachment));
                    break;

                case Attachment.FILE:
                    visit(((FileAttachment) attachment));
                    break;

                case Attachment.AUDIO:
                    visit(((AudioAttachment) attachment));
                    break;

                default:
                    break;
            }
        }

        @Override
        public void visit(PhotoAttachment model) {
            url = model.getPayload().getUrl();
        }

        @Override
        public void visit(VideoAttachment model) {
            url = model.getPayload().getUrl();
        }

        @Override
        public void visit(AudioAttachment model) {
            url = model.getPayload().getUrl();
        }

        @Override
        public void visit(FileAttachment model) {
            url = model.getPayload().getUrl();
        }

        @Override
        public void visit(StickerAttachment model) {

        }

        @Override
        public void visit(ContactAttachment model) {

        }

        @Override
        public void visit(InlineKeyboardAttachment model) {

        }

        @Override
        public void visit(ShareAttachment model) {

        }

        @Override
        public void visit(LocationAttachment model) {

        }

        @Override
        public void visitDefault(Attachment model) {

        }
    }
}
