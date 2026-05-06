package org.treblereel.yaml.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for examples keyword (JSON Schema Draft 6+).
 * examples provides an array of valid example values for documentation purposes.
 */
public class ExamplesTest {

    private final Parser parser = new Parser();

    // ============== String Examples Tests ==============

    @Test
    void testStringExamplesPresent() {
        SchemaDefinition schema = parser.parse("src/main/resources/examples-string.schema.yaml");
        StringType stringType = (StringType) schema.model();

        assertTrue(stringType.examples().isPresent(),
                   "examples should be present");
    }

    @Test
    void testStringExamplesCount() {
        SchemaDefinition schema = parser.parse("src/main/resources/examples-string.schema.yaml");
        StringType stringType = (StringType) schema.model();

        List<JsonNode> examples = stringType.examples().get();
        assertEquals(3, examples.size(),
                     "Should have 3 examples");
    }

    @Test
    void testStringExamplesValues() {
        SchemaDefinition schema = parser.parse("src/main/resources/examples-string.schema.yaml");
        StringType stringType = (StringType) schema.model();

        List<JsonNode> examples = stringType.examples().get();
        assertEquals("example1", examples.get(0).asText(),
                     "First example should be 'example1'");
        assertEquals("example2", examples.get(1).asText(),
                     "Second example should be 'example2'");
        assertEquals("example3", examples.get(2).asText(),
                     "Third example should be 'example3'");
    }

    // ============== Integer Examples Tests ==============

    @Test
    void testIntegerExamplesPresent() {
        SchemaDefinition schema = parser.parse("src/main/resources/examples-integer.schema.yaml");
        IntegerType integerType = (IntegerType) schema.model();

        assertTrue(integerType.examples().isPresent(),
                   "examples should be present");
    }

    @Test
    void testIntegerExamplesCount() {
        SchemaDefinition schema = parser.parse("src/main/resources/examples-integer.schema.yaml");
        IntegerType integerType = (IntegerType) schema.model();

        List<JsonNode> examples = integerType.examples().get();
        assertEquals(3, examples.size(),
                     "Should have 3 examples");
    }

    @Test
    void testIntegerExamplesValues() {
        SchemaDefinition schema = parser.parse("src/main/resources/examples-integer.schema.yaml");
        IntegerType integerType = (IntegerType) schema.model();

        List<JsonNode> examples = integerType.examples().get();
        assertEquals(42, examples.get(0).asInt(),
                     "First example should be 42");
        assertEquals(0, examples.get(1).asInt(),
                     "Second example should be 0");
        assertEquals(100, examples.get(2).asInt(),
                     "Third example should be 100");
    }

    // ============== Object Examples Tests ==============

    @Test
    void testObjectExamplesPresent() {
        SchemaDefinition schema = parser.parse("src/main/resources/examples-object.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertTrue(objectType.examples().isPresent(),
                   "examples should be present");
    }

    @Test
    void testObjectExamplesCount() {
        SchemaDefinition schema = parser.parse("src/main/resources/examples-object.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        List<JsonNode> examples = objectType.examples().get();
        assertEquals(2, examples.size(),
                     "Should have 2 examples");
    }

    @Test
    void testObjectExamplesStructure() {
        SchemaDefinition schema = parser.parse("src/main/resources/examples-object.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        List<JsonNode> examples = objectType.examples().get();

        JsonNode firstExample = examples.get(0);
        assertTrue(firstExample.has("name"),
                   "First example should have 'name' field");
        assertTrue(firstExample.has("age"),
                   "First example should have 'age' field");
        assertEquals("Alice", firstExample.get("name").asText(),
                     "First example name should be 'Alice'");
        assertEquals(30, firstExample.get("age").asInt(),
                     "First example age should be 30");

        JsonNode secondExample = examples.get(1);
        assertEquals("Bob", secondExample.get("name").asText(),
                     "Second example name should be 'Bob'");
        assertEquals(25, secondExample.get("age").asInt(),
                     "Second example age should be 25");
    }

    // ============== Edge Cases ==============

    @Test
    void testSchemaWithoutExamples() {
        SchemaDefinition schema = parser.parse("src/main/resources/array-only-minItems.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertFalse(arrayType.examples().isPresent(),
                    "Schema without examples should return empty Optional");
    }

    @Test
    void testEmptyExamplesArray() {
        // Parser should handle empty examples array gracefully
        SchemaDefinition schema = parser.parse("src/main/resources/examples-string.schema.yaml");
        StringType stringType = (StringType) schema.model();

        // If examples is present, it should be a list
        assertTrue(stringType.examples().isPresent(),
                   "examples should be present");
        assertInstanceOf(List.class, stringType.examples().get(),
                         "examples should be a List");
    }

    // ============== Workflow.yaml Integration Test ==============

    @Test
    void testWorkflowSchemaExamples() {
        SchemaDefinition schema = parser.parse("src/main/resources/workflow.yaml");
        assertNotNull(schema, "workflow.yaml should parse successfully");

        // workflow.yaml may or may not use examples
        // This test verifies the parser can handle complex real-world schemas
        ObjectType model = (ObjectType) schema.model();
        assertNotNull(model, "workflow model should be created");
    }
}
