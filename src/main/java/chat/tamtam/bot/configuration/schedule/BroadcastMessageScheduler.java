package chat.tamtam.bot.configuration.schedule;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import chat.tamtam.bot.domain.bot.BotSchemeEntity;
import chat.tamtam.bot.domain.bot.TamBotEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageState;
import chat.tamtam.bot.repository.BotSchemaRepository;
import chat.tamtam.bot.repository.BroadcastMessageRepository;
import chat.tamtam.bot.repository.TamBotRepository;
import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.NewMessageBody;
import chat.tamtam.botapi.model.SendMessageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@RequiredArgsConstructor
public class BroadcastMessageScheduler {
    private final BroadcastMessageRepository broadcastMessageRepository;
    private final BotSchemaRepository botSchemaRepository;
    private final TamBotRepository tamBotRepository;

    private final Executor executor;
    @Value("${tamtam.broadcast.executor.corePoolSize:1}")
    private int corePoolSize;
    @Value("${tamtam.broadcast.executor.maxPoolSize:1}")
    private int maxPoolSize;

    @Scheduled(fixedRate = SchedulerRates.DEFAULT_SENDING_RATE)
    public void fireScheduledMessages() {
        Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.now());
        List<BroadcastMessageEntity> scheduledMessages =
                broadcastMessageRepository.findAllByFiringTimeBeforeAndState(
                        currentTimestamp,
                        BroadcastMessageState.SCHEDULED.getValue()
                );
        scheduledMessages.forEach(e -> {
            BotSchemeEntity botScheme = botSchemaRepository.findById(e.getBotSchemeId()).orElseThrow();
            TamTamBotAPI tamTamBotAPI = getTamTamBotAPI(e, botScheme);
            if (tamTamBotAPI != null) {
                e.setState(BroadcastMessageState.PROCESSING);
                broadcastMessageRepository.save(e);
                executor.execute(() -> sendBroadcastMessageAsync(tamTamBotAPI, e));
            }
        });
    }

    @Scheduled(fixedRate = SchedulerRates.DEFAULT_ERASING_RATE)
    public void eraseScheduledMessages() {
        Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.now());
        List<BroadcastMessageEntity> scheduledMessages =
                broadcastMessageRepository.findAllByErasingTimeBeforeAndState(
                        currentTimestamp,
                        BroadcastMessageState.SENT.getValue()
                );
        scheduledMessages.forEach(e -> {
            BotSchemeEntity botScheme = botSchemaRepository.findById(e.getBotSchemeId()).orElseThrow();
            TamTamBotAPI tamTamBotAPI = getTamTamBotAPI(e, botScheme);
            if (tamTamBotAPI != null) {
                e.setState(BroadcastMessageState.PROCESSING);
                broadcastMessageRepository.save(e);
                executor.execute(() -> eraseBroadcastMessageAsync(tamTamBotAPI, e));
            }
        });
    }

    @Async
    protected void sendBroadcastMessageAsync(
            final TamTamBotAPI tamTamBotAPI,
            final BroadcastMessageEntity broadcastMessage
    ) {
        try {
            SendMessageResult sendMessageResult =
                    tamTamBotAPI
                            .sendMessage(new NewMessageBody(broadcastMessage.getText(), null))
                            .chatId(broadcastMessage.getChatChannelId())
                            .execute();
            broadcastMessage.setMessageId(sendMessageResult.getMessageId());
            broadcastMessage.setState(BroadcastMessageState.SENT);
        } catch (APIException | ClientException ex) {
            log.error(String.format("Can't send scheduled message with id=%d", broadcastMessage.getId()), ex);
            broadcastMessage.setState(BroadcastMessageState.ERROR);
        }
        broadcastMessageRepository.save(broadcastMessage);
    }

    @Async
    protected void eraseBroadcastMessageAsync(
            final TamTamBotAPI tamTamBotAPI,
            final BroadcastMessageEntity broadcastMessage
    ) {
        try {
            // @todo #CC-63 Replace editMessage method with removeMessage method when it become available
            tamTamBotAPI
                    .editMessage(
                            new NewMessageBody(".", Collections.emptyList()),
                            broadcastMessage.getMessageId()
                    ).execute();
            broadcastMessage.setState(BroadcastMessageState.ERASED);
        } catch (APIException | ClientException ex) {
            log.error(String.format("Can't erase scheduled message with id=%d", broadcastMessage.getId()), ex);
            broadcastMessage.setState(BroadcastMessageState.ERROR);
        }
        broadcastMessageRepository.save(broadcastMessage);
    }

    private @Nullable TamTamBotAPI getTamTamBotAPI(BroadcastMessageEntity e, BotSchemeEntity botScheme) {
        TamBotEntity tamBot =
                tamBotRepository.findById(new TamBotEntity.Id(botScheme.getBotId(), botScheme.getUserId()));
        if (tamBot == null) {
            log.error(
                    String.format(
                            "Can't process scheduled message with id=%d because related tamBot not found",
                            e.getId()
                    ),
                    e
            );
            return null;
        }
        return TamTamBotAPI.create(tamBot.getToken());
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setThreadNamePrefix("ScheduledBroadcastMessageProcess-");
        executor.initialize();
        return executor;
    }
}
