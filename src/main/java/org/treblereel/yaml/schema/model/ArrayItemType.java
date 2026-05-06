package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Optional;

/**
 * Represents an array item type schema.
 * <p>
 * This class wraps the schema definition for items within an array. It supports both
 * uniform arrays (where all items have the same type) and tuple validation (where each
 * position has a specific type).
 * </p>
 * <p>
 * Array items can also use schema combinators (oneOf, anyOf, allOf) to define
 * flexible item types.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * ArrayType arrayType = (ArrayType) schema.model();
 * ArrayItemType[] items = arrayType.getItems();
 * HasType itemType = items[0].getType();
 * boolean nullable = items[0].isNullable();
 * }</pre>
 * </p>
 *
 * @author Dmitrii Tikhomirov
 */
public class ArrayItemType {

  private final Applicators applicators;
  private final JsonNode node;
  private final SchemaDefinition schemaDefinition;

  /**
   * Constructs an ArrayItemType from a schema definition and JSON node.
   *
   * @param schemaDefinition the parent schema definition
   * @param node the JSON node representing this array item schema
   */
  public ArrayItemType(SchemaDefinition schemaDefinition, JsonNode node) {
    this.schemaDefinition = schemaDefinition;
    this.node = node;
    this.applicators = new Applicators(schemaDefinition, node);
  }

  /**
   * Returns the type of this array item.
   * <p>
   * The returned type can be any HasType implementation: ObjectType, StringType, ArrayType,
   * IntegerType, NumberType, BooleanType, NullType, RefType, or AllOfType.
   * </p>
   *
   * @return the item type
   */
  public HasType getType() {
    return NodeFactory.resolveType(schemaDefinition, node);
  }

  /**
   * Returns the oneOf combinator schemas for this item.
   * <p>
   * The item must validate against exactly one of the schemas in the list.
   * </p>
   *
   * @return list of oneOf schemas if present
   */
  public Optional<List<HasType>> oneOf() {
    return applicators.oneOf();
  }

  /**
   * Returns the anyOf combinator schemas for this item.
   * <p>
   * The item must validate against at least one of the schemas in the list.
   * </p>
   *
   * @return list of anyOf schemas if present
   */
  public Optional<List<HasType>> anyOf() {
    return applicators.anyOf();
  }

  /**
   * Returns the allOf combinator schemas for this item.
   * <p>
   * The item must validate against all schemas in the list.
   * </p>
   *
   * @return list of allOf schemas if present
   */
  public Optional<List<HasType>> allOf() {
    return applicators.allOf();
  }

  /**
   * Checks if this item type is nullable.
   * <p>
   * Returns true if the type array includes "null" (e.g., ["string", "null"]).
   * </p>
   *
   * @return true if the item can be null, false otherwise
   */
  public boolean isNullable() {
    JsonNode typeNode = node.get("type");
    if (typeNode != null && typeNode.isArray()) {
      for (JsonNode t : typeNode) {
        if ("null".equals(t.asText())) {
          return true;
        }
      }
    }
    return false;
  }
}
