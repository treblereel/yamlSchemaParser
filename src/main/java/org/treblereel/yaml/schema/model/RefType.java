package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a $ref reference to another schema definition.
 * <p>
 * References allow schema reuse and circular/recursive definitions. This implementation
 * supports local references within the same schema document using the #/$defs/ pattern.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * RefType addressRef = (RefType) objectType.properties().get("address");
 * String refPath = addressRef.ref(); // "#/$defs/Address"
 * ObjectType addressType = (ObjectType) addressRef.resolve();
 * Map<String, HasType> addressProps = addressType.properties();
 * }</pre>
 * </p>
 *
 * @param schema the parent schema definition containing the $defs
 * @param ref the reference path (e.g., "#/$defs/Address")
 * @author Dmitrii Tikhomirov
 */
public record RefType(SchemaDefinition schema, String ref) implements HasType {

  private static final ThreadLocal<Set<String>> RESOLVING = ThreadLocal.withInitial(HashSet::new);

  /**
   * Resolves the reference and returns the target schema type.
   * <p>
   * The resolved type can be any HasType implementation: ObjectType, StringType, ArrayType,
   * IntegerType, NumberType, BooleanType, NullType, AllOfType, or even another RefType.
   * </p>
   * <p>
   * Currently supports only local $defs references in the format "#/$defs/DefinitionName".
   * Circular references are detected and reported as IllegalStateException.
   * </p>
   *
   * @return the resolved schema type
   * @throws IllegalArgumentException if the reference format is not supported
   * @throws IllegalStateException if a circular $ref cycle is detected
   */
  public HasType resolve() {
    Set<String> active = RESOLVING.get();
    if (!active.add(ref)) {
      throw new SchemaParseException("Circular $ref detected: " + ref, ref);
    }
    try {
      if (ref.startsWith("#/$defs/")) {
        String elementName = ref.substring(8);
        JsonNode node = schema.node().get("$defs").get(elementName);
        return resolveNode(node);
      }
      if (ref.startsWith("#") && !ref.startsWith("#/")) {
        String anchor = ref.substring(1);
        JsonNode node = schema.index().findByAnchor(anchor)
            .orElseThrow(() -> new SchemaParseException("$anchor not found: " + anchor, ref));
        return resolveNode(node);
      }
      if (!ref.startsWith("#")) {
        JsonNode node = schema.index().findById(ref)
            .orElseThrow(() -> new SchemaParseException("$id not found in document: " + ref, ref));
        return resolveNode(node);
      }
      throw new SchemaParseException("Unsupported $ref format: " + ref, ref);
    } finally {
      active.remove(ref);
    }
  }

  private HasType resolveNode(JsonNode node) {
    HasType result = NodeFactory.resolveType(schema, node);
    if (result instanceof RefType nested) {
      return nested.resolve();
    }
    return result;
  }

  /**
   * Resolves the reference and returns it as the specified type if it matches.
   *
   * @param <T> the expected type
   * @param type the class of the expected type
   * @return Optional containing the resolved type if it matches, or empty otherwise
   */
  public <T extends HasType> Optional<T> resolveAs(Class<T> type) {
    HasType resolved = resolve();
    return type.isInstance(resolved)
        ? Optional.of(type.cast(resolved))
        : Optional.empty();
  }

  // ============ ObjectType delegation ============

  /**
   * Delegates to resolved ObjectType.properties().
   * Returns empty map if resolved type is not ObjectType.
   */
  public Map<String, HasType> properties() {
    return resolveAs(ObjectType.class)
        .map(ObjectType::properties)
        .orElse(Map.of());
  }

  /**
   * Delegates to resolved ObjectType.property().
   * Returns empty navigator if resolved type is not ObjectType.
   */
  public PropertyNavigator property(String name) {
    return resolveAs(ObjectType.class)
        .map(obj -> obj.property(name))
        .orElse(PropertyNavigator.empty());
  }

  /**
   * Delegates to resolved ObjectType.hasProperty().
   */
  public boolean hasProperty(String name) {
    return resolveAs(ObjectType.class)
        .map(obj -> obj.hasProperty(name))
        .orElse(false);
  }

  /**
   * Delegates to resolved ObjectType.getProperty().
   */
  public Optional<HasType> getProperty(String name) {
    return resolveAs(ObjectType.class)
        .flatMap(obj -> obj.getProperty(name));
  }

  /**
   * Delegates to resolved ObjectType.getPropertyAsString().
   */
  public Optional<StringType> getPropertyAsString(String name) {
    return resolveAs(ObjectType.class)
        .flatMap(obj -> obj.getPropertyAsString(name));
  }

  /**
   * Delegates to resolved ObjectType.getPropertyAsObject().
   */
  public Optional<ObjectType> getPropertyAsObject(String name) {
    return resolveAs(ObjectType.class)
        .flatMap(obj -> obj.getPropertyAsObject(name));
  }

  /**
   * Delegates to resolved ObjectType.getPropertyAsArray().
   */
  public Optional<ArrayType> getPropertyAsArray(String name) {
    return resolveAs(ObjectType.class)
        .flatMap(obj -> obj.getPropertyAsArray(name));
  }

  /**
   * Delegates to resolved ObjectType.getPropertyAsInteger().
   */
  public Optional<IntegerType> getPropertyAsInteger(String name) {
    return resolveAs(ObjectType.class)
        .flatMap(obj -> obj.getPropertyAsInteger(name));
  }

  /**
   * Delegates to resolved ObjectType.getPropertyAsNumber().
   */
  public Optional<NumberType> getPropertyAsNumber(String name) {
    return resolveAs(ObjectType.class)
        .flatMap(obj -> obj.getPropertyAsNumber(name));
  }

  /**
   * Delegates to resolved ObjectType.getPropertyAsBoolean().
   */
  public Optional<BooleanType> getPropertyAsBoolean(String name) {
    return resolveAs(ObjectType.class)
        .flatMap(obj -> obj.getPropertyAsBoolean(name));
  }

  /**
   * Delegates to resolved ObjectType.patternProperties().
   */
  public Map<String, HasType> patternProperties() {
    return resolveAs(ObjectType.class)
        .map(ObjectType::patternProperties)
        .orElse(Map.of());
  }

  /**
   * Delegates to resolved ObjectType.dependentRequired().
   */
  public Map<String, List<String>> dependentRequired() {
    return resolveAs(ObjectType.class)
        .map(ObjectType::dependentRequired)
        .orElse(Map.of());
  }

  /**
   * Delegates to resolved ObjectType.dependentSchemas().
   */
  public Map<String, HasType> dependentSchemas() {
    return resolveAs(ObjectType.class)
        .map(ObjectType::dependentSchemas)
        .orElse(Map.of());
  }

  // ============ ArrayType delegation ============

  /**
   * Delegates to resolved ArrayType.getItems().
   * Returns empty array if resolved type is not ArrayType.
   */
  public ArrayItemType[] getItems() {
    return resolveAs(ArrayType.class)
        .map(ArrayType::getItems)
        .orElse(new ArrayItemType[0]);
  }

  // ============ StringType delegation ============

  /**
   * Delegates to resolved StringType.pattern().
   */
  public Optional<String> pattern() {
    return resolveAs(StringType.class)
        .flatMap(StringType::pattern);
  }

  /**
   * Delegates to resolved StringType.format().
   */
  public Optional<String> format() {
    return resolveAs(StringType.class)
        .flatMap(StringType::format);
  }

  /**
   * Delegates to resolved StringType.enumConstraint().
   * Returns empty list if resolved is not StringType.
   */
  public List<String> enumConstraint() {
    return resolveAs(StringType.class)
        .map(StringType::enumConstraint)
        .orElse(List.of());
  }

  /**
   * Delegates to resolved StringType.minLength().
   */
  public Optional<Integer> minLength() {
    return resolveAs(StringType.class)
        .flatMap(StringType::minLength);
  }

  /**
   * Delegates to resolved StringType.maxLength().
   */
  public Optional<Integer> maxLength() {
    return resolveAs(StringType.class)
        .flatMap(StringType::maxLength);
  }

  // ============ IntegerType/NumberType delegation ============

  /**
   * Delegates to resolved IntegerType.minimum().
   */
  public Optional<Integer> minimum() {
    return resolveAs(IntegerType.class)
        .flatMap(IntegerType::minimum);
  }

  /**
   * Delegates to resolved IntegerType.maximum().
   */
  public Optional<Integer> maximum() {
    return resolveAs(IntegerType.class)
        .flatMap(IntegerType::maximum);
  }

  /**
   * Delegates to resolved IntegerType.exclusiveMinimum().
   */
  public Optional<Integer> exclusiveMinimum() {
    return resolveAs(IntegerType.class)
        .flatMap(IntegerType::exclusiveMinimum);
  }

  /**
   * Delegates to resolved IntegerType.exclusiveMaximum().
   */
  public Optional<Integer> exclusiveMaximum() {
    return resolveAs(IntegerType.class)
        .flatMap(IntegerType::exclusiveMaximum);
  }

  // ============ Common metadata delegation ============

  @Override
  public Optional<String> title() {
    return resolve().title();
  }

  @Override
  public Optional<String> description() {
    return resolve().description();
  }

  @Override
  public Optional<Boolean> deprecated() {
    return resolve().deprecated();
  }

  @Override
  public Optional<Boolean> readOnly() {
    return resolve().readOnly();
  }

  @Override
  public Optional<Boolean> writeOnly() {
    return resolve().writeOnly();
  }

  @Override
  public Optional<java.util.List<JsonNode>> examples() {
    return resolve().examples();
  }

  @Override
  public Optional<HasType> not() {
    return resolve().not();
  }

  @Override
  public Map<String, JsonNode> getExtensions() {
    return resolve().getExtensions();
  }

  @Override
  public Optional<JsonNode> getExtension(String key) {
    return resolve().getExtension(key);
  }
}
