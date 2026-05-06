package org.treblereel.yaml.schema.model;

/**
 * Runtime exception thrown when a type assertion fails.
 * Used by require* methods in SchemaDefinition when model type doesn't match expected type.
 */
public class TypeMismatchException extends RuntimeException {

    public TypeMismatchException(String message) {
        super(message);
    }
}
