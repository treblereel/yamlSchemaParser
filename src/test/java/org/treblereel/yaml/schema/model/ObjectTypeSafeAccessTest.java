package org.treblereel.yaml.schema.model;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.Parser;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

class ObjectTypeSafeAccessTest {

    private final Parser parser = new Parser();

    @Test
    void properties_shouldReturnEmptyMap_whenNoProperties() {
        SchemaDefinition schema = parser.parse("src/test/resources/empty-object.schema.yaml");
        ObjectType obj = schema.requireObject();

        Map<String, HasType> result = obj.properties();

        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void properties_shouldReturnMap_whenPropertiesExist() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");
        ObjectType obj = schema.requireObject();

        Map<String, HasType> result = obj.properties();

        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result).containsKey("firstName");
    }

    @Test
    void hasProperty_shouldReturnTrue_whenPropertyExists() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");
        ObjectType obj = schema.requireObject();

        boolean result = obj.hasProperty("firstName");

        assertThat(result).isTrue();
    }

    @Test
    void hasProperty_shouldReturnFalse_whenPropertyDoesNotExist() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");
        ObjectType obj = schema.requireObject();

        boolean result = obj.hasProperty("nonexistent");

        assertThat(result).isFalse();
    }

    @Test
    void getProperty_shouldReturnType_whenPropertyExists() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");
        ObjectType obj = schema.requireObject();

        var result = obj.getProperty("firstName");

        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(StringType.class);
    }

    @Test
    void getProperty_shouldReturnEmpty_whenPropertyDoesNotExist() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");
        ObjectType obj = schema.requireObject();

        var result = obj.getProperty("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void getPropertyAsString_shouldReturnStringType_whenPropertyIsString() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");
        ObjectType obj = schema.requireObject();

        var result = obj.getPropertyAsString("firstName");

        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(StringType.class);
    }

    @Test
    void getPropertyAsString_shouldReturnEmpty_whenPropertyIsNotString() {
        SchemaDefinition schema = parser.parse("src/test/resources/mixed-types-props.schema.yaml");
        ObjectType obj = schema.requireObject();

        var result = obj.getPropertyAsString("age"); // age is integer

        assertThat(result).isEmpty();
    }

    @Test
    void getPropertyAsObject_shouldReturnObjectType_whenPropertyIsObject() {
        SchemaDefinition schema = parser.parse("src/test/resources/nested-object.schema.yaml");
        ObjectType root = schema.requireObject();

        var result = root.getPropertyAsObject("user");

        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(ObjectType.class);
    }
}
