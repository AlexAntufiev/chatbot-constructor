package chat.tamtam.bot.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "Session")
@NoArgsConstructor
@Data
public class SessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "token")
    @NonNull
    private String token;

    @Column(name = "userId")
    @NonNull
    private Integer userId;

    @Column(name = "login")
    @NonNull
    private String login;

    @Column(name = "expireDate")
    @NonNull
    private Date expireDate;
}
