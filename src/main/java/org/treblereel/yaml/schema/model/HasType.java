package org.treblereel.yaml.schema.model;

/**
 * Marker interface for all schema type representations.
 * <p>
 * This interface is implemented by all type classes representing JSON Schema types:
 * </p>
 * <ul>
 *   <li>{@link ObjectType} - object schemas with properties</li>
 *   <li>{@link ArrayType} - array schemas with items</li>
 *   <li>{@link StringType} - string schemas with constraints</li>
 *   <li>{@link IntegerType} - integer schemas with range constraints</li>
 *   <li>{@link NumberType} - number schemas with range constraints</li>
 *   <li>{@link BooleanType} - boolean schemas</li>
 *   <li>{@link NullType} - null type schemas</li>
 *   <li>{@link RefType} - $ref references to other schema parts</li>
 *   <li>{@link AllOfType} - allOf combinator schemas</li>
 * </ul>
 *
 * @author Dmitrii Tikhomirov
 */
public interface HasType {

}
