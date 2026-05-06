package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.util.Optional;

/**
 * Represents an array type schema.
 * <p>
 * Provides access to array item types, size constraints (minItems, maxItems),
 * uniqueness constraint, and tuple validation support.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * ArrayType tagsArray = (ArrayType) schema.model();
 * ArrayItemType[] items = tagsArray.getItems();
 * StringType itemType = (StringType) items[0].getType();
 * Optional<Integer> minItems = tagsArray.minItems();
 * }</pre>
 * </p>
 *
 * @param schemaDefinition the parent schema definition
 * @param node the JSON node representing this array type
 * @author Dmitrii Tikhomirov
 */
public record ArrayType(SchemaDefinition schemaDefinition, JsonNode node) implements HasType {

  /**
   * Returns the array item type(s).
   * <p>
   * For uniform arrays (all items same type), returns a single-element array.
   * For tuple validation (positional types), returns multiple elements where
   * each position corresponds to a specific type.
   * </p>
   * <p>
   * Example:
   * <pre>{@code
   * // Uniform array: ["string1", "string2", ...]
   * ArrayItemType[] items = arrayType.getItems(); // length = 1
   * StringType itemType = (StringType) items[0].getType();
   *
   * // Tuple: ["name", 25, true]
   * ArrayItemType[] items = arrayType.getItems(); // length = 3
   * StringType nameType = (StringType) items[0].getType();
   * IntegerType ageType = (IntegerType) items[1].getType();
   * BooleanType activeType = (BooleanType) items[2].getType();
   * }</pre>
   * </p>
   *
   * @return array of item types (single element for uniform arrays, multiple for tuples)
   */
  public ArrayItemType[] getItems() {
    JsonNode itemsNode = node.get("items");
    if (itemsNode.getNodeType().equals(JsonNodeType.ARRAY)) {
      ArrayItemType[] arrayItemTypes = new ArrayItemType[itemsNode.size()];
      for (int i = 0; i < itemsNode.size(); i++) {
        arrayItemTypes[i] = new ArrayItemType(schemaDefinition, itemsNode.get(i));
      }
      return arrayItemTypes;
    }
    return new ArrayItemType[]{new ArrayItemType(schemaDefinition, itemsNode)};
  }

  /**
   * Returns the minimum number of items constraint.
   *
   * @return the minimum array length if specified
   */
  public Optional<Integer> minItems() {
    return Optional.ofNullable(node.get("minItems"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  /**
   * Returns the maximum number of items constraint.
   *
   * @return the maximum array length if specified
   */
  public Optional<Integer> maxItems() {
    return Optional.ofNullable(node.get("maxItems"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  /**
   * Returns the unique items constraint.
   * <p>
   * When true, all items in the array must be unique.
   * </p>
   *
   * @return the uniqueItems constraint if specified
   */
  public Optional<Boolean> uniqueItems() {
    return Optional.ofNullable(node.get("uniqueItems"))
            .filter(JsonNode::isBoolean)
            .map(JsonNode::booleanValue);
  }

  /**
   * Returns the additional items constraint.
   * <p>
   * Used with tuple validation to control whether additional items beyond
   * the defined tuple positions are allowed.
   * </p>
   *
   * @return the additionalItems constraint if specified
   */
  public Optional<Boolean> additionalItems() {
    return Optional.ofNullable(node.get("additionalItems"))
            .filter(JsonNode::isBoolean)
            .map(JsonNode::booleanValue);
  }

  /**
   * Returns the title of this array type.
   *
   * @return the title if present
   */
  public Optional<String> title() {
    return Optional.ofNullable(node.get("title")).map(JsonNode::asText);
  }

  /**
   * Returns the description of this array type.
   *
   * @return the description if present
   */
  public Optional<String> description() {
    return Optional.ofNullable(node.get("description")).map(JsonNode::asText);
  }

  /**
   * Returns the default value for this array.
   *
   * @return the default value as JsonNode if specified
   */
  public Optional<JsonNode> defaultValue() {
    return Optional.ofNullable(node.get("default"));
  }

}
