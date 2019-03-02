package chat.tamtam.bot.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "User")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "login")
    private @NonNull String login;

    @Column(name = "passwordHash")
    private @NonNull String passwordHash;

    @Column(name = "options")
    private Long options;
}
