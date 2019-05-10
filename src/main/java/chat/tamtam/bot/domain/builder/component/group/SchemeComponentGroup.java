package chat.tamtam.bot.domain.builder.component.group;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class SchemeComponentGroup {
    @Id
    @GeneratedValue
    private Long id;

    private String title;
}
