package org.treblereel.yaml.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for v2.1 code generation API features.
 *
 * Tests all four features together in realistic OpenAPI schemas:
 * - Union types (nullable detection)
 * - Extension metadata (x- properties)
 * - Property order preservation (schema order)
 * - Discriminator support (polymorphism)
 */
class OpenAPICodeGenTest {

    @Test
    void should_parse_complete_openapi_schema_with_all_features() {
        // Parse Pet schema directly (OpenAPI schema format)
        String petYaml = """
            type: object
            x-java-interface: true
            properties:
              name:
                type: string
              age:
                type: [integer, "null"]
            discriminator:
              propertyName: petType
              mapping:
                cat: '#/components/schemas/Cat'
                dog: '#/components/schemas/Dog'
            """;

        Parser parser = new Parser();
        SchemaDefinition petSchema = parser.parseYaml(petYaml);
        ObjectType pet = petSchema.requireObject();

        // Feature 1: Extension metadata
        Map<String, JsonNode> petExtensions = pet.getExtensions();
        assertThat(petExtensions).containsKey("x-java-interface");
        assertThat(petExtensions.get("x-java-interface").asBoolean()).isTrue();

        // Feature 2: Property order
        List<String> petProps = new ArrayList<>(pet.properties().keySet());
        assertThat(petProps).containsExactly("name", "age");  // Schema order, not alphabetical

        // Feature 3: Union type (nullable)
        HasType ageType = pet.properties().get("age");
        assertThat(ageType).isInstanceOf(UnionType.class);
        UnionType ageUnion = (UnionType) ageType;
        assertThat(ageUnion.isNullable()).isTrue();
        assertThat(ageUnion.types()).containsExactly("integer", "null");

        // Feature 4: Discriminator
        Discriminator disc = pet.discriminator().orElseThrow();
        assertThat(disc.propertyName()).isEqualTo("petType");
        assertThat(disc.mapping()).containsEntry("cat", "#/components/schemas/Cat");
        assertThat(disc.mapping()).containsEntry("dog", "#/components/schemas/Dog");

        // Cat schema - allOf structure with extension metadata
        String catYaml = """
            allOf:
              - $ref: '#/components/schemas/Pet'
              - type: object
                x-class-name: CatImpl
                properties:
                  meow:
                    type: boolean
            """;

        SchemaDefinition catSchema = parser.parseYaml(catYaml);
        AllOfType catAllOf = catSchema.modelAsAllOf().orElseThrow();

        // Verify Cat's allOf structure
        assertThat(catAllOf.getAllOf()).hasSize(2);

        // Second element should have x-class-name extension
        HasType catInlineType = catAllOf.getAllOf().get(1);
        assertThat(catInlineType).isInstanceOf(ObjectType.class);
        ObjectType catInline = (ObjectType) catInlineType;
        Map<String, JsonNode> catExtensions = catInline.getExtensions();
        assertThat(catExtensions).containsKey("x-class-name");
        assertThat(catExtensions.get("x-class-name").asText()).isEqualTo("CatImpl");
    }
}
