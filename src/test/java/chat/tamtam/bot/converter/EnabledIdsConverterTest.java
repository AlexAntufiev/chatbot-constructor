package chat.tamtam.bot.converter;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import chat.tamtam.bot.RunnableTestContext;

public class EnabledIdsConverterTest extends RunnableTestContext {
    @Autowired
    private EnabledIdsConverter converter;

    @Test
    public void partitionsOnlyTest() {
        String parts = "0-12, 15-100";
        EnabledIds enabledIds = converter.convert(parts);
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
        enabledIds = converter.convert(allParts);
        for (long i = 0; i <= 1000; i++) {
            Assertions.assertTrue(
                    enabledIds.isEnabled(i),
                    String.format("Id=%d should be enabled", i)
            );
        }
    }

    @Test
    public void idsOnlyTest() {
        List<Long> ids = List.of(555537636725L, 590435433004L, 575868018573L, 577949140156L);
        String property = "555537636725, 590435433004, 575868018573, 577949140156";
        EnabledIds enabledIds = converter.convert(property);
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
        List<Long> ids = List.of(555537636725L, 590435433004L, 575868018573L, 577949140156L);
        String nullString = null;
        EnabledIds enabledIdsWithNull = converter.convert(nullString);
        ids.forEach(id -> Assertions.assertFalse(
                enabledIdsWithNull.isEnabled(id),
                String.format("Id=%d should be disabled", id)
        ));
        String emptyString = "";
        EnabledIds enabledIdsWithEmpty = converter.convert(emptyString);
        ids.forEach(id -> Assertions.assertFalse(
                enabledIdsWithNull.isEnabled(id),
                String.format("Id=%d should be disabled", id)
        ));
    }

    @Test
    public void combinedTest() {
        String parts = "0-255";
        EnabledIds enabledIds = converter.convert(parts);
        List<Long> ids = List.of(555537636725L, 590435433004L, 575868018573L, 577949140156L);
        ids.forEach(id -> Assertions.assertTrue(
                enabledIds.isEnabled(id),
                String.format("Id=%d should be enabled", id)
        ));

        // single partition enabled
        ids.stream().findFirst().ifPresentOrElse(id -> {
            String partition = String.valueOf(id % converter.PARTITIONS_AMOUNT);
            EnabledIds enabledId = converter.convert(partition);
            Assertions.assertTrue(enabledId.isEnabled(id), String.format("Id=%d should be enabled", id));
            Assertions.assertFalse(enabledId.isEnabled(id + 1), String.format("Id=%d should be disabled", id));
        }, IllegalStateException::new);
    }
}
