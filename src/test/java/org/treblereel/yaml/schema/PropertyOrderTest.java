package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.ObjectType;
import org.treblereel.yaml.schema.model.SchemaDefinition;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests property order preservation in v2.1.
 *
 * BREAKING CHANGE from v2.0: Property iteration order changed from
 * alphabetical (TreeMap) to schema order (LinkedHashMap).
 *
 * Rationale: Code generators need schema-defined order for field declarations.
 */
class PropertyOrderTest {

    private final Parser parser = new Parser();

    @Test
    void properties_preserve_schema_order_not_alphabetical() {
        // v2.0 would fail this test (TreeMap = alphabetical)
        // v2.1 must pass (LinkedHashMap = schema order)

        String yaml = """
            type: object
            properties:
              zebra:
                type: string
              apple:
                type: string
              middle:
                type: string
            """;

        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = (ObjectType) schema.model();

        List<String> keys = new ArrayList<>(obj.properties().keySet());

        // Schema order, NOT alphabetical
        assertEquals(3, keys.size(), "Should have 3 properties");
        assertEquals("zebra", keys.get(0), "First property should be zebra");
        assertEquals("apple", keys.get(1), "Second property should be apple");
        assertEquals("middle", keys.get(2), "Third property should be middle");

        // v2.0 would return: ["apple", "middle", "zebra"]
    }

    @Test
    void properties_preserve_reverse_alphabetical_order() {
        // Demonstrates schema order preservation vs alphabetical sorting
        String yaml = """
            type: object
            properties:
              zulu:
                type: string
              yankee:
                type: string
              x-ray:
                type: string
            """;

        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = (ObjectType) schema.model();

        List<String> keys = new ArrayList<>(obj.properties().keySet());

        // Schema order (reverse alphabetical)
        assertEquals(3, keys.size(), "Should have 3 properties");
        assertEquals("zulu", keys.get(0));
        assertEquals("yankee", keys.get(1));
        assertEquals("x-ray", keys.get(2));
    }

    @Test
    void properties_preserve_single_property() {
        String yaml = """
            type: object
            properties:
              onlyOne:
                type: string
            """;

        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = (ObjectType) schema.model();

        List<String> keys = new ArrayList<>(obj.properties().keySet());

        assertEquals(1, keys.size());
        assertEquals("onlyOne", keys.get(0));
    }

    @Test
    void properties_preserve_order_with_multiple_types() {
        String yaml = """
            type: object
            properties:
              zebra:
                type: string
              alpha:
                type: integer
              bravo:
                type: boolean
              charlie:
                type: object
                properties:
                  nested:
                    type: string
            """;

        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = (ObjectType) schema.model();

        List<String> keys = new ArrayList<>(obj.properties().keySet());

        // Schema order preserved regardless of type variation
        assertEquals(4, keys.size());
        assertEquals("zebra", keys.get(0));
        assertEquals("alpha", keys.get(1));
        assertEquals("bravo", keys.get(2));
        assertEquals("charlie", keys.get(3));
    }

    @Test
    void properties_order_with_numeric_names() {
        String yaml = """
            type: object
            properties:
              "9":
                type: string
              "1":
                type: string
              "5":
                type: string
            """;

        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = (ObjectType) schema.model();

        List<String> keys = new ArrayList<>(obj.properties().keySet());

        // Schema order, not numeric sort
        assertEquals(3, keys.size());
        assertEquals("9", keys.get(0));
        assertEquals("1", keys.get(1));
        assertEquals("5", keys.get(2));
    }

    @Test
    void properties_order_with_special_characters() {
        String yaml = """
            type: object
            properties:
              _private:
                type: string
              publicField:
                type: string
              $special:
                type: string
            """;

        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = (ObjectType) schema.model();

        List<String> keys = new ArrayList<>(obj.properties().keySet());

        // Schema order preserved
        assertEquals(3, keys.size());
        assertEquals("_private", keys.get(0));
        assertEquals("publicField", keys.get(1));
        assertEquals("$special", keys.get(2));
    }

    @Test
    void properties_empty_object() {
        String yaml = """
            type: object
            """;

        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = (ObjectType) schema.model();

        assertTrue(obj.properties().isEmpty(),
                   "Object without properties should return empty map");
    }

    @Test
    void patternProperties_preserve_schema_order() {
        String yaml = """
            type: object
            patternProperties:
              "^z.*":
                type: string
              "^a.*":
                type: integer
              "^m.*":
                type: boolean
            """;

        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = (ObjectType) schema.model();

        List<String> patterns = new ArrayList<>(obj.patternProperties().keySet());

        // Schema order, NOT alphabetical
        assertEquals(3, patterns.size(), "Should have 3 patterns");
        assertEquals("^z.*", patterns.get(0), "First pattern should be ^z.*");
        assertEquals("^a.*", patterns.get(1), "Second pattern should be ^a.*");
        assertEquals("^m.*", patterns.get(2), "Third pattern should be ^m.*");
    }

    @Test
    void dependentSchemas_preserve_schema_order() {
        String yaml = """
            type: object
            properties:
              zip:
                type: string
              area:
                type: string
            dependentSchemas:
              zip:
                properties:
                  zipExt:
                    type: string
              area:
                properties:
                  areaCode:
                    type: string
            """;

        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = (ObjectType) schema.model();

        List<String> deps = new ArrayList<>(obj.dependentSchemas().keySet());

        // Schema order, NOT alphabetical
        assertEquals(2, deps.size(), "Should have 2 dependent schemas");
        assertEquals("zip", deps.get(0), "First should be zip");
        assertEquals("area", deps.get(1), "Second should be area");
    }

    @Test
    void dependentRequired_preserve_schema_order() {
        String yaml = """
            type: object
            dependentRequired:
              zip:
                - zipExt
              area:
                - areaCode
              name:
                - title
            """;

        SchemaDefinition schema = parser.parseYaml(yaml);
        ObjectType obj = (ObjectType) schema.model();

        List<String> deps = new ArrayList<>(obj.dependentRequired().keySet());

        // Schema order, NOT alphabetical
        assertEquals(3, deps.size(), "Should have 3 dependent required entries");
        assertEquals("zip", deps.get(0), "First should be zip");
        assertEquals("area", deps.get(1), "Second should be area");
        assertEquals("name", deps.get(2), "Third should be name");
    }
}
