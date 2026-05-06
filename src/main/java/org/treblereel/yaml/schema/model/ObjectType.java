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
   * Returns the pattern properties as a map of regex patterns to their types.
   * <p>
   * Pattern properties allow defining schemas for object properties whose names
   * match specific regex patterns. The map is sorted alphabetically by pattern.
   * </p>
   * <p>
   * Example:
   * <pre>{@code
   * ObjectType objectType = (ObjectType) schema.model();
   * Map<String, HasType> patterns = objectType.patternProperties().get();
   * HasType stringPattern = patterns.get("^s_"); // Properties starting with s_
   * }</pre>
   * </p>
   *
   * @return map of regex patterns to types if patternProperties are defined
   */
  public Optional<Map<String, HasType>> patternProperties() {
    return Optional.ofNullable(node.get("patternProperties"))
            .filter(JsonNode::isObject)
            .map(patternsNode -> {
              Map<String, HasType> patterns = new TreeMap<>(String::compareTo);
              patternsNode.fields().forEachRemaining(entry ->
                      patterns.put(entry.getKey(), NodeFactory.resolveType(schema, entry.getValue())));
              return patterns;
            });
  }

  /**
   * Returns the property names schema.
   * <p>
   * Defines a schema that ALL property names must validate against.
   * This applies to all properties: explicitly defined properties, pattern properties,
   * and additional properties.
   * </p>
   * <p>
   * Example:
   * <pre>{@code
   * ObjectType objectType = (ObjectType) schema.model();
   * if (objectType.propertyNames().isPresent()) {
   *     HasType nameSchema = objectType.propertyNames().get();
   *     // All property names must validate against this schema
   * }
   * }</pre>
   * </p>
   *
   * @return the property names validation schema if specified
   */
  public Optional<HasType> propertyNames() {
    return Optional.ofNullable(node.get("propertyNames"))
            .map(nameSchema -> NodeFactory.resolveType(schema, nameSchema));
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
   * Returns the dependent required constraints.
   * <p>
   * Specifies that if a property (key) is present, then the properties in its
   * associated list (value) must also be present. This enables conditional
   * property requirements based on the presence of other properties.
   * </p>
   * <p>
   * Example:
   * <pre>{@code
   * ObjectType objectType = (ObjectType) schema.model();
   * if (objectType.dependentRequired().isPresent()) {
   *     Map<String, List<String>> deps = objectType.dependentRequired().get();
   *     List<String> required = deps.get("creditCard");
   *     // If creditCard is present, these properties must also be present
   * }
   * }</pre>
   * </p>
   *
   * @return map of property names to their dependent required properties if specified
   */
  public Optional<Map<String, List<String>>> dependentRequired() {
    return Optional.ofNullable(node.get("dependentRequired"))
            .filter(JsonNode::isObject)
            .map(depsNode -> {
              Map<String, List<String>> deps = new TreeMap<>(String::compareTo);
              depsNode.fields().forEachRemaining(entry -> {
                String propertyName = entry.getKey();
                JsonNode requiredArray = entry.getValue();
                if (requiredArray.isArray()) {
                  List<String> requiredProps = new ArrayList<>();
                  requiredArray.forEach(prop -> requiredProps.add(prop.asText()));
                  deps.put(propertyName, requiredProps);
                }
              });
              return deps;
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

  /**
   * Returns the if condition schema.
   * <p>
   * If present, this schema is evaluated first. If the instance validates against
   * the if schema, the then schema is applied; otherwise, the else schema is applied.
   * </p>
   *
   * @return the if condition schema if present
   */
  public Optional<HasType> ifCondition() {
    return applicators.getIf();
  }

  /**
   * Returns the then schema.
   * <p>
   * Applied when the instance validates against the if schema.
   * </p>
   *
   * @return the then schema if present
   */
  public Optional<HasType> thenSchema() {
    return applicators.getThen();
  }

  /**
   * Returns the else schema.
   * <p>
   * Applied when the instance does NOT validate against the if schema.
   * </p>
   *
   * @return the else schema if present
   */
  public Optional<HasType> elseSchema() {
    return applicators.getElse();
  }

}
