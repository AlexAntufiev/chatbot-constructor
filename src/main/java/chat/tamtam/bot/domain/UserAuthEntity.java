package chat.tamtam.bot.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class UserAuthEntity {
    @NonNull
    private String login;
    @NonNull
    private String password;
}
