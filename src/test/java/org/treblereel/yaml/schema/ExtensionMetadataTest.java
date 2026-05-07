package org.treblereel.yaml.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.ArrayType;
import org.treblereel.yaml.schema.model.HasType;
import org.treblereel.yaml.schema.model.IntegerType;
import org.treblereel.yaml.schema.model.ObjectType;
import org.treblereel.yaml.schema.model.SchemaDefinition;
import org.treblereel.yaml.schema.model.StringType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExtensionMetadataTest {

    @Test
    void should_extract_x_properties_from_ObjectType() {
        String yaml = """
            type: object
            x-java-type: com.example.CustomType
            x-class-name: MyClass
            properties:
              name:
                type: string
            """;

        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = schema.requireObject();

        Map<String, JsonNode> extensions = obj.getExtensions();

        assertThat(extensions).hasSize(2);
        assertThat(extensions).containsKey("x-java-type");
        assertThat(extensions).containsKey("x-class-name");
        assertThat(extensions.get("x-java-type").asText()).isEqualTo("com.example.CustomType");
        assertThat(extensions.get("x-class-name").asText()).isEqualTo("MyClass");
    }

    @Test
    void should_return_empty_extensions_for_NullType() {
        String yaml = """
            type: "null"
            """;

        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        HasType nullType = schema.model();

        assertThat(nullType.getExtensions()).isEmpty();
        assertThat(nullType.getExtension("x-anything")).isEmpty();
    }

    @Test
    void should_delegate_extensions_from_RefType_to_resolved_type() {
        String yaml = """
            $defs:
              MyType:
                type: object
                x-java-class: com.example.MyType
                properties:
                  name:
                    type: string
            type: object
            properties:
              ref:
                $ref: '#/$defs/MyType'
            """;

        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = schema.requireObject();

        HasType refType = obj.properties().get("ref");
        assertThat(refType).isInstanceOf(org.treblereel.yaml.schema.model.RefType.class);

        Map<String, JsonNode> extensions = refType.getExtensions();
        assertThat(extensions).containsKey("x-java-class");
        assertThat(extensions.get("x-java-class").asText()).isEqualTo("com.example.MyType");
    }

    @Test
    void should_extract_extensions_from_StringType() {
        String yaml = """
            type: string
            x-format-hint: email-relaxed
            pattern: "^.+@.+$"
            """;

        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        StringType str = schema.requireString();

        assertThat(str.getExtensions()).containsKey("x-format-hint");
        assertThat(str.getExtension("x-format-hint").orElseThrow().asText())
            .isEqualTo("email-relaxed");
    }

    @Test
    void should_extract_extensions_from_IntegerType() {
        String yaml = """
            type: integer
            x-unit: milliseconds
            minimum: 0
            """;

        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        IntegerType num = schema.requireInteger();

        assertThat(num.getExtensions()).containsKey("x-unit");
    }

    @Test
    void should_extract_extensions_from_ArrayType() {
        String yaml = """
            type: array
            x-immutable: true
            items:
              type: string
            """;

        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        ArrayType arr = schema.requireArray();

        assertThat(arr.getExtensions()).containsKey("x-immutable");
    }

    @Test
    void should_return_empty_when_no_extensions() {
        String yaml = """
            type: object
            properties:
              name:
                type: string
            """;

        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = schema.requireObject();

        assertThat(obj.getExtensions()).isEmpty();
    }

    @Test
    void should_return_empty_for_non_x_prefixed_key() {
        String yaml = """
            type: object
            x-valid: value
            title: "Not an extension"
            """;

        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = schema.requireObject();

        assertThat(obj.getExtension("title")).isEmpty();
        assertThat(obj.getExtension("x-valid")).isPresent();
    }

    @Test
    void should_handle_complex_extension_values() {
        String yaml = """
            type: object
            x-metadata:
              nested:
                value: 123
              array: [1, 2, 3]
            properties:
              name:
                type: string
            """;

        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = schema.requireObject();

        JsonNode metadata = obj.getExtension("x-metadata").orElseThrow();
        assertThat(metadata.isObject()).isTrue();
        assertThat(metadata.get("nested").get("value").asInt()).isEqualTo(123);
    }

    @Test
    void should_include_field_named_exactly_x_dash() {
        String yaml = """
            type: object
            x-: edgeCase
            properties:
              name:
                type: string
            """;

        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = schema.requireObject();

        assertThat(obj.getExtensions()).containsKey("x-");
    }
}
