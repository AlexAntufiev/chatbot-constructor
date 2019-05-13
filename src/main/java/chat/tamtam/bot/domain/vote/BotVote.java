package chat.tamtam.bot.domain.vote;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import chat.tamtam.bot.domain.exception.ChatBotConstructorException;
import chat.tamtam.bot.service.Error;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table
@NoArgsConstructor
public class BotVote {
    @Id
    @GeneratedValue
    private Long id;

    private Integer schemeId;

    private Long userId;

    private Instant time;

    @JsonIgnore
    private byte[] data;

    public BotVote(final Integer botSchemeId, final Long userId, final byte[] voteData) {
        schemeId = botSchemeId;
        this.userId = userId;
        data = voteData;
        time = Instant.now();
    }

    public Object getDataAsList() {
        try {
            return new ObjectMapper().readValue(data, new TypeReference<List<VoteEntry>>() { });
        } catch (IOException e) {
            throw new ChatBotConstructorException(
                    String.format("Can't parse vote(id=%d, schemeId=%d, time=%s) data", id, schemeId, time),
                    Error.SERVICE_ERROR,
                    e
            );
        }
    }
}
