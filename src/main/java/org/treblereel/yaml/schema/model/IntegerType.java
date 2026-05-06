package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents an integer type schema.
 * <p>
 * Provides access to integer-specific constraints including range (minimum, maximum),
 * exclusive range, enumeration values, const values, and default values.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * IntegerType portType = (IntegerType) objectType.properties().get("port");
 * Optional<Integer> min = portType.minimum(); // 0
 * Optional<Integer> max = portType.maximum(); // 65535
 * }</pre>
 * </p>
 *
 * @param schema the parent schema definition
 * @param node the JSON node representing this integer type
 * @author Dmitrii Tikhomirov
 */
public record IntegerType(SchemaDefinition schema, JsonNode node) implements HasType {

  /**
   * Returns the minimum value constraint (inclusive).
   *
   * @return the minimum integer value if specified
   */
  public Optional<Integer> minimum() {
    return Optional.ofNullable(node.get("minimum"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  /**
   * Returns the maximum value constraint (inclusive).
   *
   * @return the maximum integer value if specified
   */
  public Optional<Integer> maximum() {
    return Optional.ofNullable(node.get("maximum"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  /**
   * Returns the exclusive minimum value constraint (JSON Schema Draft 6+).
   * <p>
   * The value must be greater than (not equal to) this value.
   * This is the numeric syntax introduced in Draft 6+.
   * </p>
   *
   * @return the exclusive minimum integer value if specified
   */
  public Optional<Integer> exclusiveMinimum() {
    return Optional.ofNullable(node.get("exclusiveMinimum"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  /**
   * Returns the exclusive maximum value constraint (JSON Schema Draft 6+).
   * <p>
   * The value must be less than (not equal to) this value.
   * This is the numeric syntax introduced in Draft 6+.
   * </p>
   *
   * @return the exclusive maximum integer value if specified
   */
  public Optional<Integer> exclusiveMaximum() {
    return Optional.ofNullable(node.get("exclusiveMaximum"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  /**
   * Returns the exclusive minimum flag (JSON Schema Draft 4).
   * <p>
   * Draft 4 boolean syntax: when true, the minimum value is exclusive (value > minimum).
   * When false, the minimum value is inclusive (value >= minimum).
   * Use with minimum() to get the bound value.
   * </p>
   *
   * @return true if minimum is exclusive, false if inclusive, empty if not specified or numeric syntax
   */
  public Optional<Boolean> isMinimumExclusive() {
    return Optional.ofNullable(node.get("exclusiveMinimum"))
            .filter(JsonNode::isBoolean)
            .map(JsonNode::booleanValue);
  }

  /**
   * Returns the exclusive maximum flag (JSON Schema Draft 4).
   * <p>
   * Draft 4 boolean syntax: when true, the maximum value is exclusive (value < maximum).
   * When false, the maximum value is inclusive (value <= maximum).
   * Use with maximum() to get the bound value.
   * </p>
   *
   * @return true if maximum is exclusive, false if inclusive, empty if not specified or numeric syntax
   */
  public Optional<Boolean> isMaximumExclusive() {
    return Optional.ofNullable(node.get("exclusiveMaximum"))
            .filter(JsonNode::isBoolean)
            .map(JsonNode::booleanValue);
  }

  /**
   * Returns the enumeration values constraint.
   * <p>
   * The integer value must be one of the values in this list.
   * </p>
   *
   * @return list of allowed enum values if specified
   */
  public Optional<List<Integer>> enumValues() {
    return Optional.ofNullable(node.get("enum"))
            .filter(JsonNode::isArray)
            .map(array -> {
              List<Integer> values = new ArrayList<>();
              array.forEach(v -> values.add(v.intValue()));
              return values;
            });
  }

  /**
   * Returns the const value constraint.
   * <p>
   * The integer value must exactly match this constant value.
   * </p>
   *
   * @return the const value if specified
   */
  public Optional<Integer> constValue() {
    return Optional.ofNullable(node.get("const"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  /**
   * Returns the default value.
   *
   * @return the default integer value if specified
   */
  public Optional<Integer> defaultValue() {
    return Optional.ofNullable(node.get("default"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  /**
   * Returns the title of this integer type.
   *
   * @return the title if present
   */
  public Optional<String> title() {
    return Optional.ofNullable(node.get("title")).map(JsonNode::asText);
  }

  /**
   * Returns the description of this integer type.
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
              List<JsonNode> examples = new java.util.ArrayList<>();
              examplesNode.forEach(examples::add);
              return examples;
            });
  }

  /**
   * Returns the not combinator schema.
   * <p>
   * The integer value must NOT validate against this schema.
   * </p>
   *
   * @return the not schema if present
   */
  public Optional<HasType> not() {
    return Optional.ofNullable(node.get("not"))
            .map(n -> NodeFactory.resolveType(schema, n));
  }
}
