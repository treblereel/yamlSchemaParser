package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a string type schema.
 * <p>
 * Provides access to string-specific constraints including pattern (regex), format, length constraints,
 * enumeration values, const values, and default values.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * StringType emailType = (StringType) objectType.properties().get().get("email");
 * Optional<String> pattern = emailType.pattern();
 * Optional<String> format = emailType.format(); // "email", "uri", "date-time", etc.
 * Optional<List<String>> enumValues = emailType.enumValues();
 * }</pre>
 * </p>
 *
 * @param schema the parent schema definition
 * @param node the JSON node representing this string type
 * @author Dmitrii Tikhomirov
 */
public record StringType(SchemaDefinition schema, JsonNode node) implements HasType {

  /**
   * Returns the pattern (regular expression) constraint.
   *
   * @return the regex pattern if specified
   */
  public Optional<String> pattern() {
    return Optional.ofNullable(node.get("pattern")).map(JsonNode::asText);
  }

  /**
   * Returns the format constraint.
   * <p>
   * Common formats include: "email", "uri", "uri-template", "date-time", "date", "time",
   * "uuid", "ipv4", "ipv6", "hostname", etc.
   * </p>
   *
   * @return the format if specified
   */
  public Optional<String> format() {
    return Optional.ofNullable(node.get("format")).map(JsonNode::asText);
  }

  /**
   * Returns the minimum length constraint.
   *
   * @return the minimum string length if specified
   */
  public Optional<Integer> minLength() {
    return Optional.ofNullable(node.get("minLength"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  /**
   * Returns the maximum length constraint.
   *
   * @return the maximum string length if specified
   */
  public Optional<Integer> maxLength() {
    return Optional.ofNullable(node.get("maxLength"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  /**
   * Returns the enumeration values constraint.
   * <p>
   * The string value must be one of the values in this list.
   * </p>
   *
   * @return list of allowed enum values if specified
   */
  public Optional<List<String>> enumValues() {
    return Optional.ofNullable(node.get("enum"))
            .filter(JsonNode::isArray)
            .map(array -> {
              List<String> values = new ArrayList<>();
              array.forEach(v -> values.add(v.asText()));
              return values;
            });
  }

  /**
   * Returns the const value constraint.
   * <p>
   * The string value must exactly match this constant value.
   * </p>
   *
   * @return the const value if specified
   */
  public Optional<String> constValue() {
    return Optional.ofNullable(node.get("const")).map(JsonNode::asText);
  }

  /**
   * Returns the default value.
   *
   * @return the default string value if specified
   */
  public Optional<String> defaultValue() {
    return Optional.ofNullable(node.get("default")).map(JsonNode::asText);
  }

  /**
   * Returns the title of this string type.
   *
   * @return the title if present
   */
  public Optional<String> title() {
    return Optional.ofNullable(node.get("title")).map(JsonNode::asText);
  }

  /**
   * Returns the description of this string type.
   *
   * @return the description if present
   */
  public Optional<String> description() {
    return Optional.ofNullable(node.get("description")).map(JsonNode::asText);
  }

  /**
   * Returns the not combinator schema.
   * <p>
   * The string value must NOT validate against this schema.
   * </p>
   *
   * @return the not schema if present
   */
  public Optional<HasType> not() {
    return Optional.ofNullable(node.get("not"))
            .map(n -> NodeFactory.resolveType(schema, n));
  }
}
