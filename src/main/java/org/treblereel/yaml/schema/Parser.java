package org.treblereel.yaml.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.treblereel.yaml.schema.model.SchemaDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Main entry point for parsing YAML schema files.
 * <p>
 * This parser converts YAML schema definitions (JSON Schema in YAML format) into a strongly-typed
 * Java object model that provides convenient API for schema analysis.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * Parser parser = new Parser();
 * SchemaDefinition schema = parser.parse("path/to/schema.yaml");
 * ObjectType root = (ObjectType) schema.model();
 * }</pre>
 * </p>
 *
 * @author Dmitrii Tikhomirov
 * @see SchemaDefinition
 */
public class Parser {

    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    /**
     * Loads a YAML schema from the given URI.
     *
     * @param uri the URI to load the schema from
     * @return the parsed JSON node representing the schema
     * @throws RuntimeException if an I/O error occurs during loading
     */
    private JsonNode load(URI uri) {
        try (InputStream in = uri.toURL().openStream()) {
            return mapper.readTree(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses a YAML schema from a file path.
     *
     * @param file the file path as a string
     * @return the parsed schema definition
     * @throws RuntimeException if the file cannot be read or parsed
     */
    public SchemaDefinition parse(String file) {
        return parse(new File(file));
    }

    /**
     * Parses a YAML schema from a File object.
     *
     * @param file the file to parse
     * @return the parsed schema definition
     * @throws RuntimeException if the file cannot be read or parsed
     */
    public SchemaDefinition parse(File file) {
        return parse(file.toURI());
    }

    /**
     * Parses a YAML schema from a URI.
     *
     * @param uri the URI to parse
     * @return the parsed schema definition
     * @throws RuntimeException if the URI cannot be accessed or parsed
     */
    public SchemaDefinition parse(URI uri) {
        JsonNode root = load(uri);
        return new SchemaDefinition(root);
    }
}
