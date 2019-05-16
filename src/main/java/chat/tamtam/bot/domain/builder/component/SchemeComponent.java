package chat.tamtam.bot.domain.builder.component;

import javax.persistence.Column;
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

    @Column(columnDefinition = "text")
    private String text;

    private Long nextState;

    @Column(nullable = false)
    private boolean persistVoteContext = false;

    private Long groupId;

    private Integer sequence;

    @JsonIgnore
    private boolean hasCallbacks;

    private String title;
}
