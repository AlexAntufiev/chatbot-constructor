package chat.tamtam.bot.domain;

import javax.persistence.*;

@Entity
@Table(name = "Bot")
public class Bot {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;

    @Column(name = "userId")
    private Integer userId;

    @Column(name = "token")
    private String token;

    @Column(name = "schema")
    private byte[] schema;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
