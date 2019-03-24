package chat.tamtam.bot.domain.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class UserAuthEntity {
    private @NonNull String login;
    private @NonNull String password;
}
