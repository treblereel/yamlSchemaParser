package org.treblereel.yaml.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
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
}
