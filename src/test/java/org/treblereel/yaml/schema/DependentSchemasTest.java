package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for dependentSchemas keyword.
 * dependentSchemas specifies that if property X is present, then an additional schema must be applied.
 */
public class DependentSchemasTest {

    private final Parser parser = new Parser();

    // ============== Basic dependentSchemas Tests ==============

    @Test
    void testDependentSchemasPresent() {
        SchemaDefinition schema = parser.parse("src/test/resources/dependent-schemas-simple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertTrue(objectType.dependentSchemas().isEmpty() == false,
                   "dependentSchemas should be present");
    }

    @Test
    void testDependentSchemasMapSize() {
        SchemaDefinition schema = parser.parse("src/test/resources/dependent-schemas-simple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> deps = objectType.dependentSchemas();
        assertEquals(1, deps.size(), "Should have 1 dependent schema");
    }

    @Test
    void testDependentSchemasKeys() {
        SchemaDefinition schema = parser.parse("src/test/resources/dependent-schemas-simple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> deps = objectType.dependentSchemas();
        assertTrue(deps.containsKey("creditCard"),
                   "Should have creditCard dependency");
    }

    @Test
    void testDependentSchemasCreditCardSchema() {
        SchemaDefinition schema = parser.parse("src/test/resources/dependent-schemas-simple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> deps = objectType.dependentSchemas();
        HasType creditCardSchema = deps.get("creditCard");

        assertNotNull(creditCardSchema, "creditCard should have dependent schema");
        assertInstanceOf(ObjectType.class, creditCardSchema,
                         "dependent schema should be ObjectType");
    }

    @Test
    void testDependentSchemasCreditCardProperties() {
        SchemaDefinition schema = parser.parse("src/test/resources/dependent-schemas-simple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> deps = objectType.dependentSchemas();
        ObjectType creditCardSchema = (ObjectType) deps.get("creditCard");

        assertTrue(creditCardSchema.properties().isEmpty() == false,
                   "creditCard schema should have properties");

        Map<String, HasType> props = creditCardSchema.properties();
        assertTrue(props.containsKey("billingAddress"),
                   "Should have billingAddress property");
        assertTrue(props.containsKey("cvv"),
                   "Should have cvv property");
    }

    @Test
    void testDependentSchemasCreditCardRequired() {
        SchemaDefinition schema = parser.parse("src/test/resources/dependent-schemas-simple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> deps = objectType.dependentSchemas();
        ObjectType creditCardSchema = (ObjectType) deps.get("creditCard");

        assertTrue(creditCardSchema.required().isPresent(),
                   "creditCard schema should have required fields");

        List<String> required = creditCardSchema.required().get();
        assertEquals(2, required.size(), "Should have 2 required fields");
        assertTrue(required.contains("billingAddress"),
                   "Should require billingAddress");
        assertTrue(required.contains("cvv"),
                   "Should require cvv");
    }

    @Test
    void testDependentSchemasCvvPattern() {
        SchemaDefinition schema = parser.parse("src/test/resources/dependent-schemas-simple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> deps = objectType.dependentSchemas();
        ObjectType creditCardSchema = (ObjectType) deps.get("creditCard");

        Map<String, HasType> props = creditCardSchema.properties();
        StringType cvvType = (StringType) props.get("cvv");

        assertTrue(cvvType.pattern().isPresent(),
                   "cvv should have pattern");
        assertEquals("^[0-9]{3,4}$", cvvType.pattern().get(),
                     "cvv pattern should be 3-4 digits");
    }

    // ============== Multiple Dependencies Tests ==============

    @Test
    void testMultipleDependentSchemas() {
        SchemaDefinition schema = parser.parse("src/test/resources/dependent-schemas-multiple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> deps = objectType.dependentSchemas();
        assertEquals(2, deps.size(), "Should have 2 dependent schemas");
    }

    @Test
    void testMultipleDependentSchemasKeys() {
        SchemaDefinition schema = parser.parse("src/test/resources/dependent-schemas-multiple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> deps = objectType.dependentSchemas();

        assertTrue(deps.containsKey("email"), "Should have email dependency");
        assertTrue(deps.containsKey("isPremium"), "Should have isPremium dependency");
    }

    @Test
    void testMultipleDependentSchemasEmailSchema() {
        SchemaDefinition schema = parser.parse("src/test/resources/dependent-schemas-multiple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> deps = objectType.dependentSchemas();
        ObjectType emailSchema = (ObjectType) deps.get("email");

        assertTrue(emailSchema.properties().isEmpty() == false,
                   "email schema should have properties");

        Map<String, HasType> props = emailSchema.properties();
        assertTrue(props.containsKey("emailVerified"),
                   "Should have emailVerified property");

        List<String> required = emailSchema.required().get();
        assertTrue(required.contains("emailVerified"),
                   "Should require emailVerified");
    }

    @Test
    void testMultipleDependentSchemasIsPremiumSchema() {
        SchemaDefinition schema = parser.parse("src/test/resources/dependent-schemas-multiple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> deps = objectType.dependentSchemas();
        ObjectType premiumSchema = (ObjectType) deps.get("isPremium");

        assertTrue(premiumSchema.properties().isEmpty() == false,
                   "isPremium schema should have properties");

        Map<String, HasType> props = premiumSchema.properties();
        assertTrue(props.containsKey("subscriptionId"),
                   "Should have subscriptionId property");
        assertTrue(props.containsKey("expiryDate"),
                   "Should have expiryDate property");

        List<String> required = premiumSchema.required().get();
        assertEquals(2, required.size(), "Should have 2 required fields");
        assertTrue(required.contains("subscriptionId"),
                   "Should require subscriptionId");
        assertTrue(required.contains("expiryDate"),
                   "Should require expiryDate");
    }

    // ============== Mixed Tests ==============

    @Test
    void testMixedRequiredAndDependentSchemas() {
        SchemaDefinition schema = parser.parse("src/test/resources/dependent-schemas-mixed.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertTrue(objectType.required().isPresent(),
                   "Should have regular required fields");
        assertTrue(objectType.dependentSchemas().isEmpty() == false,
                   "Should have dependent schemas");
    }

    @Test
    void testMixedRegularRequired() {
        SchemaDefinition schema = parser.parse("src/test/resources/dependent-schemas-mixed.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        List<String> required = objectType.required().get();
        assertEquals(2, required.size(), "Should have 2 regular required fields");
        assertTrue(required.contains("name"), "name should be required");
        assertTrue(required.contains("type"), "type should be required");
    }

    @Test
    void testMixedDependentSchemasWithOneOf() {
        SchemaDefinition schema = parser.parse("src/test/resources/dependent-schemas-mixed.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> deps = objectType.dependentSchemas();
        assertEquals(1, deps.size(), "Should have 1 dependent schema");
        assertTrue(deps.containsKey("type"),
                   "Should have type dependency");

        HasType typeSchema = deps.get("type");
        assertInstanceOf(OneOfType.class, typeSchema,
                         "type schema should be OneOfType");

        OneOfType typeOneOf = (OneOfType) typeSchema;
        List<HasType> oneOfList = typeOneOf.getOneOf();
        assertEquals(2, oneOfList.size(), "oneOf should have 2 alternatives");
    }

    // ============== Edge Cases ==============

    @Test
    void testSchemaWithoutDependentSchemas() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertFalse(objectType.dependentSchemas().isEmpty() == false,
                    "Schema without dependentSchemas should return empty Optional");
    }

    // ============== Workflow.yaml Integration Test ==============

    @Test
    void testWorkflowSchemaDependentSchemas() {
        SchemaDefinition schema = parser.parse("src/test/resources/workflow.yaml");
        assertNotNull(schema, "workflow.yaml should parse successfully");

        // workflow.yaml may or may not use dependentSchemas
        // This test verifies the parser can handle complex real-world schemas
        ObjectType model = (ObjectType) schema.model();
        assertNotNull(model, "workflow model should be created");
    }
}
