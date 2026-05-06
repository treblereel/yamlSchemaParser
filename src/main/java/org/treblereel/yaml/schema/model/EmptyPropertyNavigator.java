package org.treblereel.yaml.schema.model;

import java.util.Optional;

/**
 * Singleton implementation of PropertyNavigator that represents absence of value.
 * All operations return empty or this instance.
 * Package-private - only accessible via PropertyNavigator.empty()
 */
class EmptyPropertyNavigator implements PropertyNavigator {

    private static final EmptyPropertyNavigator INSTANCE = new EmptyPropertyNavigator();

    static PropertyNavigator instance() {
        return INSTANCE;
    }

    private EmptyPropertyNavigator() {}

    @Override
    public Optional<HasType> get() {
        return Optional.empty();
    }

    @Override
    public PropertyNavigator resolve() {
        return this;
    }

    @Override
    public PropertyNavigator property(String name) {
        return this;
    }

    @Override
    public PropertyNavigator item(int index) {
        return this;
    }

    @Override
    public Optional<ObjectType> asObject() {
        return Optional.empty();
    }

    @Override
    public Optional<ArrayType> asArray() {
        return Optional.empty();
    }

    @Override
    public Optional<StringType> asString() {
        return Optional.empty();
    }

    @Override
    public Optional<IntegerType> asInteger() {
        return Optional.empty();
    }

    @Override
    public Optional<NumberType> asNumber() {
        return Optional.empty();
    }

    @Override
    public Optional<BooleanType> asBoolean() {
        return Optional.empty();
    }

    @Override
    public Optional<AllOfType> asAllOf() {
        return Optional.empty();
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public boolean isResolved() {
        return false;
    }
}
