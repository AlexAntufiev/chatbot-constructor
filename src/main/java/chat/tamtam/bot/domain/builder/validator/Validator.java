package chat.tamtam.bot.domain.builder.validator;

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
@NoArgsConstructor
@Table(name = "ComponentValidator")
public class Validator {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "type", nullable = false)
    private byte type;
    @Column(name = "bytes", nullable = false)
    private byte[] bytes;
    @Column(name = "componentId", nullable = false)
    private Long componentId;
    @Column(name = "failState", nullable = false)
    private Long failState;

    public void setBytes(final String src) {
        bytes = src.getBytes();
    }
}
