package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for patternProperties keyword.
 * patternProperties allows defining schemas for object properties whose names match regex patterns.
 */
public class PatternPropertiesTest {

    private final Parser parser = new Parser();

    // ============== Basic patternProperties Tests ==============

    @Test
    void testPatternPropertiesPresent() {
        SchemaDefinition schema = parser.parse("src/test/resources/pattern-properties-simple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertTrue(objectType.patternProperties().isEmpty() == false,
                   "patternProperties should be present");
    }

    @Test
    void testPatternPropertiesMapSize() {
        SchemaDefinition schema = parser.parse("src/test/resources/pattern-properties-simple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> patterns = objectType.patternProperties();
        assertEquals(2, patterns.size(), "Should have 2 pattern properties");
    }

    @Test
    void testPatternPropertiesKeys() {
        SchemaDefinition schema = parser.parse("src/test/resources/pattern-properties-simple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> patterns = objectType.patternProperties();

        assertTrue(patterns.containsKey("^s_"), "Should contain pattern ^s_");
        assertTrue(patterns.containsKey("^i_"), "Should contain pattern ^i_");
    }

    @Test
    void testPatternPropertiesStringType() {
        SchemaDefinition schema = parser.parse("src/test/resources/pattern-properties-simple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> patterns = objectType.patternProperties();
        HasType stringPattern = patterns.get("^s_");

        assertInstanceOf(StringType.class, stringPattern,
                         "Pattern ^s_ should map to StringType");
    }

    @Test
    void testPatternPropertiesIntegerType() {
        SchemaDefinition schema = parser.parse("src/test/resources/pattern-properties-simple.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> patterns = objectType.patternProperties();
        HasType integerPattern = patterns.get("^i_");

        assertInstanceOf(IntegerType.class, integerPattern,
                         "Pattern ^i_ should map to IntegerType");
    }

    // ============== Mixed properties + patternProperties Tests ==============

    @Test
    void testMixedPropertiesAndPatterns() {
        SchemaDefinition schema = parser.parse("src/test/resources/pattern-properties-mixed.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertTrue(objectType.properties().isEmpty() == false,
                   "Should have regular properties");
        assertTrue(objectType.patternProperties().isEmpty() == false,
                   "Should have pattern properties");
    }

    @Test
    void testMixedRegularProperties() {
        SchemaDefinition schema = parser.parse("src/test/resources/pattern-properties-mixed.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> props = objectType.properties();

        assertTrue(props.containsKey("name"), "Should have name property");
        assertTrue(props.containsKey("age"), "Should have age property");
        assertEquals(2, props.size(), "Should have exactly 2 regular properties");
    }

    @Test
    void testMixedPatternProperties() {
        SchemaDefinition schema = parser.parse("src/test/resources/pattern-properties-mixed.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> patterns = objectType.patternProperties();

        assertTrue(patterns.containsKey("^config_"), "Should have ^config_ pattern");
        assertTrue(patterns.containsKey("^metadata_"), "Should have ^metadata_ pattern");
        assertEquals(2, patterns.size(), "Should have exactly 2 pattern properties");
    }

    @Test
    void testMixedConfigPatternStructure() {
        SchemaDefinition schema = parser.parse("src/test/resources/pattern-properties-mixed.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> patterns = objectType.patternProperties();
        HasType configPattern = patterns.get("^config_");

        assertInstanceOf(ObjectType.class, configPattern,
                         "config_ pattern should be ObjectType");

        ObjectType configObject = (ObjectType) configPattern;
        assertTrue(configObject.properties().isEmpty() == false,
                   "config object should have properties");

        Map<String, HasType> configProps = configObject.properties();
        assertTrue(configProps.containsKey("enabled"),
                   "config should have enabled property");
        assertInstanceOf(BooleanType.class, configProps.get("enabled"),
                         "enabled should be boolean");
    }

    @Test
    void testMixedMetadataPatternStructure() {
        SchemaDefinition schema = parser.parse("src/test/resources/pattern-properties-mixed.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> patterns = objectType.patternProperties();
        HasType metadataPattern = patterns.get("^metadata_");

        assertInstanceOf(StringType.class, metadataPattern,
                         "metadata_ pattern should be StringType");
    }

    // ============== Complex Pattern Tests ==============

    @Test
    void testComplexPatterns() {
        SchemaDefinition schema = parser.parse("src/test/resources/pattern-properties-complex.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> patterns = objectType.patternProperties();
        assertEquals(3, patterns.size(), "Should have 3 complex patterns");
    }

    @Test
    void testComplexCaseInsensitivePattern() {
        SchemaDefinition schema = parser.parse("src/test/resources/pattern-properties-complex.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> patterns = objectType.patternProperties();

        assertTrue(patterns.containsKey("^[Ss]_"),
                   "Should have case-insensitive string pattern");

        HasType stringPattern = patterns.get("^[Ss]_");
        assertInstanceOf(StringType.class, stringPattern);

        StringType stringType = (StringType) stringPattern;
        assertTrue(stringType.minLength().isPresent(),
                   "String pattern should have minLength constraint");
        assertEquals(1, stringType.minLength().get(),
                     "minLength should be 1");
    }

    @Test
    void testComplexNumberPattern() {
        SchemaDefinition schema = parser.parse("src/test/resources/pattern-properties-complex.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> patterns = objectType.patternProperties();

        assertTrue(patterns.containsKey("^[Nn]um_"),
                   "Should have number pattern");

        HasType numberPattern = patterns.get("^[Nn]um_");
        assertInstanceOf(NumberType.class, numberPattern);

        NumberType numberType = (NumberType) numberPattern;
        assertTrue(numberType.minimum().isPresent(),
                   "Number pattern should have minimum constraint");
        assertEquals(0.0, numberType.minimum().get(),
                     "minimum should be 0");
    }

    @Test
    void testComplexArrayPattern() {
        SchemaDefinition schema = parser.parse("src/test/resources/pattern-properties-complex.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        Map<String, HasType> patterns = objectType.patternProperties();

        assertTrue(patterns.containsKey("^arr_.*$"),
                   "Should have array pattern");

        HasType arrayPattern = patterns.get("^arr_.*$");
        assertInstanceOf(ArrayType.class, arrayPattern);

        ArrayType arrayType = (ArrayType) arrayPattern;
        ArrayItemType[] items = arrayType.getItems();
        assertEquals(1, items.length, "Array should have single item type");
        assertInstanceOf(StringType.class, items[0].getType(),
                         "Array items should be strings");
    }

    // ============== Edge Cases ==============

    @Test
    void testSchemaWithoutPatternProperties() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertFalse(objectType.patternProperties().isEmpty() == false,
                    "Schema without patternProperties should return empty Optional");
    }

    @Test
    void testPatternPropertiesWithAdditionalProperties() {
        SchemaDefinition schema = parser.parse("src/test/resources/pattern-properties-mixed.schema.yaml");
        ObjectType objectType = (ObjectType) schema.model();

        assertTrue(objectType.patternProperties().isEmpty() == false,
                   "Should have pattern properties");
        assertTrue(objectType.additionalProperties().isPresent(),
                   "Should have additionalProperties set to false");
    }

    // ============== Workflow.yaml Integration Test ==============

    @Test
    void testWorkflowSchemaPatternProperties() {
        SchemaDefinition schema = parser.parse("src/test/resources/workflow.yaml");
        assertNotNull(schema, "workflow.yaml should parse successfully");

        // workflow.yaml may or may not use patternProperties
        // This test verifies the parser can handle complex real-world schemas
        HasType model = schema.model();
        assertNotNull(model, "workflow model should be created");
    }
}
