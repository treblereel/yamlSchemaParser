package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a parsed YAML schema definition.
 * <p>
 * This is the root object returned by the {@link org.treblereel.yaml.schema.Parser} and provides
 * access to schema metadata ($id, $schema, title) as well as the root type model and definitions ($defs).
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * SchemaDefinition schema = parser.parse("schema.yaml");
 * Optional<String> id = schema.id();
 * HasType rootType = schema.model();
 * Map<String, ObjectType> defs = schema.definitions();
 * }</pre>
 * </p>
 *
 * @param node the Jackson JsonNode representing the root schema
 * @author Dmitrii Tikhomirov
 */
public record SchemaDefinition(JsonNode node) {

  /**
   * Returns the schema identifier ($id).
   *
   * @return the schema $id if present
   */
  public Optional<String> id() {
    return Optional.ofNullable(node.get("$id")).filter(JsonNode::isTextual).map(JsonNode::asText);
  }

  /**
   * Returns the JSON Schema version ($schema).
   *
   * @return the $schema URI if present (e.g., "https://json-schema.org/draft/2020-12/schema")
   */
  public Optional<String> schema() {
    return Optional.ofNullable(node.get("$schema")).filter(JsonNode::isTextual).map(JsonNode::asText);
  }

  /**
   * Returns the schema title.
   *
   * @return the schema title if present
   */
  public Optional<String> title() {
    return Optional.ofNullable(node.get("title")).filter(JsonNode::isTextual).map(JsonNode::asText);
  }

  /**
   * Returns the root type of the schema.
   * <p>
   * The returned type can be any of: {@link ObjectType}, {@link ArrayType}, {@link StringType},
   * {@link IntegerType}, {@link NumberType}, {@link BooleanType}, {@link NullType}, {@link RefType},
   * or {@link AllOfType}.
   * </p>
   *
   * @return the root type model
   */
  public HasType model() {
    return NodeFactory.resolveType(this, node);
  }

  /**
   * Returns all schema definitions from the $defs section.
   * <p>
   * These definitions can be referenced using {@link RefType} with paths like "#/$defs/DefinitionName".
   * </p>
   *
   * @return a map of definition names to their ObjectType representations, or an empty map if no definitions exist
   */
  public Map<String, ObjectType> definitions() {
    if (node.get("$defs") == null) {
      return Collections.emptyMap();
    }
    Map<String, ObjectType> results = new HashMap<>();
    JsonNode definitionsNode = node.get("$defs");
    Iterator<Map.Entry<String, JsonNode>> iter = definitionsNode.fields();
    while (iter.hasNext()) {
      Map.Entry<String, JsonNode> entry = iter.next();
      results.put(entry.getKey(), new ObjectType(this, entry.getValue()));
    }
    return results;
  }

}
