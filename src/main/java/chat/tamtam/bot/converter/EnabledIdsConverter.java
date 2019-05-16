package chat.tamtam.bot.converter;

import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class EnabledIdsConverter {
    public static final short PARTITIONS_AMOUNT = 256;

    public EnabledIds convert(final String source, final Class<?> invokerClass) {
        log.info(String.format("%s enabling...", invokerClass.getCanonicalName()));
        if (StringUtils.isEmpty(source)) {
            log.warn(
                    String.format(
                            "%s enabled for: partitions=%s, ids=%s",
                            invokerClass.getCanonicalName(), Collections.emptySet(), Collections.emptySet()
                    )
            );
            return new EnabledIdsImpl(Collections.emptySet(), PARTITIONS_AMOUNT, Collections.emptySet());
        }
        String[] args =
                source.trim()
                        .replaceAll(" ", "")
                        .split(",");
        Set<Short> partitions = new HashSet<>();
        Set<Long> ids = new HashSet<>();
        for (String arg
                : args) {
            Set<Short> parts = parsePartition(arg);
            if (parts.isEmpty()) {
                ids.addAll(parseId(arg));
            } else {
                partitions.addAll(parts);
            }
        }
        log.info(
                String.format(
                        "%s enabled for: partitions=%s, ids=%s",
                        invokerClass.getCanonicalName(), partitions, ids
                )
        );
        return new EnabledIdsImpl(partitions, PARTITIONS_AMOUNT, ids);
    }

    private static Set<Long> parseId(final String source) {
        try {
            long id = Long.parseLong(source);
            if (id < PARTITIONS_AMOUNT) {
                log.debug(String.format("Can't convert %s as id, because it less then %d", source, PARTITIONS_AMOUNT));
                return Collections.emptySet();
            }
            log.debug(String.format("Parsed %d as id", id));
            return Set.of(id);
        } catch (NumberFormatException e) {
            log.debug(String.format("Can't parse %s as id", source));
            return Collections.emptySet();
        }
    }

    private static Set<Short> parsePartitionsRange(final String source) {
        if (StringUtils.isEmpty(source)) {
            return Collections.emptySet();
        }

        String[] parts = source.split("-");

        if (parts.length != 2) {
            log.debug(String.format("Can't convert %s as partitions range, trying as id...", source));
            return Collections.emptySet();
        }

        short leftBound;
        short rightBound;
        try {
            leftBound = Short.parseShort(parts[0]);
            rightBound = Short.parseShort(parts[1]);
            if (rightBound >= PARTITIONS_AMOUNT || leftBound > rightBound || leftBound < 0) {
                log.debug(String.format("Can't convert %s as partitions range, trying as id...", source));
                return Collections.emptySet();
            }
            Set<Short> partitions = new HashSet<Short>();
            for (short i = leftBound; i <= rightBound; i++) {
                partitions.add(i);
            }
            log.debug(String.format("Partitions range %s converted into %s", source, partitions));
            return partitions;
        } catch (NumberFormatException e) {
            log.debug(String.format("Can't convert %s as partitions range, trying as id...", source));
            return Collections.emptySet();
        }
    }

    private static Set<Short> parsePartition(final String source) {
        try {
            short partition = Short.parseShort(source);
            if (partition < 0 || partition >= PARTITIONS_AMOUNT) {
                log.debug(String.format("Can't parse %s as partition, trying as id...", source));
                return Collections.emptySet();
            }
            log.debug(String.format("Partition %s converted into %d", source, partition));
            return Set.of(partition);
        } catch (NumberFormatException e) {
            log.debug(String.format("Can't parse %s as partition, trying as range...", source));
            return parsePartitionsRange(source);
        }
    }

    private static class EnabledIdsImpl implements EnabledIds {
        private final BitSet partitions;
        private final Set<Long> ids;
        private final short partitionsAmount;

        EnabledIdsImpl(final Set<Short> partitions, final short partitionsAmount, final Set<Long> ids) {
            this.partitions = new BitSet();
            partitions.forEach(p -> this.partitions.set(p));
            this.partitionsAmount = partitionsAmount;
            this.ids = ids;
        }

        @Override
        public boolean isEnabled(final long id) {
            short partition = ((short) (id % partitionsAmount));
            return partitions.get(partition) | ids.contains(id);
        }
    }
}
