package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.ObjectType;
import org.treblereel.yaml.schema.model.SchemaDefinition;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for dependentRequired keyword.
 * dependentRequired specifies that if property X is present, then properties Y and Z must also be present.
 */
public class DependentRequiredTest {

    private final Parser parser = new Parser();

    // ============== Basic dependentRequired Tests ==============

    @Test
    void testDependentRequiredPresent() {
        SchemaDefinition schema = parser.parse("src/main/resources/dependent-required-simple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertTrue(objectType.dependentRequired().isPresent(),
                   "dependentRequired should be present");
    }

    @Test
    void testDependentRequiredMapSize() {
        SchemaDefinition schema = parser.parse("src/main/resources/dependent-required-simple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, List<String>> deps = objectType.dependentRequired().get();
        assertEquals(1, deps.size(), "Should have 1 dependent requirement");
    }

    @Test
    void testDependentRequiredKeys() {
        SchemaDefinition schema = parser.parse("src/main/resources/dependent-required-simple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, List<String>> deps = objectType.dependentRequired().get();
        assertTrue(deps.containsKey("creditCard"),
                   "Should have creditCard dependency");
    }

    @Test
    void testDependentRequiredValues() {
        SchemaDefinition schema = parser.parse("src/main/resources/dependent-required-simple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, List<String>> deps = objectType.dependentRequired().get();
        List<String> required = deps.get("creditCard");

        assertNotNull(required, "creditCard should have required properties");
        assertEquals(2, required.size(), "Should require 2 properties");
        assertTrue(required.contains("billingAddress"),
                   "Should require billingAddress");
        assertTrue(required.contains("cvv"),
                   "Should require cvv");
    }

    // ============== Multiple Dependencies Tests ==============

    @Test
    void testMultipleDependentRequired() {
        SchemaDefinition schema = parser.parse("src/main/resources/dependent-required-multiple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, List<String>> deps = objectType.dependentRequired().get();
        assertEquals(3, deps.size(), "Should have 3 dependent requirements");
    }

    @Test
    void testMultipleDependentRequiredKeys() {
        SchemaDefinition schema = parser.parse("src/main/resources/dependent-required-multiple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, List<String>> deps = objectType.dependentRequired().get();

        assertTrue(deps.containsKey("email"), "Should have email dependency");
        assertTrue(deps.containsKey("password"), "Should have password dependency");
        assertTrue(deps.containsKey("securityQuestion"),
                   "Should have securityQuestion dependency");
    }

    @Test
    void testMultipleDependentRequiredEmailDependency() {
        SchemaDefinition schema = parser.parse("src/main/resources/dependent-required-multiple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, List<String>> deps = objectType.dependentRequired().get();
        List<String> emailDeps = deps.get("email");

        assertEquals(1, emailDeps.size(), "email should require 1 property");
        assertTrue(emailDeps.contains("password"), "email should require password");
    }

    @Test
    void testMultipleDependentRequiredPasswordDependency() {
        SchemaDefinition schema = parser.parse("src/main/resources/dependent-required-multiple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, List<String>> deps = objectType.dependentRequired().get();
        List<String> passwordDeps = deps.get("password");

        assertEquals(1, passwordDeps.size(), "password should require 1 property");
        assertTrue(passwordDeps.contains("confirmPassword"),
                   "password should require confirmPassword");
    }

    @Test
    void testMultipleDependentRequiredSecurityQuestionDependency() {
        SchemaDefinition schema = parser.parse("src/main/resources/dependent-required-multiple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, List<String>> deps = objectType.dependentRequired().get();
        List<String> securityDeps = deps.get("securityQuestion");

        assertEquals(1, securityDeps.size(),
                     "securityQuestion should require 1 property");
        assertTrue(securityDeps.contains("securityAnswer"),
                   "securityQuestion should require securityAnswer");
    }

    // ============== Mixed Required + DependentRequired Tests ==============

    @Test
    void testMixedRequiredAndDependentRequired() {
        SchemaDefinition schema = parser.parse("src/main/resources/dependent-required-mixed.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertTrue(objectType.required().isPresent(),
                   "Should have regular required fields");
        assertTrue(objectType.dependentRequired().isPresent(),
                   "Should have dependent required fields");
    }

    @Test
    void testMixedRegularRequired() {
        SchemaDefinition schema = parser.parse("src/main/resources/dependent-required-mixed.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        List<String> required = objectType.required().get();
        assertEquals(2, required.size(), "Should have 2 regular required fields");
        assertTrue(required.contains("name"), "name should be required");
        assertTrue(required.contains("email"), "email should be required");
    }

    @Test
    void testMixedDependentRequired() {
        SchemaDefinition schema = parser.parse("src/main/resources/dependent-required-mixed.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, List<String>> deps = objectType.dependentRequired().get();
        assertEquals(1, deps.size(), "Should have 1 dependent requirement");
        assertTrue(deps.containsKey("sameAsBilling"),
                   "Should have sameAsBilling dependency");

        List<String> sameAsBillingDeps = deps.get("sameAsBilling");
        assertEquals(1, sameAsBillingDeps.size(),
                     "sameAsBilling should require 1 property");
        assertTrue(sameAsBillingDeps.contains("billingAddress"),
                   "sameAsBilling should require billingAddress");
    }

    // ============== Edge Cases ==============

    @Test
    void testSchemaWithoutDependentRequired() {
        SchemaDefinition schema = parser.parse("src/main/resources/simple-string-props.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertFalse(objectType.dependentRequired().isPresent(),
                    "Schema without dependentRequired should return empty Optional");
    }

    // ============== Workflow.yaml Integration Test ==============

    @Test
    void testWorkflowSchemaDependentRequired() {
        SchemaDefinition schema = parser.parse("src/main/resources/workflow.yaml");
        assertNotNull(schema, "workflow.yaml should parse successfully");

        // workflow.yaml may or may not use dependentRequired
        // This test verifies the parser can handle complex real-world schemas
        ObjectType model = (ObjectType) schema.model();
        assertNotNull(model, "workflow model should be created");
    }
}
