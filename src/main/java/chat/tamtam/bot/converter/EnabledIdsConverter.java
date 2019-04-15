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

    public EnabledIds convert(final String source) {
        if (StringUtils.isEmpty(source)) {
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
            partitions.addAll(parsePartition(arg));
            ids.addAll(parseId(arg));
        }
        return new EnabledIdsImpl(partitions, PARTITIONS_AMOUNT, ids);
    }

    private static Set<Long> parseId(final String source) {
        try {
            long id = Long.parseLong(source);
            if (id < PARTITIONS_AMOUNT) {
                return Collections.emptySet();
            }
            return Set.of(id);
        } catch (NumberFormatException e) {
            return Collections.emptySet();
        }
    }

    private static Set<Short> parsePartitionsRange(final String source) {
        if (source == null) {
            return Collections.emptySet();
        }

        String[] parts = source.split("-");

        if (parts.length != 2) {
            return Collections.emptySet();
        }

        short leftBound;
        short rightBound;
        try {
            leftBound = Short.parseShort(parts[0]);
            rightBound = Short.parseShort(parts[1]);
            if (rightBound >= PARTITIONS_AMOUNT || leftBound > rightBound || leftBound < 0) {
                return Collections.emptySet();
            }
            Set<Short> partitions = new HashSet<Short>();
            for (short i = leftBound; i <= rightBound; i++) {
                partitions.add(i);
            }
            return partitions;
        } catch (NumberFormatException e) {
            log.debug("Can't convert partitions range", e);
            return Collections.emptySet();
        }
    }

    private static Set<Short> parsePartition(final String source) {
        try {
            short partition = Short.parseShort(source);
            if (partition < 0 || partition >= PARTITIONS_AMOUNT) {
                return Collections.emptySet();
            }
            return Set.of(partition);
        } catch (NumberFormatException e) {
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
