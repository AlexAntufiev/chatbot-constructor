package chat.tamtam.bot.domain.bot.hockey;

import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Team {

    AUSTRIA(599, "Австрия"),
    GERMANY(601, "Германия"),
    DENMARK(602, "Дания"),
    CANADA(603, "Канада"),
    LATVIA(604, "Латвия"),
    NORWAY(605, "Норвегия"),
    RUSSIA(606, "Россия"),
    SLOVAKIA(607, "Словакия"),
    USA(609, "США"),
    FINLAND(610, "Финляндия"),
    FRANCE(611, "Франция"),
    CZECH(612, "Чехия"),
    SWITZERLAND(613, "Швейцария"),
    SWEDEN(614, "Швеция"),
    ITALY(993, "Италия"),
    GREAT_BRITAIN(1533, "Великобритания"),
    ;

    private final int id;
    private final String name;

    public static int getIdByName(String name) {
        return Stream.of(values())
                .parallel()
                .filter(team -> team.name.equals(name))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Team with name %s not found", name))).id;
    }
}
