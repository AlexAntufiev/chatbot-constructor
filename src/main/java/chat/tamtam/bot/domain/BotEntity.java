package chat.tamtam.bot.domain;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GenerationType;
import javax.persistence.Column;

@Entity
@Table(name = "Bot")
public class BotEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "userId")
    private Integer userId;

    @Column(name = "token")
    private String token;

    @Column(name = "schema")
    private byte[] schema;

    BotEntity() {

    }

    public BotEntity(Integer userId, String token, byte[] schema) {
        this.userId = userId;
        this.token = token;
        this.schema = schema;
    }

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

    public byte[] getSchema() {
        return schema;
    }

    public void setSchema(byte[] schema) {
        this.schema = schema;
    }
}
