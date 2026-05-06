package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for prefixItems keyword (JSON Schema Draft 2020-12).
 * prefixItems is a modern alternative to array-based items for tuple validation.
 */
public class PrefixItemsTest {

    private final Parser parser = new Parser();

    // ============== Basic prefixItems Tests ==============

    @Test
    void testPrefixItemsPresent() {
        SchemaDefinition schema = parser.parse("src/test/resources/prefix-items-simple.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertTrue(arrayType.prefixItems().isPresent(),
                   "prefixItems should be present");
    }

    @Test
    void testPrefixItemsArrayLength() {
        SchemaDefinition schema = parser.parse("src/test/resources/prefix-items-simple.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        ArrayItemType[] items = arrayType.prefixItems().get();
        assertEquals(3, items.length, "Should have 3 prefix items");
    }

    @Test
    void testPrefixItemsFirstItemType() {
        SchemaDefinition schema = parser.parse("src/test/resources/prefix-items-simple.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        ArrayItemType[] items = arrayType.prefixItems().get();
        HasType firstItem = items[0].getType();

        assertInstanceOf(StringType.class, firstItem,
                         "First item should be StringType");
    }

    @Test
    void testPrefixItemsSecondItemType() {
        SchemaDefinition schema = parser.parse("src/test/resources/prefix-items-simple.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        ArrayItemType[] items = arrayType.prefixItems().get();
        HasType secondItem = items[1].getType();

        assertInstanceOf(NumberType.class, secondItem,
                         "Second item should be NumberType");
    }

    @Test
    void testPrefixItemsThirdItemType() {
        SchemaDefinition schema = parser.parse("src/test/resources/prefix-items-simple.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        ArrayItemType[] items = arrayType.prefixItems().get();
        HasType thirdItem = items[2].getType();

        assertInstanceOf(BooleanType.class, thirdItem,
                         "Third item should be BooleanType");
    }

    // ============== Complex prefixItems Tests ==============

    @Test
    void testComplexPrefixItemsLength() {
        SchemaDefinition schema = parser.parse("src/test/resources/prefix-items-complex.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        ArrayItemType[] items = arrayType.prefixItems().get();
        assertEquals(3, items.length, "Should have 3 prefix items");
    }

    @Test
    void testComplexPrefixItemsStringConstraints() {
        SchemaDefinition schema = parser.parse("src/test/resources/prefix-items-complex.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        ArrayItemType[] items = arrayType.prefixItems().get();
        StringType stringType = (StringType) items[0].getType();

        assertTrue(stringType.minLength().isPresent(),
                   "String should have minLength");
        assertEquals(1, stringType.minLength().get(),
                     "String minLength should be 1");
    }

    @Test
    void testComplexPrefixItemsIntegerConstraints() {
        SchemaDefinition schema = parser.parse("src/test/resources/prefix-items-complex.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        ArrayItemType[] items = arrayType.prefixItems().get();
        IntegerType integerType = (IntegerType) items[1].getType();

        assertTrue(integerType.minimum().isPresent(),
                   "Integer should have minimum");
        assertEquals(0, integerType.minimum().get(),
                     "Integer minimum should be 0");
    }

    @Test
    void testComplexPrefixItemsObjectProperties() {
        SchemaDefinition schema = parser.parse("src/test/resources/prefix-items-complex.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        ArrayItemType[] items = arrayType.prefixItems().get();
        ObjectType objectType = (ObjectType) items[2].getType();

        assertTrue(objectType.properties().isPresent(),
                   "Object should have properties");
        assertTrue(objectType.properties().get().containsKey("enabled"),
                   "Object should have enabled property");
    }

    @Test
    void testComplexPrefixItemsObjectRequired() {
        SchemaDefinition schema = parser.parse("src/test/resources/prefix-items-complex.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        ArrayItemType[] items = arrayType.prefixItems().get();
        ObjectType objectType = (ObjectType) items[2].getType();

        assertTrue(objectType.required().isPresent(),
                   "Object should have required fields");
        assertTrue(objectType.required().get().contains("enabled"),
                   "enabled should be required");
    }

    @Test
    void testComplexArrayConstraints() {
        SchemaDefinition schema = parser.parse("src/test/resources/prefix-items-complex.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertEquals(3, arrayType.minItems().get(),
                     "minItems should be 3");
        assertEquals(5, arrayType.maxItems().get(),
                     "maxItems should be 5");
    }

    // ============== Mixed prefixItems and items Tests ==============

    @Test
    void testMixedPrefixItemsPresent() {
        SchemaDefinition schema = parser.parse("src/test/resources/prefix-items-mixed.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertTrue(arrayType.prefixItems().isPresent(),
                   "prefixItems should be present");
    }

    @Test
    void testMixedPrefixItemsLength() {
        SchemaDefinition schema = parser.parse("src/test/resources/prefix-items-mixed.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        ArrayItemType[] items = arrayType.prefixItems().get();
        assertEquals(2, items.length, "Should have 2 prefix items");
    }

    @Test
    void testMixedPrefixItemsTypes() {
        SchemaDefinition schema = parser.parse("src/test/resources/prefix-items-mixed.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        ArrayItemType[] items = arrayType.prefixItems().get();

        assertInstanceOf(StringType.class, items[0].getType(),
                         "First prefix item should be StringType");
        assertInstanceOf(NumberType.class, items[1].getType(),
                         "Second prefix item should be NumberType");
    }

    @Test
    void testMixedItemsDefinition() {
        SchemaDefinition schema = parser.parse("src/test/resources/prefix-items-mixed.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        // getItems() should still work for additional items
        ArrayItemType[] regularItems = arrayType.getItems();
        assertNotNull(regularItems, "Regular items should be present");
    }

    // ============== Edge Cases ==============

    @Test
    void testSchemaWithoutPrefixItems() {
        SchemaDefinition schema = parser.parse("src/test/resources/array-only-minItems.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertFalse(arrayType.prefixItems().isPresent(),
                    "Schema without prefixItems should return empty Optional");
    }

    @Test
    void testPrefixItemsVsOldStyleItems() {
        // Test that prefixItems is distinct from old-style array items
        SchemaDefinition prefixSchema = parser.parse("src/test/resources/prefix-items-simple.schema.yaml");
        ArrayType prefixArray = (ArrayType) prefixSchema.model();

        // Schema uses prefixItems, not items array
        assertTrue(prefixArray.prefixItems().isPresent(),
                   "prefixItems should be present");
    }

    // ============== Workflow.yaml Integration Test ==============

    @Test
    void testWorkflowSchemaPrefixItems() {
        SchemaDefinition schema = parser.parse("src/test/resources/workflow.yaml");
        assertNotNull(schema, "workflow.yaml should parse successfully");

        // workflow.yaml may or may not use prefixItems
        // This test verifies the parser can handle complex real-world schemas
        ObjectType model = (ObjectType) schema.model();
        assertNotNull(model, "workflow model should be created");
    }
}
