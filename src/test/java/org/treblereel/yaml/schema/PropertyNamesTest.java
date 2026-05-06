package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for propertyNames keyword.
 * propertyNames defines a schema that ALL property names must validate against.
 */
public class PropertyNamesTest {

    private final Parser parser = new Parser();

    // ============== Basic propertyNames Tests ==============

    @Test
    void testPropertyNamesPresent() {
        SchemaDefinition schema = parser.parse("src/main/resources/property-names-pattern.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertTrue(objectType.propertyNames().isPresent(),
                   "propertyNames should be present");
    }

    @Test
    void testPropertyNamesReturnsSchema() {
        SchemaDefinition schema = parser.parse("src/main/resources/property-names-pattern.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        HasType propertyNamesSchema = objectType.propertyNames().get();
        assertNotNull(propertyNamesSchema, "propertyNames should return a schema");
    }

    @Test
    void testPropertyNamesPatternSchema() {
        SchemaDefinition schema = parser.parse("src/main/resources/property-names-pattern.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        HasType propertyNamesSchema = objectType.propertyNames().get();

        // propertyNames with pattern becomes an ObjectType with pattern property
        assertInstanceOf(ObjectType.class, propertyNamesSchema,
                         "propertyNames should be ObjectType when only pattern specified");
    }

    // ============== Length Constraint Tests ==============

    @Test
    void testPropertyNamesLengthPresent() {
        SchemaDefinition schema = parser.parse("src/main/resources/property-names-length.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertTrue(objectType.propertyNames().isPresent(),
                   "propertyNames with length constraints should be present");
    }

    @Test
    void testPropertyNamesLengthSchema() {
        SchemaDefinition schema = parser.parse("src/main/resources/property-names-length.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        HasType propertyNamesSchema = objectType.propertyNames().get();
        assertInstanceOf(StringType.class, propertyNamesSchema,
                         "propertyNames with explicit type: string should be StringType");
    }

    @Test
    void testPropertyNamesLengthConstraints() {
        SchemaDefinition schema = parser.parse("src/main/resources/property-names-length.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        HasType propertyNamesSchema = objectType.propertyNames().get();
        StringType stringSchema = (StringType) propertyNamesSchema;

        assertTrue(stringSchema.minLength().isPresent(),
                   "propertyNames should have minLength");
        assertEquals(3, stringSchema.minLength().get(),
                     "minLength should be 3");

        assertTrue(stringSchema.maxLength().isPresent(),
                   "propertyNames should have maxLength");
        assertEquals(20, stringSchema.maxLength().get(),
                     "maxLength should be 20");
    }

    // ============== Mixed Properties + PropertyNames Tests ==============

    @Test
    void testMixedPropertiesAndPropertyNames() {
        SchemaDefinition schema = parser.parse("src/main/resources/property-names-mixed.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertTrue(objectType.properties().isPresent(),
                   "Should have regular properties");
        assertTrue(objectType.propertyNames().isPresent(),
                   "Should have propertyNames constraint");
    }

    @Test
    void testMixedPropertiesStructure() {
        SchemaDefinition schema = parser.parse("src/main/resources/property-names-mixed.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        var props = objectType.properties().get();
        assertEquals(2, props.size(), "Should have 2 properties");
        assertTrue(props.containsKey("id"), "Should have id property");
        assertTrue(props.containsKey("name"), "Should have name property");
    }

    @Test
    void testMixedPropertyNamesPattern() {
        SchemaDefinition schema = parser.parse("src/main/resources/property-names-mixed.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        HasType propertyNamesSchema = objectType.propertyNames().get();
        assertNotNull(propertyNamesSchema,
                      "propertyNames constraint should exist");
    }

    // ============== Edge Cases ==============

    @Test
    void testSchemaWithoutPropertyNames() {
        SchemaDefinition schema = parser.parse("src/main/resources/simple-string-props.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertFalse(objectType.propertyNames().isPresent(),
                    "Schema without propertyNames should return empty Optional");
    }

    @Test
    void testPropertyNamesWithPatternProperties() {
        // propertyNames applies to ALL properties, including those matched by patternProperties
        SchemaDefinition schema = parser.parse("src/main/resources/property-names-pattern.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertTrue(objectType.propertyNames().isPresent(),
                   "propertyNames should work independently");

        // This schema doesn't have patternProperties, but they can coexist
        assertFalse(objectType.patternProperties().isPresent(),
                    "This particular schema has no patternProperties");
    }

    // ============== Workflow.yaml Integration Test ==============

    @Test
    void testWorkflowSchemaPropertyNames() {
        SchemaDefinition schema = parser.parse("src/main/resources/workflow.yaml");
        assertNotNull(schema, "workflow.yaml should parse successfully");

        // workflow.yaml may or may not use propertyNames
        // This test verifies the parser can handle complex real-world schemas
        HasType model = schema.model();
        assertNotNull(model, "workflow model should be created");
    }
}
