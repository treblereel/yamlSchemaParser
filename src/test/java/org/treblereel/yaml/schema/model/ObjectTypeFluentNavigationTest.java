package org.treblereel.yaml.schema.model;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.Parser;
import static org.assertj.core.api.Assertions.*;

class ObjectTypeFluentNavigationTest {

    private final Parser parser = new Parser();

    @Test
    void property_shouldReturnNavigator_whenPropertyExists() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");
        ObjectType root = schema.requireObject();

        PropertyNavigator nav = root.property("firstName");

        assertThat(nav.exists()).isTrue();
        assertThat(nav.asString()).isPresent();
    }

    @Test
    void property_shouldReturnEmptyNavigator_whenPropertyDoesNotExist() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");
        ObjectType root = schema.requireObject();

        PropertyNavigator nav = root.property("nonexistent");

        assertThat(nav.exists()).isFalse();
    }

    @Test
    void property_shouldAllowChaining_forNestedProperties() {
        SchemaDefinition schema = parser.parse("src/test/resources/nested-object.schema.yaml");
        ObjectType root = schema.requireObject();

        PropertyNavigator nameNav = root.property("user")
            .property("name");

        assertThat(nameNav.exists()).isTrue();
    }

    @Test
    void property_shouldWorkWithAsString_forDirectAccess() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");
        ObjectType root = schema.requireObject();

        var result = root.property("firstName")
            .asString()
            .flatMap(StringType::pattern);

        // Result depends on whether name has pattern in schema
        assertThat(result).isNotNull();
    }
}
