package chat.tamtam.bot.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

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
import chat.tamtam.botapi.model.MessageCallbackUpdate;
import chat.tamtam.botapi.model.MessageCreatedUpdate;
import chat.tamtam.botapi.model.Update;
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
            VoteEntry voteEntry = new VoteEntry(component.getText(), null);
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
                voteEntry.setValue(message.getMessage().getBody().getText());
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

    private void persistVote(final SchemeComponent component, final BotContext context) {
        BotVote botVote = new BotVote(
                context.getId().getBotSchemeId(),
                context.getId().getUserId(),
                component.getGroupId(),
                context.getVoteData()
        );

        voteRepository.save(botVote);
        context.setVoteData("[]".getBytes());
    }
}
