package chat.tamtam.bot.domain.bot.hockey;

import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Team {

    AUSTRIA(599, "Австрия", "\uD83C\uDDE6\uD83C\uDDF9"),
    GERMANY(601, "Германия", "Германия\uD83C\uDDE9\uD83C\uDDEA"),
    DENMARK(602, "Дания", "Дания\uD83C\uDDE9\uD83C\uDDF0"),
    CANADA(603, "Канада", "Канада\uD83C\uDDE8\uD83C\uDDE6"),
    LATVIA(604, "Латвия", "Латвия\uD83C\uDDF1\uD83C\uDDFB"),
    NORWAY(605, "Норвегия", "Норвегия\uD83C\uDDF3\uD83C\uDDF4"),
    RUSSIA(606, "Россия", "Россия\uD83C\uDDF7\uD83C\uDDFA"),
    SLOVAKIA(607, "Словакия", "Словакия\uD83C\uDDF8\uD83C\uDDF0"),
    USA(609, "США", "США\uD83C\uDDFA\uD83C\uDDF8"),
    FINLAND(610, "Финляндия", "Финляндия\uD83C\uDDEB\uD83C\uDDEE"),
    FRANCE(611, "Франция", "Франция\uD83C\uDDEB\uD83C\uDDF7"),
    CZECH(612, "Чехия", "Чехия\uD83C\uDDE8\uD83C\uDDFF"),
    SWITZERLAND(613, "Швейцария", "Швейцария\uD83C\uDDE8\uD83C\uDDED"),
    SWEDEN(614, "Швеция", "Швеция\uD83C\uDDF8\uD83C\uDDEA"),
    ITALY(993, "Италия", "Италия\uD83C\uDDEE\uD83C\uDDF9"),
    GREAT_BRITAIN(1533, "Великобритания", "Великобритания \uD83C\uDDEC\uD83C\uDDE7"),
    ;

    // @todo #CC-173 add hashmap realization

    private final int id;
    private final String name;
    private final String nameWithSmile;

    public static int getIdByName(String name) {
        return Stream.of(values())
                .parallel()
                .filter(team -> team.name.equals(name))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Team with name %s not found", name))).id;
    }
}
