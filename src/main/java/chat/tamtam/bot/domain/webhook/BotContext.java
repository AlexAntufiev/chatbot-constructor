package chat.tamtam.bot.domain.webhook;

import java.io.Serializable;

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
@Table(name = "WebHookBotContext")
public class BotContext {
    @EmbeddedId
    private Id id;
    @Column(name = "state")
    private Long state;
    @Column(name = "variables")
    private String variables;
    @Column(name = "pendingMessage")
    private byte[] pendingMessage;

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
