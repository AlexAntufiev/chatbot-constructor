package chat.tamtam.bot.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class UserAuthEntity {
    private @NonNull String login;
    private @NonNull String password;
}
