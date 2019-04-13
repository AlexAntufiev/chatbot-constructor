package chat.tamtam.bot.converter;

@FunctionalInterface
public interface Converter<S, R> {
    R convert(S source);
}
