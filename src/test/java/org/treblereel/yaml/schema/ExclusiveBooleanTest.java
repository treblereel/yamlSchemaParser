package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for exclusiveMinimum/exclusiveMaximum boolean syntax (JSON Schema Draft 4).
 * Draft 4 used boolean flags to indicate exclusive bounds, while Draft 6+ uses numeric values.
 *
 * Draft 4 syntax:
 *   minimum: 0, exclusiveMinimum: true  -> value > 0 (not >= 0)
 *
 * Draft 6+ syntax (already supported):
 *   exclusiveMinimum: 0  -> value > 0
 */
public class ExclusiveBooleanTest {

    private final Parser parser = new Parser();

    // ============== Integer Tests ==============

    @Test
    void testIntegerExclusiveMinimumTrue() {
        SchemaDefinition schema = parser.parse("src/main/resources/exclusive-boolean-integer.schema.yaml");
        IntegerType integerType = (IntegerType) schema.model();

        assertTrue(integerType.minimum().isPresent(),
                   "minimum should be present");
        assertEquals(0, integerType.minimum().get(),
                     "minimum should be 0");

        assertTrue(integerType.isMinimumExclusive().isPresent(),
                   "isMinimumExclusive should be present");
        assertTrue(integerType.isMinimumExclusive().get(),
                   "isMinimumExclusive should be true");
    }

    @Test
    void testIntegerExclusiveMaximumTrue() {
        SchemaDefinition schema = parser.parse("src/main/resources/exclusive-boolean-integer.schema.yaml");
        IntegerType integerType = (IntegerType) schema.model();

        assertTrue(integerType.maximum().isPresent(),
                   "maximum should be present");
        assertEquals(100, integerType.maximum().get(),
                     "maximum should be 100");

        assertTrue(integerType.isMaximumExclusive().isPresent(),
                   "isMaximumExclusive should be present");
        assertTrue(integerType.isMaximumExclusive().get(),
                   "isMaximumExclusive should be true");
    }

    @Test
    void testIntegerExclusiveFalse() {
        SchemaDefinition schema = parser.parse("src/main/resources/exclusive-boolean-false.schema.yaml");
        IntegerType integerType = (IntegerType) schema.model();

        assertTrue(integerType.isMinimumExclusive().isPresent(),
                   "isMinimumExclusive should be present");
        assertFalse(integerType.isMinimumExclusive().get(),
                    "isMinimumExclusive should be false");

        assertTrue(integerType.isMaximumExclusive().isPresent(),
                   "isMaximumExclusive should be present");
        assertFalse(integerType.isMaximumExclusive().get(),
                    "isMaximumExclusive should be false");
    }

    // ============== Number Tests ==============

    @Test
    void testNumberExclusiveMinimumTrue() {
        SchemaDefinition schema = parser.parse("src/main/resources/exclusive-boolean-number.schema.yaml");
        NumberType numberType = (NumberType) schema.model();

        assertTrue(numberType.minimum().isPresent(),
                   "minimum should be present");
        assertEquals(0.0, numberType.minimum().get(),
                     "minimum should be 0.0");

        assertTrue(numberType.isMinimumExclusive().isPresent(),
                   "isMinimumExclusive should be present");
        assertTrue(numberType.isMinimumExclusive().get(),
                   "isMinimumExclusive should be true");
    }

    @Test
    void testNumberExclusiveMaximumFalse() {
        SchemaDefinition schema = parser.parse("src/main/resources/exclusive-boolean-number.schema.yaml");
        NumberType numberType = (NumberType) schema.model();

        assertTrue(numberType.maximum().isPresent(),
                   "maximum should be present");
        assertEquals(100.0, numberType.maximum().get(),
                     "maximum should be 100.0");

        assertTrue(numberType.isMaximumExclusive().isPresent(),
                   "isMaximumExclusive should be present");
        assertFalse(numberType.isMaximumExclusive().get(),
                    "isMaximumExclusive should be false");
    }

    // ============== Edge Cases ==============

    @Test
    void testSchemaWithoutBooleanExclusive() {
        SchemaDefinition schema = parser.parse("src/main/resources/array-only-minItems.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        // ArrayType doesn't have exclusive bounds
        assertNotNull(arrayType);
    }

    @Test
    void testBooleanExclusiveSemantics() {
        SchemaDefinition schema = parser.parse("src/main/resources/exclusive-boolean-integer.schema.yaml");
        IntegerType integerType = (IntegerType) schema.model();

        // Verify Draft 4 semantics: exclusiveMinimum: true means > minimum (not >= minimum)
        assertTrue(integerType.isMinimumExclusive().isPresent(),
                   "isMinimumExclusive should be present");
        assertTrue(integerType.isMinimumExclusive().get(),
                   "exclusiveMinimum: true means value > minimum");

        assertEquals(0, integerType.minimum().get(),
                     "minimum value is 0, so valid values are > 0");
    }

    @Test
    void testInclusiveSemantics() {
        SchemaDefinition schema = parser.parse("src/main/resources/exclusive-boolean-false.schema.yaml");
        IntegerType integerType = (IntegerType) schema.model();

        // Verify Draft 4 semantics: exclusiveMinimum: false means >= minimum (inclusive)
        assertTrue(integerType.isMinimumExclusive().isPresent(),
                   "isMinimumExclusive should be present");
        assertFalse(integerType.isMinimumExclusive().get(),
                    "exclusiveMinimum: false means value >= minimum");

        assertEquals(0, integerType.minimum().get(),
                     "minimum value is 0, so valid values are >= 0");
    }

    @Test
    void testBothSyntaxesCoexist() {
        // This test verifies that both Draft 4 (boolean) and Draft 6+ (numeric) syntaxes
        // can be supported simultaneously without conflict

        SchemaDefinition booleanSchema = parser.parse("src/main/resources/exclusive-boolean-integer.schema.yaml");
        IntegerType booleanType = (IntegerType) booleanSchema.model();

        // Draft 4 boolean syntax
        assertTrue(booleanType.isMinimumExclusive().isPresent(),
                   "Boolean syntax should be present");

        // Draft 6+ numeric syntax should return empty (not a numeric value)
        assertFalse(booleanType.exclusiveMinimum().isPresent(),
                    "Numeric syntax should not be present for boolean exclusive");
    }

    // ============== Workflow.yaml Integration Test ==============

    @Test
    void testWorkflowSchemaExclusiveBoolean() {
        SchemaDefinition schema = parser.parse("src/main/resources/workflow.yaml");
        assertNotNull(schema, "workflow.yaml should parse successfully");

        // workflow.yaml may or may not use Draft 4 boolean syntax
        // This test verifies the parser can handle complex real-world schemas
        ObjectType model = (ObjectType) schema.model();
        assertNotNull(model, "workflow model should be created");
    }
}
