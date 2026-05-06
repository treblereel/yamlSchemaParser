package org.treblereel.yaml.schema.model;

import java.util.Optional;

/**
 * Implementation of PropertyNavigator that wraps a HasType value.
 * Supports navigation through object properties and array items.
 * Package-private - created internally by ObjectType.property()
 */
class PropertyNavigatorImpl implements PropertyNavigator {

    private final Optional<HasType> current;
    private final boolean resolved;

    /**
     * Creates a PropertyNavigator with a value.
     * @param current the HasType value, or empty
     * @param resolved whether RefType resolution has occurred
     */
    PropertyNavigatorImpl(Optional<HasType> current, boolean resolved) {
        this.current = current;
        this.resolved = resolved;
    }

    @Override
    public Optional<HasType> get() {
        return current;
    }

    @Override
    public PropertyNavigator resolve() {
        if (resolved || current.isEmpty()) {
            return this;
        }

        HasType type = current.get();
        if (type instanceof RefType ref) {
            return new PropertyNavigatorImpl(Optional.of(ref.resolve()), true);
        }

        return new PropertyNavigatorImpl(current, true);
    }

    @Override
    public PropertyNavigator property(String name) {
        return current
            .map(type -> {
                if (type instanceof ObjectType obj) {
                    return obj.property(name);
                }
                return PropertyNavigator.empty();
            })
            .orElse(PropertyNavigator.empty());
    }

    @Override
    public PropertyNavigator item(int index) {
        return current
            .filter(ArrayType.class::isInstance)
            .map(ArrayType.class::cast)
            .filter(arr -> index >= 0 && index < arr.getItems().length)
            .map(arr -> (PropertyNavigator) new PropertyNavigatorImpl(
                Optional.of(arr.getItems()[index].getType()),
                false))
            .orElse(PropertyNavigator.empty());
    }

    @Override
    public Optional<ObjectType> asObject() {
        return current
            .filter(ObjectType.class::isInstance)
            .map(ObjectType.class::cast);
    }

    @Override
    public Optional<ArrayType> asArray() {
        return current
            .filter(ArrayType.class::isInstance)
            .map(ArrayType.class::cast);
    }

    @Override
    public Optional<StringType> asString() {
        return current
            .filter(StringType.class::isInstance)
            .map(StringType.class::cast);
    }

    @Override
    public Optional<IntegerType> asInteger() {
        return current
            .filter(IntegerType.class::isInstance)
            .map(IntegerType.class::cast);
    }

    @Override
    public Optional<NumberType> asNumber() {
        return current
            .filter(NumberType.class::isInstance)
            .map(NumberType.class::cast);
    }

    @Override
    public Optional<BooleanType> asBoolean() {
        return current
            .filter(BooleanType.class::isInstance)
            .map(BooleanType.class::cast);
    }

    @Override
    public Optional<AllOfType> asAllOf() {
        return current
            .filter(AllOfType.class::isInstance)
            .map(AllOfType.class::cast);
    }

    @Override
    public boolean exists() {
        return current.isPresent();
    }

    @Override
    public boolean isResolved() {
        return resolved;
    }
}
