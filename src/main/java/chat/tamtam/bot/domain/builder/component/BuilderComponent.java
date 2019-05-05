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
public class BuilderComponent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "schemeId")
    private Integer schemeId;
    @Column(name = "type")
    private Byte type;
    @Column(name = "text")
    private String text;
    @Column(name = "nextState")
    private Long nextState;
    @Column(name = "groupId")
    private Long groupId;
    @JsonIgnore
    @Column(name = "hasCallbacks")
    private boolean hasCallbacks;
}
