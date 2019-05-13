package chat.tamtam.bot.domain.builder.action;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table
@Entity
@NoArgsConstructor
public class SchemeAction {
    @Id
    @GeneratedValue
    private Long id;

    private Long componentId;

    private Integer sequence;

    private Byte type;
}
