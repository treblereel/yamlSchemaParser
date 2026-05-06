package org.treblereel.yaml.schema.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class TypeMismatchExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        String message = "Expected ObjectType but got StringType";

        TypeMismatchException exception = new TypeMismatchException(message);

        assertThat(exception)
            .isInstanceOf(RuntimeException.class)
            .hasMessage(message);
    }

    @Test
    void shouldAllowThrowingAndCatching() {
        assertThatThrownBy(() -> {
            throw new TypeMismatchException("Type mismatch");
        })
            .isInstanceOf(TypeMismatchException.class)
            .hasMessage("Type mismatch");
    }
}
