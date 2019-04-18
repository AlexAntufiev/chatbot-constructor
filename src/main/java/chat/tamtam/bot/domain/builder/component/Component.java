package chat.tamtam.bot.domain.builder.component;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "BuilderComponent")
@NoArgsConstructor
public class Component {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "schemeId")
    private Integer schemeId;
    @Column(name = "type")
    private Byte type;
    @Column(name = "text")
    private String text;
    @Column(name = "nextComponent")
    private Long nextComponent;
}
