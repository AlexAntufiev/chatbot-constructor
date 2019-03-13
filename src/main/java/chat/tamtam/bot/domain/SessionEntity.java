package chat.tamtam.bot.domain;

import java.util.Date;

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
@Table(name = "Session")
@NoArgsConstructor
@Data
@RequiredArgsConstructor
public class SessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "token")
    private @NonNull String token;

    @Column(name = "userId")
    private @NonNull Integer userId;

    @Column(name = "login")
    private @NonNull String login;

    @Column(name = "expireDate")
    private @NonNull Date expireDate;

    public boolean isExpired() {
        return expireDate.before(new Date());
    }
}
