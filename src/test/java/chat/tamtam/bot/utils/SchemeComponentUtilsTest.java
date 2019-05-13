package chat.tamtam.bot.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import chat.tamtam.bot.domain.builder.component.ComponentUpdate;
import chat.tamtam.bot.domain.builder.component.SchemeComponent;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class SchemeComponentUtilsTest {
    // CHECKSTYLE_OFF: ALMOST_ALL

    @Test
    public void nonCyclicGraphTest() {
        List<ComponentUpdate> list = new ArrayList<>();
        list.add(getComponent(0, 1, 1L));
        list.add(getComponent(1, 1, 2L));
        list.add(getComponent(2, 0, 3L));
        list.add(getComponent(4, 1, 5L));
        list.add(getComponent(5, 1, null));
        list.add(getComponent(6, 1, 7L));
        list.add(getComponent(7, 1, 8L));
        list.add(getComponent(8, 2, 9L));
        Assertions.assertTrue(
                SchemeComponentUtils.isGraphIsNonCyclic(list),
                "Graph is cyclic, but should be non"
        );

        list = new ArrayList<>();
        list.add(getComponent(0, 1, 1L));
        list.add(getComponent(1, 1, 2L));
        list.add(getComponent(2, 0, 0L));
        Assertions.assertTrue(
                SchemeComponentUtils.isGraphIsNonCyclic(list),
                "Graph is cyclic, but should be non"
        );

        list = new ArrayList<>();
        list.add(getComponent(0, 1, 1L));
        list.add(getComponent(1, 1, 2L));
        list.add(getComponent(2, 1, 4L));
        list.add(getComponent(4, 2, 0L));
        Assertions.assertTrue(
                SchemeComponentUtils.isGraphIsNonCyclic(list),
                "Graph is cyclic, but should be non"
        );
    }

    @Test
    public void cyclicGraphTest() {
        List<ComponentUpdate> list = new ArrayList<>();
        list.add(getComponent(0, 1, 1L));
        list.add(getComponent(1, 1, 2L));
        list.add(getComponent(2, 1, 0L));
        list.add(getComponent(4, 1, 5L));
        list.add(getComponent(5, 1, null));
        list.add(getComponent(6, 1, 7L));
        list.add(getComponent(7, 1, 8L));
        list.add(getComponent(8, 2, 9L));
        Assertions.assertFalse(
                SchemeComponentUtils.isGraphIsNonCyclic(list),
                "Graph is non cyclic, but should be cyclic"
        );

        list = new ArrayList<>();
        list.add(getComponent(0, 1, 0L));
        Assertions.assertFalse(
                SchemeComponentUtils.isGraphIsNonCyclic(list),
                "Graph is non cyclic, but should be cyclic"
        );

        list = new ArrayList<>();
        list.add(getComponent(0, 1, 1L));
        list.add(getComponent(1, 1, 4L));
        list.add(getComponent(4, 1, 5L));
        list.add(getComponent(5, 1, 6L));
        list.add(getComponent(6, 1, 7L));
        list.add(getComponent(7, 1, 8L));
        list.add(getComponent(8, 1, 0L));
        Assertions.assertFalse(
                SchemeComponentUtils.isGraphIsNonCyclic(list),
                "Graph is non cyclic, but should be cyclic"
        );
    }

    @Test
    public void singleComponentGraphTest() {
        List<ComponentUpdate> list = Collections.singletonList(getComponent(0, 1, null));
        Assertions.assertTrue(
                SchemeComponentUtils.isGraphIsNonCyclic(list),
                "Graph is cyclic, but should be non"
        );
    }

    public void emptyComponentsListTest() {
        Assertions.assertTrue(
                SchemeComponentUtils.isGraphIsNonCyclic(Collections.emptyList()),
                "Graph is cyclic, but should be non"
        );
    }

    private ComponentUpdate getComponent(final long id, final int type, final Long nextState) {
        SchemeComponent component = new SchemeComponent();
        component.setId(id);
        component.setType(((byte) type));
        component.setNextState(nextState);
        return new ComponentUpdate(component, null, null, null);
    }

    // CHECKSTYLE_ON: ALMOST_ALL
}
