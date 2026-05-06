package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for contains, minContains, and maxContains keywords (JSON Schema Draft 6+).
 * contains specifies that an array must contain at least one element matching the schema.
 */
public class ContainsTest {

    private final Parser parser = new Parser();

    // ============== Basic contains Tests ==============

    @Test
    void testContainsPresent() {
        SchemaDefinition schema = parser.parse("src/test/resources/contains-simple.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertTrue(arrayType.contains().isPresent(),
                   "contains should be present");
    }

    @Test
    void testContainsSchemaType() {
        SchemaDefinition schema = parser.parse("src/test/resources/contains-simple.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        HasType containsSchema = arrayType.contains().get();
        assertInstanceOf(NumberType.class, containsSchema,
                         "contains schema should be NumberType");
    }

    @Test
    void testContainsSchemaConstraints() {
        SchemaDefinition schema = parser.parse("src/test/resources/contains-simple.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        NumberType numberType = (NumberType) arrayType.contains().get();

        assertTrue(numberType.minimum().isPresent(),
                   "Number should have minimum");
        assertEquals(5.0, numberType.minimum().get(),
                     "Number minimum should be 5");
    }

    // ============== minContains and maxContains Tests ==============

    @Test
    void testMinContainsPresent() {
        SchemaDefinition schema = parser.parse("src/test/resources/contains-with-min-max.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertTrue(arrayType.minContains().isPresent(),
                   "minContains should be present");
        assertEquals(2, arrayType.minContains().get(),
                     "minContains should be 2");
    }

    @Test
    void testMaxContainsPresent() {
        SchemaDefinition schema = parser.parse("src/test/resources/contains-with-min-max.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertTrue(arrayType.maxContains().isPresent(),
                   "maxContains should be present");
        assertEquals(5, arrayType.maxContains().get(),
                     "maxContains should be 5");
    }

    @Test
    void testContainsWithMinMaxSchema() {
        SchemaDefinition schema = parser.parse("src/test/resources/contains-with-min-max.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertTrue(arrayType.contains().isPresent(),
                   "contains should be present");

        HasType containsSchema = arrayType.contains().get();
        assertInstanceOf(StringType.class, containsSchema,
                         "contains schema should be StringType");

        StringType stringType = (StringType) containsSchema;
        assertTrue(stringType.pattern().isPresent(),
                   "String should have pattern");
        assertEquals("^test", stringType.pattern().get(),
                     "String pattern should be ^test");
    }

    // ============== Complex contains Tests ==============

    @Test
    void testComplexContainsObjectType() {
        SchemaDefinition schema = parser.parse("src/test/resources/contains-complex.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        HasType containsSchema = arrayType.contains().get();
        assertInstanceOf(ObjectType.class, containsSchema,
                         "contains schema should be ObjectType");
    }

    @Test
    void testComplexContainsObjectProperties() {
        SchemaDefinition schema = parser.parse("src/test/resources/contains-complex.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        ObjectType objectType = (ObjectType) arrayType.contains().get();

        assertTrue(objectType.properties().isEmpty() == false,
                   "Object should have properties");

        Map<String, HasType> props = objectType.properties();
        assertTrue(props.containsKey("status"),
                   "Object should have status property");
    }

    @Test
    void testComplexContainsObjectRequired() {
        SchemaDefinition schema = parser.parse("src/test/resources/contains-complex.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        ObjectType objectType = (ObjectType) arrayType.contains().get();

        assertTrue(objectType.required().isPresent(),
                   "Object should have required fields");
        assertTrue(objectType.required().get().contains("status"),
                   "status should be required");
    }

    @Test
    void testComplexContainsStatusEnum() {
        SchemaDefinition schema = parser.parse("src/test/resources/contains-complex.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        ObjectType objectType = (ObjectType) arrayType.contains().get();
        Map<String, HasType> props = objectType.properties();
        StringType statusType = (StringType) props.get("status");

        assertTrue(statusType.enumValues().isPresent(),
                   "status should have enum values");
        assertEquals(2, statusType.enumValues().get().size(),
                     "status should have 2 enum values");
        assertTrue(statusType.enumValues().get().contains("active"),
                   "status enum should contain 'active'");
        assertTrue(statusType.enumValues().get().contains("pending"),
                   "status enum should contain 'pending'");
    }

    @Test
    void testComplexContainsMinContains() {
        SchemaDefinition schema = parser.parse("src/test/resources/contains-complex.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertTrue(arrayType.minContains().isPresent(),
                   "minContains should be present");
        assertEquals(1, arrayType.minContains().get(),
                     "minContains should be 1");
    }

    @Test
    void testComplexContainsNoMaxContains() {
        SchemaDefinition schema = parser.parse("src/test/resources/contains-complex.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertFalse(arrayType.maxContains().isPresent(),
                    "maxContains should not be present");
    }

    // ============== Edge Cases ==============

    @Test
    void testSchemaWithoutContains() {
        SchemaDefinition schema = parser.parse("src/test/resources/array-only-minItems.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertFalse(arrayType.contains().isPresent(),
                    "Schema without contains should return empty Optional");
        assertFalse(arrayType.minContains().isPresent(),
                    "Schema without minContains should return empty Optional");
        assertFalse(arrayType.maxContains().isPresent(),
                    "Schema without maxContains should return empty Optional");
    }

    @Test
    void testContainsWithoutMinMax() {
        SchemaDefinition schema = parser.parse("src/test/resources/contains-simple.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertTrue(arrayType.contains().isPresent(),
                   "contains should be present");
        assertFalse(arrayType.minContains().isPresent(),
                    "minContains should not be present");
        assertFalse(arrayType.maxContains().isPresent(),
                    "maxContains should not be present");
    }

    // ============== Workflow.yaml Integration Test ==============

    @Test
    void testWorkflowSchemaContains() {
        SchemaDefinition schema = parser.parse("src/test/resources/workflow.yaml");
        assertNotNull(schema, "workflow.yaml should parse successfully");

        // workflow.yaml may or may not use contains
        // This test verifies the parser can handle complex real-world schemas
        ObjectType model = (ObjectType) schema.model();
        assertNotNull(model, "workflow model should be created");
    }
}
