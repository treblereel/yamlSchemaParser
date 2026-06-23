package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents an anyOf combinator schema.
 * <p>
 * The anyOf combinator requires that an instance validates against at least one of the schemas
 * in the list. This is commonly used in OpenAPI specifications for polymorphic types where
 * a value can match one or more of the specified schemas.
 * </p>
 *
 * @param schema the parent schema definition
 * @param node the JSON node representing this anyOf combinator
 * @author Dmitrii Tikhomirov
 */
public record AnyOfType(SchemaDefinition schema, JsonNode node) implements HasType {

  /**
   * Returns the list of schemas where at least one must be satisfied.
   *
   * @return list of schemas (never null, but may be empty if anyOf is not present)
   */
  public List<HasType> getAnyOf() {
    List<HasType> anyOfTypes = new ArrayList<>();
    for (JsonNode anyOfNode : node.get("anyOf")) {
      anyOfTypes.add(NodeFactory.resolveType(schema, anyOfNode));
    }
    return anyOfTypes;
  }

  /**
   * Returns the unevaluatedProperties constraint.
   *
   * @return the unevaluatedProperties value if specified
   */
  public Optional<Boolean> unevaluatedProperties() {
    return Optional.ofNullable(node.get("unevaluatedProperties")).map(JsonNode::asBoolean);
  }

  @Override
  public JsonNode getRawNode() {
    return node;
  }

  @Override
  public SchemaDefinition getSchema() {
    return schema;
  }
}
