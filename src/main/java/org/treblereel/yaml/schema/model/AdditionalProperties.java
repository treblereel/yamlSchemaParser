package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

/**
 * Represents the additionalProperties configuration for an object schema.
 * <p>
 * The additionalProperties keyword controls whether properties not explicitly defined
 * in the schema are allowed, and if so, what type they must conform to.
 * </p>
 * <p>
 * Three common patterns:
 * </p>
 * <ul>
 *   <li><b>additionalProperties: false</b> - No additional properties allowed (strict schema)</li>
 *   <li><b>additionalProperties: true</b> - Any additional properties allowed (no type constraint)</li>
 *   <li><b>additionalProperties: {type: "string"}</b> - Additional properties allowed but must be strings</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * ObjectType objectType = (ObjectType) schema.model();
 * if (objectType.additionalProperties().isPresent()) {
 *     AdditionalProperties addProps = objectType.additionalProperties().get();
 *     boolean allowed = addProps.isAllowed();
 *     Optional<HasType> type = addProps.getType();
 *     if (type.isPresent()) {
 *         StringType stringType = (StringType) type.get();
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @param node the JSON node representing the additionalProperties configuration
 * @param schema the parent schema definition
 * @author Dmitrii Tikhomirov
 */
public record AdditionalProperties(JsonNode node, SchemaDefinition schema) {

  /**
   * Returns whether additional properties are allowed.
   * <p>
   * Returns true if:
   * </p>
   * <ul>
   *   <li>additionalProperties is set to true (boolean)</li>
   *   <li>additionalProperties is a type schema (object) - means allowed with type constraint</li>
   * </ul>
   * <p>
   * Returns false if additionalProperties is set to false (boolean).
   * </p>
   *
   * @return true if additional properties are allowed, false otherwise
   */
  public boolean isAllowed() {
    if (node.isBoolean()) {
      return node.asBoolean();
    }
    return true; // type schema means allowed
  }

  /**
   * Returns the type constraint for additional properties.
   * <p>
   * Returns empty if additionalProperties is a boolean (true/false).
   * Returns the type schema if additionalProperties defines a type constraint.
   * </p>
   * <p>
   * Example schemas:
   * </p>
   * <pre>{@code
   * additionalProperties: false           // isAllowed() = false, getType() = empty
   * additionalProperties: true            // isAllowed() = true,  getType() = empty
   * additionalProperties:                 // isAllowed() = true,  getType() = StringType
   *   type: string
   * }</pre>
   *
   * @return the type constraint for additional properties if specified
   */
  public Optional<HasType> getType() {
    if (node.isBoolean()) {
      return Optional.empty();
    }
    return Optional.of(NodeFactory.resolveType(schema, node));
  }
}
