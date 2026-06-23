package org.treblereel.yaml.schema.model;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.Parser;

import static org.assertj.core.api.Assertions.*;
import org.treblereel.yaml.schema.model.SchemaParseException;

class AnchorIdRefTest {

    private final Parser parser = new Parser();

    @Test
    void resolve_shouldWork_whenAnchorRef() {
        SchemaDefinition schema = parser.parse("src/test/resources/anchor-ref.schema.yaml");
        ObjectType root = schema.requireObject();
        RefType homeRef = (RefType) root.properties().get("home");

        assertThat(homeRef.ref()).isEqualTo("#address");

        HasType resolved = homeRef.resolve();
        assertThat(resolved).isInstanceOf(ObjectType.class);

        ObjectType address = (ObjectType) resolved;
        assertThat(address.properties()).containsKey("street");
        assertThat(address.properties()).containsKey("city");
    }

    @Test
    void resolve_shouldWork_whenIdRef() {
        SchemaDefinition schema = parser.parse("src/test/resources/id-ref.schema.yaml");
        ObjectType root = schema.requireObject();
        RefType billingRef = (RefType) root.properties().get("billing");

        assertThat(billingRef.ref()).isEqualTo("https://example.com/schemas/address");

        HasType resolved = billingRef.resolve();
        assertThat(resolved).isInstanceOf(ObjectType.class);

        ObjectType address = (ObjectType) resolved;
        assertThat(address.properties()).containsKey("street");
        assertThat(address.properties()).containsKey("zip");
    }

    @Test
    void resolve_shouldThrow_whenAnchorNotFound() {
        SchemaDefinition schema = parser.parse("src/test/resources/anchor-ref.schema.yaml");
        RefType badRef = new RefType(schema, "#nonexistent");

        assertThatThrownBy(badRef::resolve)
            .isInstanceOf(SchemaParseException.class)
            .hasMessageContaining("$anchor not found");
    }

    @Test
    void resolve_shouldThrow_whenIdNotFound() {
        SchemaDefinition schema = parser.parse("src/test/resources/id-ref.schema.yaml");
        RefType badRef = new RefType(schema, "https://example.com/schemas/missing");

        assertThatThrownBy(badRef::resolve)
            .isInstanceOf(SchemaParseException.class)
            .hasMessageContaining("$id not found");
    }

    @Test
    void resolve_shouldWork_whenMixedRefStyles() {
        SchemaDefinition schema = parser.parse("src/test/resources/mixed-ref-styles.schema.yaml");
        ObjectType root = schema.requireObject();

        RefType nameRef = (RefType) root.properties().get("name");
        HasType nameResolved = nameRef.resolve();
        assertThat(nameResolved).isInstanceOf(StringType.class);

        RefType homeRef = (RefType) root.properties().get("home");
        HasType homeResolved = homeRef.resolve();
        assertThat(homeResolved).isInstanceOf(ObjectType.class);
        assertThat(((ObjectType) homeResolved).properties()).containsKey("street");

        RefType billingRef = (RefType) root.properties().get("billing");
        HasType billingResolved = billingRef.resolve();
        assertThat(billingResolved).isInstanceOf(ObjectType.class);
        assertThat(((ObjectType) billingResolved).properties()).containsKey("number");
    }

    @Test
    void resolve_shouldWork_whenAnchorInNestedProperty() {
        SchemaDefinition schema = parser.parse("src/test/resources/anchor-ref.schema.yaml");
        ObjectType root = schema.requireObject();

        RefType homeRef = (RefType) root.properties().get("home");
        RefType workRef = (RefType) root.properties().get("work");

        HasType homeResolved = homeRef.resolve();
        HasType workResolved = workRef.resolve();
        assertThat(homeResolved).isInstanceOf(ObjectType.class);
        assertThat(workResolved).isInstanceOf(ObjectType.class);
    }

    @Test
    void resolve_shouldDetectCycle_whenCircularAnchorRef() {
        SchemaDefinition schema = parser.parse("src/test/resources/circular-anchor-ref.schema.yaml");
        ObjectType root = schema.requireObject();
        RefType ref = (RefType) root.properties().get("data");

        assertThatThrownBy(ref::resolve)
            .isInstanceOf(SchemaParseException.class)
            .hasMessageContaining("Circular $ref detected");
    }

    @Test
    void resolve_shouldStillWork_forExistingDefsRefs() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-ref.schema.yaml");
        ObjectType root = schema.requireObject();
        RefType addressRef = (RefType) root.properties().get("address");

        assertThatCode(addressRef::resolve).doesNotThrowAnyException();
        assertThat(addressRef.resolve()).isInstanceOf(ObjectType.class);
    }
}
