package org.treblereel.yaml.schema.model;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.Parser;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for type resolution edge cases and corner cases in NodeFactory.
 * Verifies correct handling of ambiguous schemas, mixed types, and special cases.
 */
public class TypeResolutionTest {

    private final Parser parser = new Parser();

    // ============== Nullable Type Tests ==============

    @Test
    void testNullableItemsArray() {
        SchemaDefinition schema = parser.parse("src/test/resources/nullable-items-array.schema.yaml");
        ArrayType arrayType = (ArrayType) schema.model();

        ArrayItemType[] items = arrayType.getItems();
        assertTrue(items.length > 0, "Array should have items");

        // Check if item type supports nullable
        ArrayItemType firstItem = items[0];
        boolean isNullable = firstItem.isNullable();
        assertNotNull(isNullable, "isNullable() should return a value");
    }

    // ============== Empty Schema Edge Cases ==============

    @Test
    void testEmptyObject() {
        SchemaDefinition schema = parser.parse("src/test/resources/empty-object.schema.yaml");
        HasType type = schema.model();

        assertInstanceOf(ObjectType.class, type, "Empty schema should resolve to ObjectType");
        ObjectType objectType = (ObjectType) type;
        assertFalse(objectType.properties().isEmpty() == false, "Empty object should have no properties");
    }

    @Test
    void testEmptyArrayConstraint() {
        SchemaDefinition schema = parser.parse("src/test/resources/empty-array-constraint.schema.yaml");
        HasType type = schema.model();

        assertInstanceOf(ArrayType.class, type, "Should resolve to ArrayType");
        ArrayType arrayType = (ArrayType) type;
        assertTrue(arrayType.minItems().isPresent(), "Should have minItems constraint");
        assertEquals(0, arrayType.minItems().get(), "Min items should be 0");
        assertTrue(arrayType.maxItems().isPresent(), "Should have maxItems constraint");
        assertEquals(0, arrayType.maxItems().get(), "Max items should be 0");
    }

    // ============== Implicit Type Resolution ==============

    @Test
    void testImplicitObjectType() {
        SchemaDefinition schema = parser.parse("src/test/resources/implicit-object.schema.yaml");
        HasType type = schema.model();

        assertInstanceOf(ObjectType.class, type, "Schema with properties but no explicit 'type' should resolve to ObjectType");
        ObjectType objectType = (ObjectType) type;
        assertTrue(objectType.properties().isEmpty() == false, "Should have properties");
        assertTrue(objectType.required().isPresent(), "Should have required fields");
    }

    // ============== oneOf/anyOf at Root Level ==============

    @Test
    void testRootAnyOf() {
        SchemaDefinition schema = parser.parse("src/test/resources/root-anyof.schema.yaml");
        HasType type = schema.model();

        assertInstanceOf(AnyOfType.class, type, "Should resolve to AnyOfType");
        AnyOfType anyOfType = (AnyOfType) type;
        assertEquals(2, anyOfType.getAnyOf().size(), "anyOf should have 2 alternatives");
    }

    @Test
    void testRootOneOf() {
        SchemaDefinition schema = parser.parse("src/test/resources/root-oneof.schema.yaml");
        HasType type = schema.model();

        assertInstanceOf(OneOfType.class, type, "Should resolve to OneOfType");
        OneOfType oneOfType = (OneOfType) type;
        assertEquals(2, oneOfType.getOneOf().size(), "oneOf should have 2 alternatives");
    }

    @Test
    void testRootAnyOfMetadata() {
        SchemaDefinition schema = parser.parse("src/test/resources/root-anyof-metadata.schema.yaml");
        HasType type = schema.model();

        assertInstanceOf(AnyOfType.class, type);
        assertTrue(type.title().isPresent(), "Should have title");
        assertEquals("Contact Info", type.title().get());
        assertTrue(type.description().isPresent(), "Should have description");
        assertEquals("Either email or phone contact", type.description().get());
    }

    @Test
    void testRootOneOfMetadata() {
        SchemaDefinition schema = parser.parse("src/test/resources/root-oneof-metadata.schema.yaml");
        HasType type = schema.model();

        assertInstanceOf(OneOfType.class, type);
        assertTrue(type.title().isPresent(), "Should have title");
        assertEquals("Payment Method", type.title().get());
        assertTrue(type.description().isPresent(), "Should have description");
        assertEquals("Exactly one payment method", type.description().get());
    }

    @Test
    void testOneOfWithTypeFieldStillReturnsObjectType() {
        SchemaDefinition schema = parser.parse("src/test/resources/discriminator.schema.yaml");
        HasType type = schema.model();

        assertInstanceOf(ObjectType.class, type, "type: object + oneOf should still be ObjectType");
        ObjectType objectType = (ObjectType) type;
        assertTrue(objectType.oneOf().isPresent(), "Should have oneOf");
        assertEquals(3, objectType.oneOf().get().size());
    }

    // ============== allOf Type Resolution ==============

    @Test
    void testAllOfAsRootType() {
        SchemaDefinition schema = parser.parse("src/test/resources/with-allof.schema.yaml");
        HasType type = schema.model();

        assertInstanceOf(AllOfType.class, type, "Schema with allOf at root should resolve to AllOfType");
        AllOfType allOfType = (AllOfType) type;
        assertEquals(3, allOfType.getAllOf().size(), "allOf should have 3 schemas");
    }

    @Test
    void testAllOfWithRef() {
        SchemaDefinition schema = parser.parse("src/test/resources/allof-with-ref.schema.yaml");
        HasType type = schema.model();

        assertInstanceOf(AllOfType.class, type, "Should resolve to AllOfType");
        AllOfType allOfType = (AllOfType) type;
        assertEquals(2, allOfType.getAllOf().size(), "allOf should have 2 items");

        // First item should be a RefType
        assertInstanceOf(RefType.class, allOfType.getAllOf().get(0), "First allOf item should be RefType");
    }

    // ============== $ref Only Schema ==============

    @Test
    void testRefOnlySchema() {
        SchemaDefinition schema = parser.parse("src/test/resources/ref-only.schema.yaml");
        HasType type = schema.model();

        assertInstanceOf(RefType.class, type, "Schema that is only a $ref should resolve to RefType");
        RefType refType = (RefType) type;
        assertEquals("#/$defs/MainType", refType.ref(), "Should point to correct definition");

        HasType resolved = refType.resolve();
        assertInstanceOf(ObjectType.class, resolved, "Resolved type should be ObjectType");
    }

    // ============== Mixed Constraints ==============

    @Test
    void testCombinedConstraints() {
        SchemaDefinition schema = parser.parse("src/test/resources/combined-constraints.schema.yaml");
        HasType type = schema.model();

        assertInstanceOf(ObjectType.class, type);
        ObjectType objectType = (ObjectType) type;

        // Should have multiple constraints
        assertTrue(objectType.title().isPresent(), "Should have title");
        assertTrue(objectType.description().isPresent(), "Should have description");
        assertTrue(objectType.additionalProperties().isPresent(), "Should have additionalProperties");
        assertFalse(objectType.additionalProperties().get().isAllowed(), "additionalProperties should be false");
        assertTrue(objectType.required().isPresent(), "Should have required");
        assertEquals(2, objectType.required().get().size(), "Should have 2 required fields");
        assertTrue(objectType.properties().isEmpty() == false, "Should have properties");
        assertEquals(3, objectType.properties().size(), "Should have 3 properties");
    }

    // ============== Pattern Properties Edge Cases ==============

    @Test
    void testPatternProperties() {
        SchemaDefinition schema = parser.parse("src/test/resources/pattern-properties.schema.yaml");
        HasType type = schema.model();

        assertInstanceOf(ObjectType.class, type);
        ObjectType objectType = (ObjectType) type;

        assertTrue(objectType.additionalProperties().isPresent(), "Should have additionalProperties");
        assertFalse(objectType.additionalProperties().get().isAllowed(), "additionalProperties should be false when patternProperties is used");
    }

    // ============== All Primitive Types Test ==============

    @Test
    void testAllPrimitiveTypes() {
        SchemaDefinition schema = parser.parse("src/test/resources/all-primitive-types.schema.yaml");
        HasType type = schema.model();

        assertInstanceOf(ObjectType.class, type);
        ObjectType objectType = (ObjectType) type;

        assertTrue(objectType.properties().isEmpty() == false, "Should have properties");
        var props = objectType.properties();

        assertEquals(6, props.size(), "Should have 6 properties");
        assertInstanceOf(StringType.class, props.get("stringProp"), "stringProp should be StringType");
        assertInstanceOf(IntegerType.class, props.get("integerProp"), "integerProp should be IntegerType");
        assertInstanceOf(NumberType.class, props.get("numberProp"), "numberProp should be NumberType");
        assertInstanceOf(BooleanType.class, props.get("booleanProp"), "booleanProp should be BooleanType");
        assertInstanceOf(ArrayType.class, props.get("arrayProp"), "arrayProp should be ArrayType");
        assertInstanceOf(ObjectType.class, props.get("objectProp"), "objectProp should be ObjectType");
    }

    // ============== Discriminator Handling ==============

    @Test
    void testDiscriminator() {
        SchemaDefinition schema = parser.parse("src/test/resources/discriminator.schema.yaml");
        HasType type = schema.model();

        assertInstanceOf(ObjectType.class, type);
        ObjectType objectType = (ObjectType) type;

        assertTrue(objectType.required().isPresent(), "Should have required fields");
        assertTrue(objectType.required().get().contains("type"), "Should require 'type' field for discriminator");
        assertTrue(objectType.oneOf().isPresent(), "Discriminator pattern uses oneOf");
        assertEquals(3, objectType.oneOf().get().size(), "Should have 3 oneOf alternatives");
    }
}
