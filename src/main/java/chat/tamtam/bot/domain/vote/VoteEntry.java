package chat.tamtam.bot.domain.vote;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VoteEntry {
    private String field;
    private Value value;

    public VoteEntry(final String field, final Value value) {
        this.field = field;
        this.value = value;
    }
}
