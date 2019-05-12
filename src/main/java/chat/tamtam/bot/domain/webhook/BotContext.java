package chat.tamtam.bot.domain.webhook;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

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

    private Instant schemeUpdate;

    private byte[] pendingMessage;

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
}
