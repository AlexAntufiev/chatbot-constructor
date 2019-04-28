package chat.tamtam.bot.domain.bot.hockey;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Team {

    CANADA(603, "Канада"),
    FINLAND(610, "Финляндия"),
    NORWAY(605, "Норвегия"),
    RUSSIA(606, "Россия"),
    SLOVAKIA(607, "Словакия"),
    USA(609, "США"),
    SWEDEN(614, "Швеция"),
    CZECH(612, "Чехия"),
    FRANCE(611, "Франция"),
    DENMARK(602, "Дания"),
    GERMANY(603, "Германия"),
    SWITZERLAND(613, "Швейцария"),

    // @todo #CC-165 fix ids
    GREAT_BRITAIN(603, "Великобритания"),
    LATVIA(604, "Латвия"),
    AUSTRIA(603, "Австрия"),
    ITALY(603, "Италия"),
    ;

    private final int id;
    private final String name;
}
