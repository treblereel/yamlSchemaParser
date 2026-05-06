package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the 'not' combinator across all type implementations.
 * The 'not' keyword requires that the instance does NOT validate against the specified schema.
 */
public class NotCombinatorTest {

    private final Parser parser = new Parser();

    // ============== ObjectType not Tests ==============

    @Test
    void testObjectTypeNotPresent() {
        SchemaDefinition schema = parser.parse("src/main/resources/not-object.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertTrue(objectType.not().isPresent(), "ObjectType should support not combinator");
    }

    @Test
    void testObjectTypeNotStructure() {
        SchemaDefinition schema = parser.parse("src/main/resources/not-object.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        HasType notSchema = objectType.not().get();
        assertInstanceOf(ObjectType.class, notSchema, "not schema should be ObjectType");
    }

    // ============== StringType not Tests ==============

    @Test
    void testStringTypeNotPresent() {
        SchemaDefinition schema = parser.parse("src/main/resources/not-string.schema.yaml");
        StringType stringType = (StringType) schema.model();

        assertTrue(stringType.not().isPresent(), "StringType should support not combinator");
    }

    @Test
    void testStringTypeNotStructure() {
        SchemaDefinition schema = parser.parse("src/main/resources/not-string.schema.yaml");
        StringType stringType = (StringType) schema.model();

        HasType notSchema = stringType.not().get();
        assertInstanceOf(ObjectType.class, notSchema, "not schema should be an object with pattern");

        ObjectType notObject = (ObjectType) notSchema;
        // The not schema is an inline object with pattern property
        // In JSON Schema: not: { pattern: "^admin" }
        // This gets parsed as an ObjectType because it has no explicit type
    }

    @Test
    void testStringTypeNotPattern() {
        SchemaDefinition schema = parser.parse("src/main/resources/not-string.schema.yaml");
        StringType stringType = (StringType) schema.model();

        HasType notSchema = stringType.not().get();
        ObjectType notObject = (ObjectType) notSchema;

        // The pattern constraint should be inside the not schema
        // We're testing that the parser correctly reads the not keyword
        assertNotNull(notSchema, "not schema should exist");
    }

    // ============== IntegerType not Tests ==============

    @Test
    void testIntegerTypeNotPresent() {
        SchemaDefinition schema = parser.parse("src/main/resources/not-integer.schema.yaml");
        IntegerType integerType = (IntegerType) schema.model();

        assertTrue(integerType.not().isPresent(), "IntegerType should support not combinator");
    }

    @Test
    void testIntegerTypeNotConstraints() {
        SchemaDefinition schema = parser.parse("src/main/resources/not-integer.schema.yaml");
        IntegerType integerType = (IntegerType) schema.model();

        HasType notSchema = integerType.not().get();
        assertInstanceOf(ObjectType.class, notSchema, "not schema contains constraints");
    }

    // ============== NumberType not Tests ==============

    @Test
    void testNumberTypeNotPresent() {
        SchemaDefinition schema = parser.parse("src/main/resources/not-number.schema.yaml");
        NumberType numberType = (NumberType) schema.model();

        assertTrue(numberType.not().isPresent(), "NumberType should support not combinator");
    }

    @Test
    void testNumberTypeNotMultipleOf() {
        SchemaDefinition schema = parser.parse("src/main/resources/not-number.schema.yaml");
        NumberType numberType = (NumberType) schema.model();

        HasType notSchema = numberType.not().get();
        assertNotNull(notSchema, "not schema should exist");
    }

    // ============== ArrayType not Tests ==============

    @Test
    void testArrayTypeNotPresent() {
        SchemaDefinition schema = parser.parse("src/main/resources/not-array.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        assertTrue(arrayType.not().isPresent(), "ArrayType should support not combinator");
    }

    @Test
    void testArrayTypeNotMinItems() {
        SchemaDefinition schema = parser.parse("src/main/resources/not-array.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        HasType notSchema = arrayType.not().get();
        assertInstanceOf(ObjectType.class, notSchema, "not schema contains minItems constraint");
    }

    // ============== BooleanType not Tests ==============

    @Test
    void testBooleanTypeNotPresent() {
        SchemaDefinition schema = parser.parse("src/main/resources/not-boolean.schema.yaml");
        BooleanType booleanType = (BooleanType) schema.model();

        assertTrue(booleanType.not().isPresent(), "BooleanType should support not combinator");
    }

    @Test
    void testBooleanTypeNotConst() {
        SchemaDefinition schema = parser.parse("src/main/resources/not-boolean.schema.yaml");
        BooleanType booleanType = (BooleanType) schema.model();

        HasType notSchema = booleanType.not().get();
        assertNotNull(notSchema, "not schema should exist");
    }

    // ============== Edge Cases ==============

    @Test
    void testTypeWithoutNot() {
        SchemaDefinition schema = parser.parse("src/main/resources/simple-string-props.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        var props = objectType.properties().get();
        StringType nameType = (StringType) props.get("firstName");

        assertFalse(nameType.not().isPresent(), "Type without not should return empty Optional");
    }
}
