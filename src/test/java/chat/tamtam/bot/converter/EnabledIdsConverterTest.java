package chat.tamtam.bot.converter;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EnabledIdsConverterTest {
    @Autowired
    private EnabledIdsConverter converter;
    private final List<Long> ids = List.of(155537636725L, 190435433004L, 175868018573L, 177949140156L);
    private final String idsAsString = "155537636725, 190435433004, 175868018573, 177949140156";

    // CHECKSTYLE_OFF: ALMOST_ALL
    @Test
    public void partitionsOnlyTest() {
        String parts = "0-12, 15-100";
        EnabledIds enabledIds = converter.convert(parts, getClass());
        for (long i = 0; i <= 12; i++) {
            Assertions.assertTrue(
                    enabledIds.isEnabled(i),
                    String.format("Id=%d should be enabled", i)
            );
        }
        for (long i = 15; i <= 100; i++) {
            Assertions.assertTrue(
                    enabledIds.isEnabled(i),
                    String.format("Id=%d should be enabled", i)
            );
        }
        for (long i = 101; i <= 255; i++) {
            Assertions.assertFalse(
                    enabledIds.isEnabled(i),
                    String.format("Id=%d should be disabled", i)
            );
        }
        Assertions.assertTrue(
                enabledIds.isEnabled(256),
                String.format("Id=%d should be enabled", 256)
        );

        String allParts = "0-255";
        enabledIds = converter.convert(allParts, getClass());
        for (long i = 0; i <= 1000; i++) {
            Assertions.assertTrue(
                    enabledIds.isEnabled(i),
                    String.format("Id=%d should be enabled", i)
            );
        }
    }

    @Test
    public void idsOnlyTest() {
        EnabledIds enabledIds = converter.convert(idsAsString, getClass());
        ids.forEach(id -> Assertions.assertTrue(
                enabledIds.isEnabled(id),
                String.format("Id=%d should be enabled", id)
        ));
        List<Long> disabledIds = List.of(555537636721L, 590435433001L, 575868018571L, 577949140151L);
        disabledIds.forEach(id -> Assertions.assertFalse(
                enabledIds.isEnabled(id),
                String.format("Id=%d should be disabled", id)
        ));
    }

    @Test
    public void emptyStringTest() {
        String nullString = null;
        EnabledIds enabledIdsWithNull = converter.convert(nullString, getClass());
        ids.forEach(id -> Assertions.assertFalse(
                enabledIdsWithNull.isEnabled(id),
                String.format("Id=%d should be disabled", id)
        ));
        String emptyString = "";
        EnabledIds enabledIdsWithEmpty = converter.convert(emptyString, getClass());
        ids.forEach(id -> Assertions.assertFalse(
                enabledIdsWithNull.isEnabled(id),
                String.format("Id=%d should be disabled", id)
        ));
    }

    @Test
    public void allPartitionsEnabledTest() {
        String parts = "0-255";
        EnabledIds enabledIds = converter.convert(parts, getClass());
        ids.forEach(id -> Assertions.assertTrue(
                enabledIds.isEnabled(id),
                String.format("Id=%d should be enabled", id)
        ));
    }

    @Test
    public void singlePartitionEnabledTest() {
        ids.stream().findFirst().ifPresentOrElse(id -> {
            String partition = String.valueOf(id % converter.PARTITIONS_AMOUNT);
            EnabledIds enabledId = converter.convert(partition, getClass());
            Assertions.assertTrue(enabledId.isEnabled(id), String.format("Id=%d should be enabled", id));
            Assertions.assertFalse(enabledId.isEnabled(id + 1), String.format("Id=%d should be disabled", id));
        }, IllegalStateException::new);
    }
    // CHECKSTYLE_ON: ALMOST_ALL

}
