package chat.tamtam.bot.domain.webhook;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "WebHookBotState")
public class WebHookBotState {
    @EmbeddedId
    private Id id;
    private Long state;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class Id implements Serializable {
        private Long userId;
        private Long botId;
    }
}
