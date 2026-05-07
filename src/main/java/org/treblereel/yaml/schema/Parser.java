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
    private final boolean strictMode;
    private final boolean resolveReferences;

    /**
     * Creates a Parser with default configuration.
     */
    public Parser() {
        this(builder());
    }

    /**
     * Creates a Parser from a Builder.
     * Package-private constructor - use builder() or default constructor.
     */
    private Parser(Builder builder) {
        this.strictMode = builder.strictMode;
        this.resolveReferences = builder.resolveReferences;
    }

    /**
     * Creates a new Parser.Builder for configurable parsing.
     *
     * @return new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for creating configured Parser instances.
     */
    public static class Builder {
        private boolean strictMode = false;
        private boolean resolveReferences = true;

        /**
         * Enables strict mode - throws exception on unknown fields/types.
         * Default: false
         *
         * @param value true to enable strict mode
         * @return this builder for chaining
         */
        public Builder strictMode(boolean value) {
            this.strictMode = value;
            return this;
        }

        /**
         * Enables automatic reference resolution during parsing.
         * Default: true
         *
         * @param value true to resolve references automatically
         * @return this builder for chaining
         */
        public Builder resolveReferences(boolean value) {
            this.resolveReferences = value;
            return this;
        }

        /**
         * Builds the Parser with configured options.
         *
         * @return new Parser instance
         */
        public Parser build() {
            return new Parser(this);
        }
    }

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

    /**
     * Parses YAML schema from a string (for testing).
     *
     * @param yamlContent the YAML content as a string
     * @return the parsed schema definition
     * @throws RuntimeException if the YAML cannot be parsed
     */
    public SchemaDefinition parseYaml(String yamlContent) {
        try {
            JsonNode root = mapper.readTree(yamlContent);
            return new SchemaDefinition(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
