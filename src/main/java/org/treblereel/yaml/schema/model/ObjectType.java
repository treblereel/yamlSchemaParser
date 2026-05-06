package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Represents an object type schema.
 * <p>
 * Provides access to object properties, required fields, additional properties configuration,
 * property count constraints, and schema combinators (anyOf, oneOf, allOf).
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * ObjectType userType = (ObjectType) schema.model();
 * Map<String, HasType> props = userType.properties().get();
 * List<String> required = userType.required().get();
 * boolean nameRequired = required.contains("name");
 * }</pre>
 * </p>
 *
 * @author Dmitrii Tikhomirov
 */
public class ObjectType implements HasType {

  private final SchemaDefinition schema;
  private final JsonNode node;

  private final Applicators applicators;

  /**
   * Constructs an ObjectType from a schema definition and JSON node.
   *
   * @param schema the parent schema definition
   * @param node the JSON node representing this object type
   */
  public ObjectType(SchemaDefinition schema, JsonNode node) {
    this.schema = schema;
    this.node = node;
    applicators = new Applicators(schema, node);
  }

  /**
   * Returns the unevaluatedProperties constraint.
   * <p>
   * When false, prohibits properties that are not defined in the schema.
   * </p>
   *
   * @return the unevaluatedProperties value if specified
   */
  public Optional<Boolean> unevaluatedProperties() {
    return Optional.ofNullable(node.get("unevaluatedProperties")).map(JsonNode::asBoolean);
  }

  /**
   * Returns the oneOf combinator schemas.
   * <p>
   * The instance must validate against exactly one of the schemas in the list.
   * </p>
   *
   * @return list of oneOf schemas if present
   */
  public Optional<List<HasType>> oneOf() {
    return applicators.oneOf();
  }

  /**
   * Returns the anyOf combinator schemas.
   * <p>
   * The instance must validate against at least one of the schemas in the list.
   * </p>
   *
   * @return list of anyOf schemas if present
   */
  public Optional<List<HasType>> anyOf() {
    return applicators.anyOf();
  }

  /**
   * Returns the allOf combinator schemas.
   * <p>
   * The instance must validate against all schemas in the list.
   * </p>
   *
   * @return list of allOf schemas if present
   */
  public Optional<List<HasType>> allOf() {
    return applicators.allOf();
  }

  /**
   * Returns the object properties as a map of property names to their types.
   * <p>
   * The map is sorted alphabetically by property name.
   * </p>
   *
   * @return map of property names to types if properties are defined
   */
  public Optional<Map<String, HasType>> properties() {
    return Optional.ofNullable(node.get("properties"))
            .filter(JsonNode::isObject)
            .map(propsNode -> {
              Map<String, HasType> properties = new TreeMap<>(String::compareTo);
              propsNode.fields().forEachRemaining(entry ->
                      properties.put(entry.getKey(), NodeFactory.resolveType(schema, entry.getValue())));
              return properties;
            });
  }

  /**
   * Returns the additional properties configuration.
   * <p>
   * Determines whether properties not explicitly defined in the schema are allowed,
   * and if so, what type they must conform to.
   * </p>
   *
   * @return the additional properties configuration if specified
   * @see AdditionalProperties
   */
  public Optional<AdditionalProperties> additionalProperties() {
    return Optional.ofNullable(node.get("additionalProperties"))
            .map(n -> new AdditionalProperties(n, schema));
  }

  /**
   * Returns the description of this object type.
   *
   * @return the description if present
   */
  public Optional<String> getDescription() {
    return Optional.ofNullable(node.get("description")).map(JsonNode::asText);
  }

  /**
   * Returns the title of this object type.
   *
   * @return the title if present
   */
  public Optional<String> getTitle() {
    return Optional.ofNullable(node.get("title")).map(JsonNode::asText);
  }

  /**
   * Returns the list of required property names.
   * <p>
   * Properties in this list must be present in valid instances.
   * </p>
   *
   * @return list of required property names if specified
   */
  public Optional<List<String>> required() {
    return Optional.ofNullable(node.get("required")).filter(n -> n.isArray())
            .map(array -> {
              List<String> required = new ArrayList<>();
              array.forEach(p -> required.add(p.asText()));
              return required;
            });
  }

  /**
   * Returns the minimum number of properties constraint.
   *
   * @return the minimum number of properties if specified
   */
  public Optional<Integer> minProperties() {
    return Optional.ofNullable(node.get("minProperties"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  /**
   * Returns the maximum number of properties constraint.
   *
   * @return the maximum number of properties if specified
   */
  public Optional<Integer> maxProperties() {
    return Optional.ofNullable(node.get("maxProperties"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  /**
   * Returns the not combinator schema.
   * <p>
   * The instance must NOT validate against this schema.
   * </p>
   *
   * @return the not schema if present
   */
  public Optional<HasType> not() {
    return applicators.getNot();
  }

}
