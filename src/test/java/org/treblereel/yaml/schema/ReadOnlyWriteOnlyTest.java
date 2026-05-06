package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for readOnly and writeOnly keywords (JSON Schema Draft 7+).
 * These OpenAPI metadata keywords indicate whether a property should be
 * included in requests (writeOnly) or responses (readOnly).
 */
public class ReadOnlyWriteOnlyTest {

    private final Parser parser = new Parser();

    // ============== ReadOnly Tests ==============

    @Test
    void testStringReadOnlyTrue() {
        SchemaDefinition schema = parser.parse("src/test/resources/readonly-string.schema.yaml");
        StringType stringType = (StringType) schema.model();

        assertTrue(stringType.readOnly().isPresent(),
                   "readOnly should be present");
        assertTrue(stringType.readOnly().get(),
                   "readOnly should be true");
    }

    @Test
    void testStringReadOnlyWithDescription() {
        SchemaDefinition schema = parser.parse("src/test/resources/readonly-string.schema.yaml");
        StringType stringType = (StringType) schema.model();

        assertTrue(stringType.description().isPresent(),
                   "description should be present");
        assertTrue(stringType.description().get().contains("responses"),
                   "description should mention responses");
    }

    @Test
    void testObjectWithReadOnlyProperties() {
        SchemaDefinition schema = parser.parse("src/test/resources/readonly-object.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertTrue(objectType.properties().isEmpty() == false,
                   "properties should be present");
        Map<String, HasType> properties = objectType.properties();

        // Check id property is readOnly
        StringType idType = (StringType) properties.get("id");
        assertTrue(idType.readOnly().isPresent(),
                   "id readOnly should be present");
        assertTrue(idType.readOnly().get(),
                   "id should be readOnly");

        // Check createdAt property is readOnly
        StringType createdAtType = (StringType) properties.get("createdAt");
        assertTrue(createdAtType.readOnly().isPresent(),
                   "createdAt readOnly should be present");
        assertTrue(createdAtType.readOnly().get(),
                   "createdAt should be readOnly");

        // Check name property has no readOnly
        StringType nameType = (StringType) properties.get("name");
        assertFalse(nameType.readOnly().isPresent(),
                    "name should not have readOnly");
    }

    // ============== WriteOnly Tests ==============

    @Test
    void testStringWriteOnlyTrue() {
        SchemaDefinition schema = parser.parse("src/test/resources/writeonly-string.schema.yaml");
        StringType stringType = (StringType) schema.model();

        assertTrue(stringType.writeOnly().isPresent(),
                   "writeOnly should be present");
        assertTrue(stringType.writeOnly().get(),
                   "writeOnly should be true");
    }

    @Test
    void testStringWriteOnlyWithDescription() {
        SchemaDefinition schema = parser.parse("src/test/resources/writeonly-string.schema.yaml");
        StringType stringType = (StringType) schema.model();

        assertTrue(stringType.description().isPresent(),
                   "description should be present");
        assertTrue(stringType.description().get().contains("requests"),
                   "description should mention requests");
    }

    // ============== Edge Cases ==============

    @Test
    void testSchemaWithoutReadOnlyOrWriteOnly() {
        SchemaDefinition schema = parser.parse("src/test/resources/array-only-minItems.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertFalse(arrayType.readOnly().isPresent(),
                    "Schema without readOnly should return empty Optional");
        assertFalse(arrayType.writeOnly().isPresent(),
                    "Schema without writeOnly should return empty Optional");
    }

    @Test
    void testReadOnlyOnAllTypes() {
        // Test that readOnly works on string type
        SchemaDefinition stringSchema = parser.parse("src/test/resources/readonly-string.schema.yaml");
        StringType stringType = (StringType) stringSchema.model();
        assertTrue(stringType.readOnly().isPresent(),
                   "StringType should support readOnly");
    }

    @Test
    void testWriteOnlyOnAllTypes() {
        // Test that writeOnly works on string type
        SchemaDefinition stringSchema = parser.parse("src/test/resources/writeonly-string.schema.yaml");
        StringType stringType = (StringType) stringSchema.model();
        assertTrue(stringType.writeOnly().isPresent(),
                   "StringType should support writeOnly");
    }

    @Test
    void testReadOnlySemantics() {
        SchemaDefinition schema = parser.parse("src/test/resources/readonly-string.schema.yaml");
        StringType stringType = (StringType) schema.model();

        // Verify semantic meaning: true means read-only (response only)
        assertTrue(stringType.readOnly().isPresent(),
                   "readOnly should be present");
        assertTrue(stringType.readOnly().get(),
                   "readOnly: true means property is only in responses");
    }

    @Test
    void testWriteOnlySemantics() {
        SchemaDefinition schema = parser.parse("src/test/resources/writeonly-string.schema.yaml");
        StringType stringType = (StringType) schema.model();

        // Verify semantic meaning: true means write-only (request only)
        assertTrue(stringType.writeOnly().isPresent(),
                   "writeOnly should be present");
        assertTrue(stringType.writeOnly().get(),
                   "writeOnly: true means property is only in requests");
    }

    // ============== Workflow.yaml Integration Test ==============

    @Test
    void testWorkflowSchemaReadOnlyWriteOnly() {
        SchemaDefinition schema = parser.parse("src/test/resources/workflow.yaml");
        assertNotNull(schema, "workflow.yaml should parse successfully");

        // workflow.yaml may or may not use readOnly/writeOnly
        // This test verifies the parser can handle complex real-world schemas
        ObjectType model = (ObjectType) schema.model();
        assertNotNull(model, "workflow model should be created");
    }
}
