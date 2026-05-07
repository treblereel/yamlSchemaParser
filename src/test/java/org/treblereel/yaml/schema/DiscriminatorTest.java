package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.Discriminator;
import org.treblereel.yaml.schema.model.ObjectType;
import org.treblereel.yaml.schema.model.SchemaDefinition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscriminatorTest {

    @Test
    void should_create_discriminator_with_propertyName_and_mapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("cat", "#/components/schemas/Cat");
        mapping.put("dog", "#/components/schemas/Dog");

        Discriminator disc = new Discriminator("petType", mapping);

        assertThat(disc.propertyName()).isEqualTo("petType");
        assertThat(disc.mapping()).containsEntry("cat", "#/components/schemas/Cat");
        assertThat(disc.mapping()).containsEntry("dog", "#/components/schemas/Dog");
    }

    @Test
    void should_throw_when_propertyName_is_null() {
        assertThatThrownBy(() -> new Discriminator(null, Map.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Discriminator must have propertyName field");
    }

    @Test
    void should_throw_when_propertyName_is_blank() {
        assertThatThrownBy(() -> new Discriminator("", Map.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Discriminator must have propertyName field");
        
        assertThatThrownBy(() -> new Discriminator("   ", Map.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Discriminator must have propertyName field");
    }

    @Test
    void should_accept_empty_mapping() {
        // mapping is optional per OpenAPI spec
        Discriminator disc = new Discriminator("petType", Map.of());
        
        assertThat(disc.propertyName()).isEqualTo("petType");
        assertThat(disc.mapping()).isEmpty();
    }

    @Test
    void should_accept_null_mapping_as_empty() {
        Discriminator disc = new Discriminator("petType", null);
        
        assertThat(disc.mapping()).isEmpty();
        assertThat(disc.mapping()).isEqualTo(Map.of());
    }

    @Test
    void should_preserve_mapping_order() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("zebra", "#/components/schemas/Zebra");
        mapping.put("apple", "#/components/schemas/Apple");
        mapping.put("middle", "#/components/schemas/Middle");
        
        Discriminator disc = new Discriminator("type", mapping);
        
        List<String> keys = new ArrayList<>(disc.mapping().keySet());
        assertThat(keys).containsExactly("zebra", "apple", "middle");
    }

    @Test
    void should_parse_discriminator_from_ObjectType() {
        String yaml = """
            type: object
            oneOf:
              - $ref: '#/components/schemas/Cat'
              - $ref: '#/components/schemas/Dog'
            discriminator:
              propertyName: petType
              mapping:
                cat: '#/components/schemas/Cat'
                dog: '#/components/schemas/Dog'
            """;
        
        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = schema.requireObject();
        
        Optional<Discriminator> disc = obj.discriminator();
        
        assertThat(disc).isPresent();
        assertThat(disc.get().propertyName()).isEqualTo("petType");
        assertThat(disc.get().mapping()).hasSize(2);
        assertThat(disc.get().mapping().get("cat")).isEqualTo("#/components/schemas/Cat");
        assertThat(disc.get().mapping().get("dog")).isEqualTo("#/components/schemas/Dog");
    }

    @Test
    void should_return_empty_when_no_discriminator() {
        String yaml = """
            type: object
            properties:
              name:
                type: string
            """;
        
        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = schema.requireObject();
        
        assertThat(obj.discriminator()).isEmpty();
    }

    @Test
    void should_parse_discriminator_without_mapping() {
        String yaml = """
            type: object
            discriminator:
              propertyName: type
            """;
        
        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = schema.requireObject();
        
        Optional<Discriminator> disc = obj.discriminator();
        
        assertThat(disc).isPresent();
        assertThat(disc.get().propertyName()).isEqualTo("type");
        assertThat(disc.get().mapping()).isEmpty();
    }

    @Test
    void should_parse_discriminator_with_empty_mapping() {
        String yaml = """
            type: object
            discriminator:
              propertyName: type
              mapping: {}
            """;
        
        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = schema.requireObject();
        
        assertThat(obj.discriminator().orElseThrow().mapping()).isEmpty();
    }

    @Test
    void should_accept_discriminator_without_oneOf() {
        // Parser is permissive - doesn't validate schema correctness
        String yaml = """
            type: object
            discriminator:
              propertyName: type
            properties:
              name:
                type: string
            """;
        
        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = schema.requireObject();
        
        assertThat(obj.discriminator()).isPresent();
    }

    @Test
    void should_store_non_ref_mapping_values() {
        // Parser doesn't validate - stores as-is
        String yaml = """
            type: object
            discriminator:
              propertyName: type
              mapping:
                cat: Cat
                dog: Dog
            """;
        
        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = schema.requireObject();
        
        Discriminator disc = obj.discriminator().orElseThrow();
        assertThat(disc.mapping().get("cat")).isEqualTo("Cat");
        assertThat(disc.mapping().get("dog")).isEqualTo("Dog");
    }

    @Test
    void should_throw_when_discriminator_has_no_propertyName() {
        String yaml = """
            type: object
            discriminator:
              mapping:
                cat: '#/components/schemas/Cat'
            """;
        
        Parser parser = new Parser();
        
        assertThatThrownBy(() -> parser.parseYaml(yaml).requireObject())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Discriminator must have propertyName field");
    }
}
