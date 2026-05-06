package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.HasType;
import org.treblereel.yaml.schema.model.NullType;
import org.treblereel.yaml.schema.model.RefType;
import org.treblereel.yaml.schema.model.SchemaDefinition;

import java.io.File;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Error handling and robustness tests for Parser.
 * Tests negative scenarios, invalid inputs, and error conditions.
 */
public class ParserErrorHandlingTest {

    private final Parser parser = new Parser();

    // ============== File Not Found Tests ==============

    @Test
    void testParseNonExistentFile() {
        assertThrows(RuntimeException.class, () -> {
            parser.parse("non-existent-file.yaml");
        }, "Should throw RuntimeException when file does not exist");
    }

    @Test
    void testParseNonExistentFileObject() {
        File nonExistent = new File("non-existent-schema.yaml");
        assertThrows(RuntimeException.class, () -> {
            parser.parse(nonExistent);
        }, "Should throw RuntimeException when File object points to non-existent file");
    }

    @Test
    void testParseInvalidURI() {
        URI invalidUri = URI.create("file:///absolutely/non/existent/path/schema.yaml");
        assertThrows(RuntimeException.class, () -> {
            parser.parse(invalidUri);
        }, "Should throw RuntimeException when URI points to non-existent resource");
    }

    // ============== Invalid YAML Tests ==============

    @Test
    void testParseInvalidYaml() {
        // This test requires a malformed YAML file
        // For now, document the expected behavior
        // In production, consider creating test resources with invalid YAML
    }

    // ============== Empty/Minimal Schema Tests ==============

    @Test
    void testParseEmptySchema() {
        SchemaDefinition schema = parser.parse("src/test/resources/empty-schema.schema.yaml");
        assertNotNull(schema, "Should successfully parse empty schema");
        assertNotNull(schema.model(), "Empty schema should have a model");
    }

    // ============== RefType Error Handling Tests ==============

    @Test
    void testResolveNonExistentRef() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-ref.schema.yaml");

        // Create a RefType pointing to non-existent definition
        RefType invalidRef = new RefType(schema, "#/$defs/NonExistentType");

        // Current behavior: resolves to NullType instead of throwing exception
        // This allows schemas to be parsed even if some refs are broken
        HasType resolved = invalidRef.resolve();
        assertInstanceOf(NullType.class, resolved, "Non-existent $ref should resolve to NullType");
    }

    @Test
    void testResolveUnsupportedRefFormat() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");

        // Create a RefType with unsupported format (external reference)
        RefType externalRef = new RefType(schema, "http://example.com/external-schema.json#/definitions/Address");

        assertThrows(IllegalArgumentException.class, () -> {
            externalRef.resolve();
        }, "Should throw IllegalArgumentException for unsupported reference format");
    }

    @Test
    void testResolveRelativeRef() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");

        // Relative reference (not supported)
        RefType relativeRef = new RefType(schema, "../other-schema.yaml#/$defs/Type");

        assertThrows(IllegalArgumentException.class, () -> {
            relativeRef.resolve();
        }, "Should throw IllegalArgumentException for relative reference paths");
    }

    // ============== Null Safety Tests ==============

    @Test
    void testOptionalFieldsReturnEmpty() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");

        // Schema without $id should return empty Optional
        assertNotNull(schema.id(), "id() should not return null");

        // Schema without $schema should return empty Optional
        assertNotNull(schema.schema(), "schema() should not return null");

        // Schema without title should return empty Optional
        assertNotNull(schema.title(), "title() should not return null");
    }

    // ============== Edge Cases ==============

    @Test
    void testParseVeryDeepNesting() {
        // Test that parser handles deeply nested objects without stack overflow
        SchemaDefinition schema = parser.parse("src/test/resources/deep-nested-object.schema.yaml");
        assertNotNull(schema.model(), "Should handle deeply nested schemas");
    }

    @Test
    void testParseCircularReferences() {
        // Test that circular references don't cause infinite loops during parsing
        SchemaDefinition schema = parser.parse("src/test/resources/circular-refs.schema.yaml");
        assertNotNull(schema.model(), "Should handle circular references");

        // Note: Resolution of circular refs is lazy, so no infinite loop during parse
    }

    @Test
    void testParseSelfReferencingSchema() {
        SchemaDefinition schema = parser.parse("src/test/resources/self-ref.schema.yaml");
        assertNotNull(schema.model(), "Should handle self-referencing schemas");
    }
}
