package chat.tamtam.bot.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import chat.tamtam.bot.domain.builder.action.SchemeAction;
import chat.tamtam.bot.domain.builder.action.SchemeActionType;
import chat.tamtam.bot.domain.builder.button.ButtonPayload;
import chat.tamtam.bot.domain.builder.component.ComponentType;
import chat.tamtam.bot.domain.builder.component.SchemeComponent;
import chat.tamtam.bot.domain.vote.BotVote;
import chat.tamtam.bot.domain.vote.Value;
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

                Value value = new Value();

                value.setText(message.getMessage().getBody().getText());

                List<Attachment> attachments = message.getMessage().getBody().getAttachments();

                if (attachments != null && !attachments.isEmpty()) {

                    List<chat.tamtam.bot.domain.vote.Attachment> list =
                            attachments
                                    .stream()
                                    .map(attachment -> Optional.ofNullable(asAttachment(attachment)))
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .collect(Collectors.toList());

                    value.setAttachments(list);
                }

                voteEntry.setValue(value);
            }

            if (update instanceof MessageCallbackUpdate) {
                MessageCallbackUpdate callback = ((MessageCallbackUpdate) update);
                ButtonPayload payload = new ButtonPayload(callback.getCallback().getPayload());
                Value value = new Value();
                value.setText(payload.getValue());
                value.setAttachments(Collections.emptyList());
                voteEntry.setValue(value);
            }

            context.setVoteData(mapper.writeValueAsBytes(entries));
        } catch (IOException e) {
            log.error(String.format("Can't parse context(%s) vote data", context), e);
        }
    }

    private chat.tamtam.bot.domain.vote.Attachment asAttachment(Attachment attachment) {
        return new AttachmentVisitor(attachment).getAttachment();
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
        private chat.tamtam.bot.domain.vote.Attachment attachment;

        AttachmentVisitor(final Attachment attachment) {
            this.attachment = new chat.tamtam.bot.domain.vote.Attachment();
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
                    this.attachment = null;
            }
        }

        @Override
        public void visit(PhotoAttachment model) {
            attachment.setType(model.getType());
            attachment.setUrl(model.getPayload().getUrl());
        }

        @Override
        public void visit(VideoAttachment model) {
            attachment.setType(model.getType());
            attachment.setUrl(model.getPayload().getUrl());
        }

        @Override
        public void visit(AudioAttachment model) {
            attachment.setType(model.getType());
            attachment.setUrl(model.getPayload().getUrl());
        }

        @Override
        public void visit(FileAttachment model) {
            attachment.setType(model.getType());
            attachment.setUrl(model.getPayload().getUrl());
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
