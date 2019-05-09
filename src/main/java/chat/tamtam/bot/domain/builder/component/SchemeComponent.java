package chat.tamtam.bot.domain.builder.component;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table
@NoArgsConstructor
public class SchemeComponent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Integer schemeId;

    private Byte type;

    private String text;

    private Long nextState;

    private Long groupId;

    private Long voteId;

    @JsonIgnore
    private boolean hasCallbacks;

    private String title;
}
