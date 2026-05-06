package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.HasType;
import org.treblereel.yaml.schema.model.ObjectType;
import org.treblereel.yaml.schema.model.SchemaDefinition;
import org.treblereel.yaml.schema.model.StringType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for conditional schema keywords: if, then, else.
 * These keywords enable conditional validation based on data content.
 */
public class ConditionalSchemaTest {

    private final Parser parser = new Parser();

    // ============== Basic if/then/else Tests ==============

    @Test
    void testIfThenElsePresent() {
        SchemaDefinition schema = parser.parse("src/test/resources/if-then-else.schema.yaml");
        HasType type = schema.model();

        assertInstanceOf(ObjectType.class, type, "Root should be ObjectType");
        ObjectType objectType = (ObjectType) type;

        // Check if condition exists
        assertTrue(objectType.ifCondition().isPresent(), "if condition should be present");

        // Check then schema exists
        assertTrue(objectType.thenSchema().isPresent(), "then schema should be present");

        // Check else schema exists
        assertTrue(objectType.elseSchema().isPresent(), "else schema should be present");
    }

    @Test
    void testIfConditionStructure() {
        SchemaDefinition schema = parser.parse("src/test/resources/if-then-else.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        HasType ifCondition = objectType.ifCondition().get();
        assertInstanceOf(ObjectType.class, ifCondition, "if condition should be ObjectType");

        ObjectType ifObject = (ObjectType) ifCondition;
        assertTrue(ifObject.properties().isPresent(), "if condition should have properties");

        var props = ifObject.properties().get();
        assertTrue(props.containsKey("country"), "if condition should check 'country' property");

        // Check const value for country
        ObjectType countryCondition = (ObjectType) props.get("country");
        assertNotNull(countryCondition, "country condition should exist");
    }

    @Test
    void testThenSchemaStructure() {
        SchemaDefinition schema = parser.parse("src/test/resources/if-then-else.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        HasType thenSchema = objectType.thenSchema().get();
        assertInstanceOf(ObjectType.class, thenSchema, "then schema should be ObjectType");

        ObjectType thenObject = (ObjectType) thenSchema;
        assertTrue(thenObject.properties().isPresent(), "then schema should have properties");

        var props = thenObject.properties().get();
        assertTrue(props.containsKey("postalCode"), "then schema should define postalCode");

        StringType postalCodeType = (StringType) props.get("postalCode");
        assertTrue(postalCodeType.pattern().isPresent(), "postalCode should have pattern");
        assertEquals("^[0-9]{5}$", postalCodeType.pattern().get(), "USA postal code pattern");
    }

    @Test
    void testElseSchemaStructure() {
        SchemaDefinition schema = parser.parse("src/test/resources/if-then-else.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        HasType elseSchema = objectType.elseSchema().get();
        assertInstanceOf(ObjectType.class, elseSchema, "else schema should be ObjectType");

        ObjectType elseObject = (ObjectType) elseSchema;
        assertTrue(elseObject.properties().isPresent(), "else schema should have properties");

        var props = elseObject.properties().get();
        assertTrue(props.containsKey("postalCode"), "else schema should define postalCode");

        StringType postalCodeType = (StringType) props.get("postalCode");
        assertTrue(postalCodeType.pattern().isPresent(), "postalCode should have pattern");
        assertEquals("^[A-Z][0-9][A-Z] [0-9][A-Z][0-9]$", postalCodeType.pattern().get(),
                     "Canadian postal code pattern");
    }

    // ============== if/then only (no else) Tests ==============

    @Test
    void testIfThenWithoutElse() {
        SchemaDefinition schema = parser.parse("src/test/resources/if-then-only.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertTrue(objectType.ifCondition().isPresent(), "if condition should be present");
        assertTrue(objectType.thenSchema().isPresent(), "then schema should be present");
        assertFalse(objectType.elseSchema().isPresent(), "else schema should NOT be present");
    }

    @Test
    void testIfThenRequiredField() {
        SchemaDefinition schema = parser.parse("src/test/resources/if-then-only.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        HasType thenSchema = objectType.thenSchema().get();
        assertInstanceOf(ObjectType.class, thenSchema);

        ObjectType thenObject = (ObjectType) thenSchema;
        assertTrue(thenObject.required().isPresent(), "then schema should have required fields");

        var required = thenObject.required().get();
        assertTrue(required.contains("creditCard"), "creditCard should be required in then schema");
    }

    // ============== Nested if/then/else Tests ==============

    @Test
    void testNestedIfThenElse() {
        SchemaDefinition schema = parser.parse("src/test/resources/nested-if-then.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertTrue(objectType.ifCondition().isPresent(), "Outer if should be present");
        assertTrue(objectType.thenSchema().isPresent(), "Outer then should be present");

        // Check nested if/then/else inside the then schema
        HasType outerThen = objectType.thenSchema().get();
        assertInstanceOf(ObjectType.class, outerThen);

        ObjectType outerThenObject = (ObjectType) outerThen;
        assertTrue(outerThenObject.ifCondition().isPresent(), "Inner if should be present");
        assertTrue(outerThenObject.thenSchema().isPresent(), "Inner then should be present");
        assertTrue(outerThenObject.elseSchema().isPresent(), "Inner else should be present");
    }

    @Test
    void testNestedIfThenPatterns() {
        SchemaDefinition schema = parser.parse("src/test/resources/nested-if-then.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        ObjectType outerThen = (ObjectType) objectType.thenSchema().get();

        // Inner then - AMEX 15 digits
        ObjectType innerThen = (ObjectType) outerThen.thenSchema().get();
        assertTrue(innerThen.properties().isPresent());
        StringType amexPattern = (StringType) innerThen.properties().get().get("cardNumber");
        assertEquals("^[0-9]{15}$", amexPattern.pattern().get(), "AMEX should be 15 digits");

        // Inner else - Other cards 16 digits
        ObjectType innerElse = (ObjectType) outerThen.elseSchema().get();
        assertTrue(innerElse.properties().isPresent());
        StringType otherPattern = (StringType) innerElse.properties().get().get("cardNumber");
        assertEquals("^[0-9]{16}$", otherPattern.pattern().get(), "Other cards should be 16 digits");
    }

    // ============== Edge Cases ==============

    @Test
    void testSchemaWithoutConditionals() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertFalse(objectType.ifCondition().isPresent(), "if should not be present in simple schema");
        assertFalse(objectType.thenSchema().isPresent(), "then should not be present in simple schema");
        assertFalse(objectType.elseSchema().isPresent(), "else should not be present in simple schema");
    }

    @Test
    void testConditionalWithOtherCombinators() {
        // Test that if/then/else can coexist with allOf, anyOf, oneOf
        // This will be tested once we create a schema that combines them
    }

    // ============== Workflow.yaml Real-World Test ==============

    @Test
    void testWorkflowSchemaConditionals() {
        SchemaDefinition schema = parser.parse("src/test/resources/workflow.yaml");
        assertNotNull(schema, "workflow.yaml should parse successfully");

        // workflow.yaml uses if/then/else in several places
        // This test verifies the parser can handle a real-world complex schema
        HasType model = schema.model();
        assertNotNull(model, "workflow model should be created");
    }
}
