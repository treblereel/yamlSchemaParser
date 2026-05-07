package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
 * Map<String, HasType> props = userType.properties();
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
   * Returns all properties defined in this object schema.
   * <p>
   * The map preserves the order as defined in the schema (insertion order).
   * </p>
   *
   * @return Map of property names to their types, empty map if no properties defined
   */
  public Map<String, HasType> properties() {
    return Optional.ofNullable(node.get("properties"))
            .filter(JsonNode::isObject)
            .map(propsNode -> {
              Map<String, HasType> properties = new LinkedHashMap<>();
              propsNode.fields().forEachRemaining(entry ->
                      properties.put(entry.getKey(), NodeFactory.resolveType(schema, entry.getValue())));
              return properties;
            })
            .orElse(Map.of());
  }

  /**
   * Returns the raw JsonNode for this object type.
   * Used by default extension metadata methods to access x- prefixed properties.
   *
   * @return the underlying JsonNode
   */
  @Override
  public JsonNode getRawNode() {
    return node;
  }

  /**
   * Checks if a property with the given name exists.
   *
   * @param name property name
   * @return true if property exists, false otherwise
   */
  public boolean hasProperty(String name) {
    return properties().containsKey(name);
  }

  /**
   * Returns the type of a property by name.
   *
   * @param name property name
   * @return Optional containing the property type, or empty if property doesn't exist
   */
  public Optional<HasType> getProperty(String name) {
    return Optional.ofNullable(properties().get(name));
  }

  /**
   * Returns the property as StringType if it exists and is a StringType.
   *
   * @param name property name
   * @return Optional containing StringType, or empty if property doesn't exist or is not StringType
   */
  public Optional<StringType> getPropertyAsString(String name) {
    return Optional.ofNullable(properties().get(name))
            .filter(StringType.class::isInstance)
            .map(StringType.class::cast);
  }

  /**
   * Returns the property as ObjectType if it exists and is an ObjectType.
   *
   * @param name property name
   * @return Optional containing ObjectType, or empty if property doesn't exist or is not ObjectType
   */
  public Optional<ObjectType> getPropertyAsObject(String name) {
    return Optional.ofNullable(properties().get(name))
            .filter(ObjectType.class::isInstance)
            .map(ObjectType.class::cast);
  }

  /**
   * Returns the property as ArrayType if it exists and is an ArrayType.
   *
   * @param name property name
   * @return Optional containing ArrayType, or empty if property doesn't exist or is not ArrayType
   */
  public Optional<ArrayType> getPropertyAsArray(String name) {
    return Optional.ofNullable(properties().get(name))
            .filter(ArrayType.class::isInstance)
            .map(ArrayType.class::cast);
  }

  /**
   * Returns the property as IntegerType if it exists and is an IntegerType.
   *
   * @param name property name
   * @return Optional containing IntegerType, or empty if property doesn't exist or is not IntegerType
   */
  public Optional<IntegerType> getPropertyAsInteger(String name) {
    return Optional.ofNullable(properties().get(name))
            .filter(IntegerType.class::isInstance)
            .map(IntegerType.class::cast);
  }

  /**
   * Returns the property as NumberType if it exists and is a NumberType.
   *
   * @param name property name
   * @return Optional containing NumberType, or empty if property doesn't exist or is not NumberType
   */
  public Optional<NumberType> getPropertyAsNumber(String name) {
    return Optional.ofNullable(properties().get(name))
            .filter(NumberType.class::isInstance)
            .map(NumberType.class::cast);
  }

  /**
   * Returns the property as BooleanType if it exists and is a BooleanType.
   *
   * @param name property name
   * @return Optional containing BooleanType, or empty if property doesn't exist or is not BooleanType
   */
  public Optional<BooleanType> getPropertyAsBoolean(String name) {
    return Optional.ofNullable(properties().get(name))
            .filter(BooleanType.class::isInstance)
            .map(BooleanType.class::cast);
  }

  /**
   * Returns a PropertyNavigator for fluent navigation to a property.
   * Entry point for fluent schema navigation.
   *
   * Example:
   * <pre>
   * Optional<String> pattern = root.property("address")
   *     .property("city")
   *     .asString()
   *     .flatMap(StringType::pattern);
   * </pre>
   *
   * @param name property name
   * @return PropertyNavigator for the property, or empty navigator if property doesn't exist
   */
  public PropertyNavigator property(String name) {
    return new PropertyNavigatorImpl(
        Optional.ofNullable(properties().get(name)),
        false
    );
  }

  /**
   * Returns property stream for iteration.
   *
   * @return Stream of map entries (property name -> type)
   */
  public java.util.stream.Stream<Map.Entry<String, HasType>> propertiesStream() {
    return properties().entrySet().stream();
  }

  /**
   * Returns pattern properties - regex-based property schemas.
   * <p>
   * Pattern properties allow defining schemas for object properties whose names
   * match specific regex patterns. The map is sorted alphabetically by pattern.
   * </p>
   * <p>
   * Example:
   * <pre>{@code
   * ObjectType objectType = (ObjectType) schema.model();
   * Map<String, HasType> patterns = objectType.patternProperties();
   * HasType stringPattern = patterns.get("^s_"); // Properties starting with s_
   * }</pre>
   * </p>
   *
   * @return Map of regex patterns to their schemas, empty map if none defined
   */
  public Map<String, HasType> patternProperties() {
    return Optional.ofNullable(node.get("patternProperties"))
            .filter(JsonNode::isObject)
            .map(patternsNode -> {
              Map<String, HasType> patterns = new TreeMap<>(String::compareTo);
              patternsNode.fields().forEachRemaining(entry ->
                      patterns.put(entry.getKey(), NodeFactory.resolveType(schema, entry.getValue())));
              return patterns;
            })
            .orElse(Map.of());
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
   * Returns dependent required fields - conditional required based on property presence.
   * <p>
   * Specifies that if a property (key) is present, then the properties in its
   * associated list (value) must also be present. This enables conditional
   * property requirements based on the presence of other properties.
   * </p>
   * <p>
   * Example:
   * <pre>{@code
   * ObjectType objectType = (ObjectType) schema.model();
   * Map<String, List<String>> deps = objectType.dependentRequired();
   * List<String> required = deps.get("creditCard");
   * // If creditCard is present, these properties must also be present
   * }</pre>
   * </p>
   *
   * @return Map of property names to lists of dependent required fields, empty map if none
   */
  public Map<String, List<String>> dependentRequired() {
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
            })
            .orElse(Map.of());
  }

  /**
   * Returns dependent schemas - conditional schemas based on property presence.
   * <p>
   * Specifies that if a property (key) is present, then the associated schema (value)
   * must also be applied to the instance. This enables conditional schema application
   * based on the presence of properties.
   * </p>
   * <p>
   * Example:
   * <pre>{@code
   * ObjectType objectType = (ObjectType) schema.model();
   * Map<String, HasType> deps = objectType.dependentSchemas();
   * HasType schema = deps.get("creditCard");
   * // If creditCard is present, this schema must also be applied
   * }</pre>
   * </p>
   *
   * @return Map of property names to dependent schemas, empty map if none defined
   */
  public Map<String, HasType> dependentSchemas() {
    return Optional.ofNullable(node.get("dependentSchemas"))
            .filter(JsonNode::isObject)
            .map(schemasNode -> {
              Map<String, HasType> schemas = new TreeMap<>(String::compareTo);
              schemasNode.fields().forEachRemaining(entry ->
                      schemas.put(entry.getKey(), NodeFactory.resolveType(schema, entry.getValue())));
              return schemas;
            })
            .orElse(Map.of());
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
   * Returns the description of this object type.
   *
   * @return the description if present
   */
  public Optional<String> description() {
    return Optional.ofNullable(node.get("description")).map(JsonNode::asText);
  }

  /**
   * Returns the deprecated flag (JSON Schema Draft 2019-09+).
   * <p>
   * When true, indicates this schema is deprecated and should not be used.
   * This is metadata only and does not affect validation.
   * </p>
   *
   * @return true if deprecated, false if not deprecated, empty if not specified
   */
  public Optional<Boolean> deprecated() {
    return Optional.ofNullable(node.get("deprecated"))
            .filter(JsonNode::isBoolean)
            .map(JsonNode::booleanValue);
  }

  /**
   * Returns the readOnly flag (JSON Schema Draft 7+, OpenAPI).
   * <p>
   * When true, indicates this property should only appear in responses,
   * not in requests. Commonly used for server-generated fields like IDs
   * and timestamps.
   * </p>
   *
   * @return true if read-only, false if not, empty if not specified
   */
  public Optional<Boolean> readOnly() {
    return Optional.ofNullable(node.get("readOnly"))
            .filter(JsonNode::isBoolean)
            .map(JsonNode::booleanValue);
  }

  /**
   * Returns the writeOnly flag (JSON Schema Draft 7+, OpenAPI).
   * <p>
   * When true, indicates this property should only appear in requests,
   * not in responses. Commonly used for sensitive fields like passwords.
   * </p>
   *
   * @return true if write-only, false if not, empty if not specified
   */
  public Optional<Boolean> writeOnly() {
    return Optional.ofNullable(node.get("writeOnly"))
            .filter(JsonNode::isBoolean)
            .map(JsonNode::booleanValue);
  }

  /**
   * Returns the examples array (JSON Schema Draft 6+).
   * <p>
   * Provides example values for documentation purposes. These are metadata
   * only and do not affect validation.
   * </p>
   *
   * @return list of example values if specified
   */
  public Optional<List<JsonNode>> examples() {
    return Optional.ofNullable(node.get("examples"))
            .filter(JsonNode::isArray)
            .map(examplesNode -> {
              List<JsonNode> examples = new ArrayList<>();
              examplesNode.forEach(examples::add);
              return examples;
            });
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
