package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents an allOf combinator schema.
 * <p>
 * The allOf combinator requires that an instance validates against ALL schemas in the list.
 * This is commonly used for schema composition and inheritance, where an instance must satisfy
 * multiple schema constraints simultaneously.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * AllOfType allOfType = (AllOfType) schema.model();
 * List<HasType> schemas = allOfType.getAllOf();
 * for (HasType type : schemas) {
 *     // Each type represents a constraint that must be satisfied
 *     ObjectType obj = (ObjectType) type;
 *     // Merge properties, required fields, etc.
 * }
 * }</pre>
 * </p>
 *
 * @param schema the parent schema definition
 * @param node the JSON node representing this allOf combinator
 * @author Dmitrii Tikhomirov
 */
public record AllOfType(SchemaDefinition schema, JsonNode node) implements HasType {

  /**
   * Returns the list of schemas that must all be satisfied.
   * <p>
   * Each schema in the list is a constraint that the instance must validate against.
   * Common use cases include:
   * </p>
   * <ul>
   *   <li>Combining a base schema ($ref) with additional properties</li>
   *   <li>Merging multiple property sets</li>
   *   <li>Applying multiple constraint layers</li>
   * </ul>
   *
   * @return list of schemas (never null, but may be empty if allOf is not present)
   */
  public List<HasType> getAllOf() {
    List<HasType> allOfTypes = new ArrayList<>();
    for (JsonNode allOfNode : node.get("allOf")) {
      allOfTypes.add(NodeFactory.resolveType(schema, allOfNode));
    }
    return allOfTypes;
  }

  /**
   * Returns the unevaluatedProperties constraint.
   * <p>
   * When false, prohibits properties that are not defined in any of the allOf schemas.
   * </p>
   *
   * @return the unevaluatedProperties value if specified
   */
  public Optional<Boolean> unevaluatedProperties() {
    return Optional.ofNullable(node.get("unevaluatedProperties")).map(JsonNode::asBoolean);
  }

  /**
   * Returns the title of this allOf combinator.
   *
   * @return the title if present
   */
  public Optional<String> title() {
    return Optional.ofNullable(node.get("title")).map(JsonNode::asText);
  }

  /**
   * Returns the raw JsonNode for this allOf type.
   * Used by default extension metadata methods to access x- prefixed properties.
   *
   * @return the underlying JsonNode
   */
  @Override
  public JsonNode getRawNode() {
    return node;
  }

  /**
   * Returns the description of this allOf combinator.
   *
   * @return the description if present
   */
  public Optional<String> description() {
    return Optional.ofNullable(node.get("description")).map(JsonNode::asText);
  }
}
