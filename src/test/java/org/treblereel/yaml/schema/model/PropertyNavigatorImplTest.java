package org.treblereel.yaml.schema.model;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.Parser;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;

class PropertyNavigatorImplTest {

    private final Parser parser = new Parser();

    @Test
    void get_shouldReturnPresentType_whenCreatedWithValue() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");
        ObjectType root = schema.requireObject();
        HasType nameType = root.properties().get("firstName");

        PropertyNavigator nav = new PropertyNavigatorImpl(Optional.of(nameType), false);

        assertThat(nav.get()).isPresent();
        assertThat(nav.get().get()).isSameAs(nameType);
    }

    @Test
    void exists_shouldReturnTrue_whenValuePresent() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");
        ObjectType root = schema.requireObject();
        HasType nameType = root.properties().get("firstName");

        PropertyNavigator nav = new PropertyNavigatorImpl(Optional.of(nameType), false);

        assertThat(nav.exists()).isTrue();
    }

    @Test
    void asString_shouldReturnStringType_whenTypeIsString() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");
        ObjectType root = schema.requireObject();
        HasType nameType = root.properties().get("firstName");

        PropertyNavigator nav = new PropertyNavigatorImpl(Optional.of(nameType), false);

        Optional<StringType> result = nav.asString();

        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(StringType.class);
    }

    @Test
    void asObject_shouldReturnEmpty_whenTypeIsNotObject() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");
        ObjectType root = schema.requireObject();
        HasType nameType = root.properties().get("firstName");

        PropertyNavigator nav = new PropertyNavigatorImpl(Optional.of(nameType), false);

        Optional<ObjectType> result = nav.asObject();

        assertThat(result).isEmpty();
    }

    @Test
    void property_shouldNavigateToNestedProperty_whenTypeIsObject() {
        SchemaDefinition schema = parser.parse("src/test/resources/nested-object.schema.yaml");
        ObjectType root = schema.requireObject();

        PropertyNavigator nav = new PropertyNavigatorImpl(Optional.of(root), false);
        PropertyNavigator userNav = nav.property("user");

        assertThat(userNav.exists()).isTrue();
        assertThat(userNav.asObject()).isPresent();
    }

    @Test
    void property_shouldReturnEmpty_whenTypeIsNotObject() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");
        ObjectType root = schema.requireObject();
        HasType nameType = root.properties().get("firstName");

        PropertyNavigator nav = new PropertyNavigatorImpl(Optional.of(nameType), false);
        PropertyNavigator result = nav.property("anything");

        assertThat(result.exists()).isFalse();
    }

    @Test
    void item_shouldNavigateToArrayItem_whenTypeIsArray() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-array.schema.yaml");
        ArrayType arr = schema.requireArray();

        PropertyNavigator nav = new PropertyNavigatorImpl(Optional.of(arr), false);
        PropertyNavigator itemNav = nav.item(0);

        assertThat(itemNav.exists()).isTrue();
    }

    @Test
    void item_shouldReturnEmpty_whenIndexOutOfBounds() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-array.schema.yaml");
        ArrayType arr = schema.requireArray();

        PropertyNavigator nav = new PropertyNavigatorImpl(Optional.of(arr), false);
        PropertyNavigator itemNav = nav.item(999);

        assertThat(itemNav.exists()).isFalse();
    }

    @Test
    void isResolved_shouldReturnFalse_whenCreatedUnresolved() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");
        ObjectType root = schema.requireObject();

        PropertyNavigator nav = new PropertyNavigatorImpl(Optional.of(root), false);

        assertThat(nav.isResolved()).isFalse();
    }

    @Test
    void isResolved_shouldReturnTrue_whenCreatedResolved() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");
        ObjectType root = schema.requireObject();

        PropertyNavigator nav = new PropertyNavigatorImpl(Optional.of(root), true);

        assertThat(nav.isResolved()).isTrue();
    }
}
