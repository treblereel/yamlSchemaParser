package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a $ref reference to another schema definition.
 * <p>
 * References allow schema reuse and circular/recursive definitions. This implementation
 * supports local references within the same schema document using the #/$defs/ pattern.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * RefType addressRef = (RefType) objectType.properties().get().get("address");
 * String refPath = addressRef.ref(); // "#/$defs/Address"
 * ObjectType addressType = (ObjectType) addressRef.resolve();
 * Map<String, HasType> addressProps = addressType.properties().get();
 * }</pre>
 * </p>
 *
 * @param schema the parent schema definition containing the $defs
 * @param ref the reference path (e.g., "#/$defs/Address")
 * @author Dmitrii Tikhomirov
 */
public record RefType(SchemaDefinition schema, String ref) implements HasType {

  /**
   * Resolves the reference and returns the target schema type.
   * <p>
   * The resolved type can be any HasType implementation: ObjectType, StringType, ArrayType,
   * IntegerType, NumberType, BooleanType, NullType, AllOfType, or even another RefType.
   * </p>
   * <p>
   * Currently supports only local $defs references in the format "#/$defs/DefinitionName".
   * </p>
   *
   * @return the resolved schema type
   * @throws IllegalArgumentException if the reference format is not supported
   */
  public HasType resolve() {
    if (ref.startsWith("#/$defs/")) {
      String elementName = ref.substring(8);
      JsonNode node = schema.node().get("$defs").get(elementName);
      return NodeFactory.resolveType(schema, node);
    }
    throw new IllegalArgumentException("Only local $defs references are supported: " + ref);
  }
}
