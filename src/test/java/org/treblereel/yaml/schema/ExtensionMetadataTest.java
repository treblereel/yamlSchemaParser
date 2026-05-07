package org.treblereel.yaml.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.HasType;
import org.treblereel.yaml.schema.model.ObjectType;
import org.treblereel.yaml.schema.model.SchemaDefinition;

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
}
