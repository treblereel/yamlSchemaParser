package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a oneOf combinator schema.
 * <p>
 * The oneOf combinator requires that an instance validates against exactly one of the schemas
 * in the list. This is commonly used in OpenAPI specifications for discriminated unions where
 * a value must match precisely one of the specified schemas.
 * </p>
 *
 * @param schema the parent schema definition
 * @param node the JSON node representing this oneOf combinator
 * @author Dmitrii Tikhomirov
 */
public record OneOfType(SchemaDefinition schema, JsonNode node) implements HasType {

  /**
   * Returns the list of schemas where exactly one must be satisfied.
   *
   * @return list of schemas (never null, but may be empty if oneOf is not present)
   */
  public List<HasType> getOneOf() {
    List<HasType> oneOfTypes = new ArrayList<>();
    for (JsonNode oneOfNode : node.get("oneOf")) {
      oneOfTypes.add(NodeFactory.resolveType(schema, oneOfNode));
    }
    return oneOfTypes;
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
