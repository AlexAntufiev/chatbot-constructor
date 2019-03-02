package chat.tamtam.bot.domain;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

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
