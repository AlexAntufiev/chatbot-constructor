package chat.tamtam.bot.service;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import chat.tamtam.bot.domain.bot.BotSchemeEntity;
import chat.tamtam.bot.domain.webhook.WebHookBotState;
import chat.tamtam.bot.repository.BotSchemaRepository;
import chat.tamtam.bot.repository.WebHookBotStateRepository;
import chat.tamtam.botapi.model.BotAddedToChatUpdate;
import chat.tamtam.botapi.model.BotRemovedFromChatUpdate;
import chat.tamtam.botapi.model.BotStartedUpdate;
import chat.tamtam.botapi.model.ChatTitleChangedUpdate;
import chat.tamtam.botapi.model.MessageCallbackUpdate;
import chat.tamtam.botapi.model.MessageCreatedUpdate;
import chat.tamtam.botapi.model.MessageEditedUpdate;
import chat.tamtam.botapi.model.MessageRemovedUpdate;
import chat.tamtam.botapi.model.MessageRestoredUpdate;
import chat.tamtam.botapi.model.Update;
import chat.tamtam.botapi.model.UserAddedToChatUpdate;
import chat.tamtam.botapi.model.UserRemovedFromChatUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class WebHookBotService {
    private final WebHookBotStateRepository webHookBotStateRepository;
    private final BotSchemaRepository botSchemaRepository;

    public void submit(final long botId, final Update update) {
        update.visit(new WebHookBotVisitor(botId));
    }

    @RequiredArgsConstructor
    private class WebHookBotVisitor implements Update.Visitor {
        private final long botId;

        private WebHookBotState getState(final long userId, final long botId) {
            return webHookBotStateRepository
                    .findByIdUserIdAndIdBotId(userId, botId)
                    .orElseThrow(NoSuchElementException::new);
        }

        @Override
        public void visit(MessageCreatedUpdate model) {
            Long userId = model.getMessage().getSender().getUserId();
            WebHookBotState state = getState(userId, botId);

        }

        @Override
        public void visit(MessageCallbackUpdate model) {

        }

        @Override
        public void visit(MessageEditedUpdate model) {

        }

        @Override
        public void visit(MessageRemovedUpdate model) {

        }

        @Override
        public void visit(MessageRestoredUpdate model) {

        }

        @Override
        public void visit(BotAddedToChatUpdate model) {

        }

        @Override
        public void visit(BotRemovedFromChatUpdate model) {

        }

        @Override
        public void visit(UserAddedToChatUpdate model) {

        }

        @Override
        public void visit(UserRemovedFromChatUpdate model) {

        }

        @Override
        public void visit(BotStartedUpdate model) {
            BotSchemeEntity botScheme = botSchemaRepository.findByBotId(botId);
            WebHookBotState state = new WebHookBotState();
            state.setId(new WebHookBotState.Id(model.getUserId(), botId));
            state.setState(botScheme.getSchema());
            webHookBotStateRepository.save(state);
        }

        @Override
        public void visit(ChatTitleChangedUpdate model) {

        }

        @Override
        public void visitDefault(Update model) {

        }
    }
}
