package org.treblereel.yaml.schema.model;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.Parser;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;

class PropertyNavigatorTest {

    private final Parser parser = new Parser();

    @Test
    void empty_shouldReturnNavigatorWithNoValue() {
        PropertyNavigator nav = PropertyNavigator.empty();

        assertThat(nav.exists()).isFalse();
        assertThat(nav.get()).isEmpty();
    }

    @Test
    void empty_shouldReturnSameInstance() {
        PropertyNavigator nav1 = PropertyNavigator.empty();
        PropertyNavigator nav2 = PropertyNavigator.empty();

        assertThat(nav1).isSameAs(nav2);
    }

    @Test
    void empty_property_shouldReturnEmpty() {
        PropertyNavigator nav = PropertyNavigator.empty();

        PropertyNavigator result = nav.property("anything");

        assertThat(result.exists()).isFalse();
    }

    @Test
    void empty_asObject_shouldReturnEmpty() {
        PropertyNavigator nav = PropertyNavigator.empty();

        Optional<ObjectType> result = nav.asObject();

        assertThat(result).isEmpty();
    }
}
