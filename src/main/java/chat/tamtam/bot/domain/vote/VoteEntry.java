package chat.tamtam.bot.domain.vote;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VoteEntry {
    public static final String AUTHOR_BOT = "bot";
    public static final String AUTHOR_USER = "user";

    private String author;
    private String data;

    public VoteEntry(final String author, final String data) {
        this.author = author;
        this.data = data;
    }
}
