package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for deprecated keyword (JSON Schema Draft 2019-09+).
 * deprecated indicates that a schema is deprecated and should not be used.
 */
public class DeprecatedTest {

    private final Parser parser = new Parser();

    // ============== String Deprecated Tests ==============

    @Test
    void testStringDeprecatedTrue() {
        SchemaDefinition schema = parser.parse("src/test/resources/deprecated-string.schema.yaml");
        StringType stringType = (StringType) schema.model();

        assertTrue(stringType.deprecated().isPresent(),
                   "deprecated should be present");
        assertTrue(stringType.deprecated().get(),
                   "deprecated should be true");
    }

    @Test
    void testStringDeprecatedWithDescription() {
        SchemaDefinition schema = parser.parse("src/test/resources/deprecated-string.schema.yaml");
        StringType stringType = (StringType) schema.model();

        assertTrue(stringType.description().isPresent(),
                   "description should be present");
        assertTrue(stringType.description().get().contains("deprecated"),
                   "description should mention deprecation");
    }

    // ============== Object Deprecated Tests ==============

    @Test
    void testObjectDeprecatedTrue() {
        SchemaDefinition schema = parser.parse("src/test/resources/deprecated-object.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertTrue(objectType.deprecated().isPresent(),
                   "deprecated should be present");
        assertTrue(objectType.deprecated().get(),
                   "deprecated should be true");
    }

    @Test
    void testObjectDeprecatedWithProperties() {
        SchemaDefinition schema = parser.parse("src/test/resources/deprecated-object.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertTrue(objectType.properties().isPresent(),
                   "properties should be present");
        assertEquals(2, objectType.properties().get().size(),
                     "Should have 2 properties");
    }

    // ============== Integer Deprecated False Tests ==============

    @Test
    void testIntegerDeprecatedFalse() {
        SchemaDefinition schema = parser.parse("src/test/resources/deprecated-false.schema.yaml");
        IntegerType integerType = (IntegerType) schema.model();

        assertTrue(integerType.deprecated().isPresent(),
                   "deprecated should be present");
        assertFalse(integerType.deprecated().get(),
                    "deprecated should be false");
    }

    @Test
    void testIntegerDeprecatedFalseWithConstraints() {
        SchemaDefinition schema = parser.parse("src/test/resources/deprecated-false.schema.yaml");
        IntegerType integerType = (IntegerType) schema.model();

        assertTrue(integerType.minimum().isPresent(),
                   "minimum should be present");
        assertTrue(integerType.maximum().isPresent(),
                   "maximum should be present");
    }

    // ============== Edge Cases ==============

    @Test
    void testSchemaWithoutDeprecated() {
        SchemaDefinition schema = parser.parse("src/test/resources/array-only-minItems.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertFalse(arrayType.deprecated().isPresent(),
                    "Schema without deprecated should return empty Optional");
    }

    @Test
    void testDeprecatedOnAllTypes() {
        // Test that deprecated works on various types
        SchemaDefinition stringSchema = parser.parse("src/test/resources/deprecated-string.schema.yaml");
        StringType stringType = (StringType) stringSchema.model();
        assertTrue(stringType.deprecated().isPresent(),
                   "StringType should support deprecated");

        SchemaDefinition objectSchema = parser.parse("src/test/resources/deprecated-object.schema.yaml");
        ObjectType objectType = (ObjectType) objectSchema.model();
        assertTrue(objectType.deprecated().isPresent(),
                   "ObjectType should support deprecated");

        SchemaDefinition integerSchema = parser.parse("src/test/resources/deprecated-false.schema.yaml");
        IntegerType integerType = (IntegerType) integerSchema.model();
        assertTrue(integerType.deprecated().isPresent(),
                   "IntegerType should support deprecated");
    }

    @Test
    void testDeprecatedSemantics() {
        SchemaDefinition schema = parser.parse("src/test/resources/deprecated-string.schema.yaml");
        StringType stringType = (StringType) schema.model();

        // Verify semantic meaning: true means deprecated
        assertTrue(stringType.deprecated().isPresent(),
                   "deprecated should be present");
        assertTrue(stringType.deprecated().get(),
                   "deprecated: true means schema is deprecated");
    }

    @Test
    void testNotDeprecatedSemantics() {
        SchemaDefinition schema = parser.parse("src/test/resources/deprecated-false.schema.yaml");
        IntegerType integerType = (IntegerType) schema.model();

        // Verify semantic meaning: false means not deprecated
        assertTrue(integerType.deprecated().isPresent(),
                   "deprecated should be present");
        assertFalse(integerType.deprecated().get(),
                    "deprecated: false means schema is not deprecated");
    }

    // ============== Workflow.yaml Integration Test ==============

    @Test
    void testWorkflowSchemaDeprecated() {
        SchemaDefinition schema = parser.parse("src/test/resources/workflow.yaml");
        assertNotNull(schema, "workflow.yaml should parse successfully");

        // workflow.yaml may or may not use deprecated
        // This test verifies the parser can handle complex real-world schemas
        ObjectType model = (ObjectType) schema.model();
        assertNotNull(model, "workflow model should be created");
    }
}
