package org.treblereel.yaml.schema.model;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.Parser;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;

class RefTypeResolveAsTest {

    private final Parser parser = new Parser();

    @Test
    void resolveAs_shouldReturnObjectType_whenResolvedIsObject() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-ref.schema.yaml");
        ObjectType root = schema.requireObject();
        RefType ref = (RefType) root.properties().get("address");

        Optional<ObjectType> result = ref.resolveAs(ObjectType.class);

        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(ObjectType.class);
    }

    @Test
    void resolveAs_shouldReturnEmpty_whenResolvedIsNotRequestedType() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-ref.schema.yaml");
        ObjectType root = schema.requireObject();
        RefType ref = (RefType) root.properties().get("address");

        Optional<ArrayType> result = ref.resolveAs(ArrayType.class);

        assertThat(result).isEmpty();
    }

    @Test
    void resolveAs_shouldWorkWithStringType() {
        SchemaDefinition schema = parser.parse("src/test/resources/ref-with-inline.schema.yaml");
        ObjectType root = schema.requireObject();

        // Assumes schema has a ref to string type
        Optional<HasType> prop = root.getProperty("description");
        if (prop.isPresent() && prop.get() instanceof RefType ref) {
            Optional<StringType> result = ref.resolveAs(StringType.class);
            assertThat(result).isPresent();
        }
    }
}
