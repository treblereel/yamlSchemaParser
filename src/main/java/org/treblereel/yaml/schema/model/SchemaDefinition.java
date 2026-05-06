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
   * Returns the root type as ObjectType if it is one.
   * @return Optional containing ObjectType, or empty if model is not an ObjectType
   */
  public Optional<ObjectType> modelAsObject() {
    HasType type = model();
    return type instanceof ObjectType obj ? Optional.of(obj) : Optional.empty();
  }

  /**
   * Returns the root type as ArrayType if it is one.
   * @return Optional containing ArrayType, or empty if model is not an ArrayType
   */
  public Optional<ArrayType> modelAsArray() {
    HasType type = model();
    return type instanceof ArrayType arr ? Optional.of(arr) : Optional.empty();
  }

  /**
   * Returns the root type as StringType if it is one.
   * @return Optional containing StringType, or empty if model is not a StringType
   */
  public Optional<StringType> modelAsString() {
    HasType type = model();
    return type instanceof StringType str ? Optional.of(str) : Optional.empty();
  }

  /**
   * Returns the root type as IntegerType if it is one.
   * @return Optional containing IntegerType, or empty if model is not an IntegerType
   */
  public Optional<IntegerType> modelAsInteger() {
    HasType type = model();
    return type instanceof IntegerType i ? Optional.of(i) : Optional.empty();
  }

  /**
   * Returns the root type as NumberType if it is one.
   * @return Optional containing NumberType, or empty if model is not a NumberType
   */
  public Optional<NumberType> modelAsNumber() {
    HasType type = model();
    return type instanceof NumberType num ? Optional.of(num) : Optional.empty();
  }

  /**
   * Returns the root type as BooleanType if it is one.
   * @return Optional containing BooleanType, or empty if model is not a BooleanType
   */
  public Optional<BooleanType> modelAsBoolean() {
    HasType type = model();
    return type instanceof BooleanType bool ? Optional.of(bool) : Optional.empty();
  }

  /**
   * Returns the root type as AllOfType if it is one.
   * @return Optional containing AllOfType, or empty if model is not an AllOfType
   */
  public Optional<AllOfType> modelAsAllOf() {
    HasType type = model();
    return type instanceof AllOfType allOf ? Optional.of(allOf) : Optional.empty();
  }

  /**
   * Returns the root type as ObjectType, throwing if it is not one.
   * @return ObjectType
   * @throws TypeMismatchException if model is not an ObjectType
   */
  public ObjectType requireObject() {
    return modelAsObject()
        .orElseThrow(() -> new TypeMismatchException(
            "Expected ObjectType but got " + model().getClass().getSimpleName()));
  }

  /**
   * Returns the root type as ArrayType, throwing if it is not one.
   * @return ArrayType
   * @throws TypeMismatchException if model is not an ArrayType
   */
  public ArrayType requireArray() {
    return modelAsArray()
        .orElseThrow(() -> new TypeMismatchException(
            "Expected ArrayType but got " + model().getClass().getSimpleName()));
  }

  /**
   * Returns the root type as StringType, throwing if it is not one.
   * @return StringType
   * @throws TypeMismatchException if model is not a StringType
   */
  public StringType requireString() {
    return modelAsString()
        .orElseThrow(() -> new TypeMismatchException(
            "Expected StringType but got " + model().getClass().getSimpleName()));
  }

  /**
   * Returns the root type as IntegerType, throwing if it is not one.
   * @return IntegerType
   * @throws TypeMismatchException if model is not an IntegerType
   */
  public IntegerType requireInteger() {
    return modelAsInteger()
        .orElseThrow(() -> new TypeMismatchException(
            "Expected IntegerType but got " + model().getClass().getSimpleName()));
  }

  /**
   * Returns the root type as NumberType, throwing if it is not one.
   * @return NumberType
   * @throws TypeMismatchException if model is not a NumberType
   */
  public NumberType requireNumber() {
    return modelAsNumber()
        .orElseThrow(() -> new TypeMismatchException(
            "Expected NumberType but got " + model().getClass().getSimpleName()));
  }

  /**
   * Returns the root type as BooleanType, throwing if it is not one.
   * @return BooleanType
   * @throws TypeMismatchException if model is not a BooleanType
   */
  public BooleanType requireBoolean() {
    return modelAsBoolean()
        .orElseThrow(() -> new TypeMismatchException(
            "Expected BooleanType but got " + model().getClass().getSimpleName()));
  }

  /**
   * Returns the root type as AllOfType, throwing if it is one.
   * @return AllOfType
   * @throws TypeMismatchException if model is not an AllOfType
   */
  public AllOfType requireAllOf() {
    return modelAsAllOf()
        .orElseThrow(() -> new TypeMismatchException(
            "Expected AllOfType but got " + model().getClass().getSimpleName()));
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
