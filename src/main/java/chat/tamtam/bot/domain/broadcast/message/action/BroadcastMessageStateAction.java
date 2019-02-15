package chat.tamtam.bot.domain.broadcast.message.action;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import chat.tamtam.bot.domain.broadcast.message.BroadcastMessage;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageState;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageUpdate;
import chat.tamtam.bot.domain.exception.UpdateBroadcastMessageException;
import chat.tamtam.bot.service.Error;

public abstract class BroadcastMessageStateAction {

    public abstract void doAction(
            BroadcastMessage broadcastMessage,
            BroadcastMessageUpdate broadcastMessageUpdate
    );

    protected ZonedDateTime parseZonedDateTime(
            final String zonedDateTime,
            final Error malformedDateTimeError
    ) {
        try {
            return ZonedDateTime.parse(
                    zonedDateTime,
                    DateTimeFormatter.RFC_1123_DATE_TIME
            );
        } catch (DateTimeException ex) {
            throw new UpdateBroadcastMessageException(
                    ex.getLocalizedMessage(),
                    malformedDateTimeError
            );
        }
    }

    protected Instant getInstant(
            final String gmtDateTime,
            final Instant pastTime,
            final long broadcastMessageId,
            final Error malformedDateTimeError,
            final Error timeSequenceError
    ) {
        Instant futureInstant =
                parseZonedDateTime(gmtDateTime, malformedDateTimeError)
                        .toInstant();
        if (futureInstant.isBefore(pastTime)) {
            throw new UpdateBroadcastMessageException(
                    String.format(
                            "Future time=%s is in the past, past time=%s, message id=%d",
                            futureInstant,
                            pastTime,
                            broadcastMessageId
                    ),
                    timeSequenceError
            );
        }
        return futureInstant;
    }

    protected void updateText(
            final BroadcastMessage broadcastMessage,
            final BroadcastMessageUpdate broadcastMessageUpdate
    ) {
        if (broadcastMessageUpdate.getText() == null) {
            return;
        }
        setText(broadcastMessage, broadcastMessageUpdate);
    }

    protected abstract void setText(
            BroadcastMessage broadcastMessage,
            BroadcastMessageUpdate broadcastMessageUpdate
    );

    protected void rejectTextUpdate(
            final BroadcastMessage broadcastMessage,
            final BroadcastMessageUpdate broadcastMessageUpdate
    ) {
        throw new UpdateBroadcastMessageException(
                String.format(
                        "Can't update broadcast message text, because message with id=%d is in state=%s",
                        broadcastMessage.getId(),
                        BroadcastMessageState.getById(broadcastMessage.getState()).name()
                ),
                Error.BROADCAST_MESSAGE_ILLEGAL_STATE
        );
    }

    protected void applyTextUpdate(
            final BroadcastMessage broadcastMessage,
            final BroadcastMessageUpdate broadcastMessageUpdate
    ) {
        if (broadcastMessageUpdate.getText().isEmpty()) {
            throw new UpdateBroadcastMessageException(
                    String.format(
                            "Can't update broadcastMessage's text with id=%d because new title is empty",
                            broadcastMessage.getId()
                    ),
                    Error.BROADCAST_MESSAGE_TEXT_IS_EMPTY
            );
        }
        broadcastMessage.setText(broadcastMessageUpdate.getText());
    }
}
