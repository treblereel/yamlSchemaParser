package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for unevaluatedItems keyword (JSON Schema Draft 2019-09+).
 * unevaluatedItems controls whether array elements not covered by items/prefixItems are allowed.
 */
public class UnevaluatedItemsTest {

    private final Parser parser = new Parser();

    // ============== Basic unevaluatedItems Tests ==============

    @Test
    void testUnevaluatedItemsFalsePresent() {
        SchemaDefinition schema = parser.parse("src/test/resources/unevaluated-items-false.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertTrue(arrayType.unevaluatedItems().isPresent(),
                   "unevaluatedItems should be present");
        assertFalse(arrayType.unevaluatedItems().get(),
                    "unevaluatedItems should be false");
    }

    @Test
    void testUnevaluatedItemsTruePresent() {
        SchemaDefinition schema = parser.parse("src/test/resources/unevaluated-items-true.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertTrue(arrayType.unevaluatedItems().isPresent(),
                   "unevaluatedItems should be present");
        assertTrue(arrayType.unevaluatedItems().get(),
                   "unevaluatedItems should be true");
    }

    @Test
    void testUnevaluatedItemsWithPrefixItems() {
        SchemaDefinition schema = parser.parse("src/test/resources/unevaluated-items-false.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertTrue(arrayType.prefixItems().isPresent(),
                   "prefixItems should be present");
        assertEquals(2, arrayType.prefixItems().get().length,
                     "Should have 2 prefix items");

        assertTrue(arrayType.unevaluatedItems().isPresent(),
                   "unevaluatedItems should be present");
        assertFalse(arrayType.unevaluatedItems().get(),
                    "unevaluatedItems should be false");
    }

    // ============== Combined with items Tests ==============

    @Test
    void testUnevaluatedItemsWithBothPrefixAndItems() {
        SchemaDefinition schema = parser.parse("src/test/resources/unevaluated-items-with-items.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertTrue(arrayType.prefixItems().isPresent(),
                   "prefixItems should be present");
        assertEquals(2, arrayType.prefixItems().get().length,
                     "Should have 2 prefix items");

        ArrayItemType[] items = arrayType.getItems();
        assertEquals(1, items.length,
                     "Should have items schema");

        assertTrue(arrayType.unevaluatedItems().isPresent(),
                   "unevaluatedItems should be present");
        assertFalse(arrayType.unevaluatedItems().get(),
                    "unevaluatedItems should be false");
    }

    @Test
    void testUnevaluatedItemsPrefixItemsTypes() {
        SchemaDefinition schema = parser.parse("src/test/resources/unevaluated-items-with-items.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        ArrayItemType[] prefixItems = arrayType.prefixItems().get();

        StringType firstItem = (StringType) prefixItems[0].getType();
        assertTrue(firstItem.minLength().isPresent(),
                   "First prefix item should have minLength");
        assertEquals(3, firstItem.minLength().get(),
                     "First prefix item minLength should be 3");

        IntegerType secondItem = (IntegerType) prefixItems[1].getType();
        assertTrue(secondItem.minimum().isPresent(),
                   "Second prefix item should have minimum");
        assertEquals(0, secondItem.minimum().get(),
                     "Second prefix item minimum should be 0");
    }

    @Test
    void testUnevaluatedItemsItemsType() {
        SchemaDefinition schema = parser.parse("src/test/resources/unevaluated-items-with-items.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        ArrayItemType[] items = arrayType.getItems();
        assertInstanceOf(BooleanType.class, items[0].getType(),
                         "items schema should be BooleanType");
    }

    // ============== Edge Cases ==============

    @Test
    void testSchemaWithoutUnevaluatedItems() {
        SchemaDefinition schema = parser.parse("src/test/resources/array-only-minItems.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertFalse(arrayType.unevaluatedItems().isPresent(),
                    "Schema without unevaluatedItems should return empty Optional");
    }

    @Test
    void testUnevaluatedItemsWithoutPrefixItems() {
        // Create a schema with unevaluatedItems but no prefixItems/items
        // (edge case - unevaluatedItems without prefix/items context)
        SchemaDefinition schema = parser.parse("src/test/resources/prefix-items-simple.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        // This schema has prefixItems but no unevaluatedItems
        assertFalse(arrayType.unevaluatedItems().isPresent(),
                    "Schema without unevaluatedItems should return empty Optional");
    }

    @Test
    void testUnevaluatedItemsFalseSemantics() {
        SchemaDefinition schema = parser.parse("src/test/resources/unevaluated-items-false.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        // Verify semantic meaning: false means no additional items beyond prefixItems
        assertTrue(arrayType.prefixItems().isPresent(),
                   "Schema should have prefixItems");
        assertEquals(2, arrayType.prefixItems().get().length,
                     "Should have exactly 2 prefix items");

        assertTrue(arrayType.unevaluatedItems().isPresent(),
                   "unevaluatedItems should be present");
        assertFalse(arrayType.unevaluatedItems().get(),
                    "unevaluatedItems: false means no items beyond prefixItems allowed");
    }

    @Test
    void testUnevaluatedItemsTrueSemantics() {
        SchemaDefinition schema = parser.parse("src/test/resources/unevaluated-items-true.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        // Verify semantic meaning: true means additional items are allowed
        assertTrue(arrayType.prefixItems().isPresent(),
                   "Schema should have prefixItems");
        assertEquals(2, arrayType.prefixItems().get().length,
                     "Should have exactly 2 prefix items");

        assertTrue(arrayType.unevaluatedItems().isPresent(),
                   "unevaluatedItems should be present");
        assertTrue(arrayType.unevaluatedItems().get(),
                   "unevaluatedItems: true means additional items are allowed");
    }

    // ============== Workflow.yaml Integration Test ==============

    @Test
    void testWorkflowSchemaUnevaluatedItems() {
        SchemaDefinition schema = parser.parse("src/test/resources/workflow.yaml");
        assertNotNull(schema, "workflow.yaml should parse successfully");

        // workflow.yaml may or may not use unevaluatedItems
        // This test verifies the parser can handle complex real-world schemas
        ObjectType model = (ObjectType) schema.model();
        assertNotNull(model, "workflow model should be created");
    }
}
