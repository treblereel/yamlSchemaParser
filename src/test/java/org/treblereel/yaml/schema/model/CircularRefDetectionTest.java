package org.treblereel.yaml.schema.model;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.Parser;

import static org.assertj.core.api.Assertions.*;
import org.treblereel.yaml.schema.model.SchemaParseException;

class CircularRefDetectionTest {

    private final Parser parser = new Parser();

    @Test
    void resolve_shouldThrow_whenDirectCycle() {
        SchemaDefinition schema = parser.parse("src/test/resources/direct-circular-ref.schema.yaml");
        ObjectType root = schema.requireObject();
        RefType ref = (RefType) root.properties().get("data");

        assertThatThrownBy(ref::resolve)
            .isInstanceOf(SchemaParseException.class)
            .hasMessageContaining("Circular $ref detected");
    }

    @Test
    void resolve_shouldThrow_whenSelfRef() {
        SchemaDefinition schema = parser.parse("src/test/resources/self-circular-ref.schema.yaml");
        ObjectType root = schema.requireObject();
        RefType ref = (RefType) root.properties().get("data");

        assertThatThrownBy(ref::resolve)
            .isInstanceOf(SchemaParseException.class)
            .hasMessageContaining("Circular $ref detected");
    }

    @Test
    void resolve_shouldSucceed_whenNonCircularRef() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-ref.schema.yaml");
        ObjectType root = schema.requireObject();
        RefType ref = (RefType) root.properties().get("address");

        HasType resolved = ref.resolve();
        assertThat(resolved).isInstanceOf(ObjectType.class);
    }

    @Test
    void resolve_shouldSucceed_whenCircularSchemaButNoRefCycle() {
        SchemaDefinition schema = parser.parse("src/test/resources/circular-refs.schema.yaml");
        ObjectType root = schema.requireObject();
        RefType personRef = (RefType) root.properties().get("person");

        HasType resolved = personRef.resolve();
        assertThat(resolved).isInstanceOf(ObjectType.class);

        ObjectType personObj = (ObjectType) resolved;
        assertThat(personObj.properties()).containsKey("name");
        assertThat(personObj.properties()).containsKey("friend");

        RefType friendRef = (RefType) personObj.properties().get("friend");
        HasType friendResolved = friendRef.resolve();
        assertThat(friendResolved).isInstanceOf(ObjectType.class);
    }

    @Test
    void resolve_shouldSucceed_whenTripleRefChainNoCycle() {
        SchemaDefinition schema = parser.parse("src/test/resources/triple-ref-chain.schema.yaml");
        ObjectType root = schema.requireObject();

        RefType refA = (RefType) root.properties().get("a");
        HasType resolvedA = refA.resolve();
        assertThat(resolvedA).isInstanceOf(ObjectType.class);

        ObjectType typeA = (ObjectType) resolvedA;
        RefType refB = (RefType) typeA.properties().get("b");
        HasType resolvedB = refB.resolve();
        assertThat(resolvedB).isInstanceOf(ObjectType.class);

        ObjectType typeB = (ObjectType) resolvedB;
        RefType refC = (RefType) typeB.properties().get("c");
        HasType resolvedC = refC.resolve();
        assertThat(resolvedC).isInstanceOf(ObjectType.class);
    }

    @Test
    void resolve_shouldCleanUpAfterCycleDetection() {
        SchemaDefinition circularSchema = parser.parse("src/test/resources/direct-circular-ref.schema.yaml");
        ObjectType circularRoot = circularSchema.requireObject();
        RefType circularRef = (RefType) circularRoot.properties().get("data");

        assertThatThrownBy(circularRef::resolve)
            .isInstanceOf(SchemaParseException.class);

        SchemaDefinition simpleSchema = parser.parse("src/test/resources/simple-ref.schema.yaml");
        ObjectType simpleRoot = simpleSchema.requireObject();
        RefType simpleRef = (RefType) simpleRoot.properties().get("address");

        assertThatCode(simpleRef::resolve).doesNotThrowAnyException();
    }
}
