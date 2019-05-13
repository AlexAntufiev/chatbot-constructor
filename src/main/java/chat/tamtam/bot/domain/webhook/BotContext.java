package chat.tamtam.bot.domain.webhook;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.ArrayUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table
public class BotContext {
    @EmbeddedId
    private Id id;

    private Long state;

    private String variables;

    private byte[] pendingMessage;

    @JsonIgnore
    private byte[] voteData = "[]".getBytes();

    /*@Column(nullable = false)
    private Long resetState;*/

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class Id implements Serializable {
        @Column(name = "userId", nullable = false)
        private Long userId;
        @Column(name = "botSchemeId", nullable = false)
        private Integer botSchemeId;
    }

    public void addVoteData(final byte[] data) {
        ArrayUtils.addAll(voteData, data);
    }

    //    public String getVoteDataAsJson() {
    //        return new String(voteData);
    //    }
}
