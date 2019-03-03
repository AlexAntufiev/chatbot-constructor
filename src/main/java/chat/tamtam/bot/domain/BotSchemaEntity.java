package chat.tamtam.bot.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "BotSchema")
@NoArgsConstructor
@Data
@RequiredArgsConstructor
public class BotSchemaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "userId")
    private @NonNull Integer userId;

    @Column(name = "token")
    private String token;

    @Column(name = "schema")
    private byte[] schema;
}
