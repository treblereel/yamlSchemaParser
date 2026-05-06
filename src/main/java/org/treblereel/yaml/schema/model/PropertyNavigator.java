package org.treblereel.yaml.schema.model;

import java.util.Optional;

/**
 * Fluent API for navigating schema property trees.
 * Provides type-safe navigation with automatic RefType resolution.
 *
 * Example:
 * <pre>
 * Optional<String> cityPattern = root.property("address")
 *     .resolve()
 *     .property("city")
 *     .asString()
 *     .flatMap(StringType::pattern);
 * </pre>
 */
public interface PropertyNavigator {

    /**
     * Returns the current type, if present.
     * @return Optional containing current HasType, or empty if navigation failed
     */
    Optional<HasType> get();

    /**
     * Resolves the current type if it is a RefType.
     * If current type is not RefType, returns this navigator unchanged.
     * @return PropertyNavigator with resolved type
     */
    PropertyNavigator resolve();

    /**
     * Navigates to a property by name.
     * Only works if current type is ObjectType.
     * @param name property name
     * @return PropertyNavigator for the property, or empty navigator if not found
     */
    PropertyNavigator property(String name);

    /**
     * Navigates to an array item by index.
     * Only works if current type is ArrayType.
     * @param index 0-based item index
     * @return PropertyNavigator for the item, or empty navigator if out of bounds
     */
    PropertyNavigator item(int index);

    /**
     * Converts current type to ObjectType if it is one.
     * @return Optional containing ObjectType, or empty if current is not ObjectType
     */
    Optional<ObjectType> asObject();

    /**
     * Converts current type to ArrayType if it is one.
     * @return Optional containing ArrayType, or empty if current is not ArrayType
     */
    Optional<ArrayType> asArray();

    /**
     * Converts current type to StringType if it is one.
     * @return Optional containing StringType, or empty if current is not StringType
     */
    Optional<StringType> asString();

    /**
     * Converts current type to IntegerType if it is one.
     * @return Optional containing IntegerType, or empty if current is not IntegerType
     */
    Optional<IntegerType> asInteger();

    /**
     * Converts current type to NumberType if it is one.
     * @return Optional containing NumberType, or empty if current is not NumberType
     */
    Optional<NumberType> asNumber();

    /**
     * Converts current type to BooleanType if it is one.
     * @return Optional containing BooleanType, or empty if current is not BooleanType
     */
    Optional<BooleanType> asBoolean();

    /**
     * Converts current type to AllOfType if it is one.
     * @return Optional containing AllOfType, or empty if current is not AllOfType
     */
    Optional<AllOfType> asAllOf();

    /**
     * Checks if current navigation has a value.
     * @return true if a type is present, false otherwise
     */
    boolean exists();

    /**
     * Checks if resolve() has been called on this navigator.
     * @return true if resolved, false otherwise
     */
    boolean isResolved();

    /**
     * Returns an empty PropertyNavigator that always returns empty for all operations.
     * @return empty PropertyNavigator singleton
     */
    static PropertyNavigator empty() {
        return EmptyPropertyNavigator.instance();
    }
}
