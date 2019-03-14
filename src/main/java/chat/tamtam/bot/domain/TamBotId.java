package chat.tamtam.bot.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TamBotId implements Serializable {
    @Column(name = "botId", nullable = false)
    private Long botId;
    @Column(name = "userId", nullable = false)
    private Integer userId;
}
