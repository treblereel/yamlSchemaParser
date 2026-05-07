package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.UnionType;
import org.treblereel.yaml.schema.model.SchemaDefinition;
import org.treblereel.yaml.schema.model.ArrayType;
import org.treblereel.yaml.schema.model.StringType;
import org.treblereel.yaml.schema.model.IntegerType;
import org.treblereel.yaml.schema.model.NullType;
import org.treblereel.yaml.schema.model.HasType;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnionTypeTest {
    @Test
    void should_parse_string_null_union() {
        String yaml = """
            type: [string, null]
            """;
        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);

        assertThat(schema.model()).isInstanceOf(UnionType.class);
        UnionType unionType = (UnionType) schema.model();
        assertThat(unionType.types()).containsExactly("string", "null");
    }

    @Test
    void should_not_break_array_schemas() {
        // Regression test: ensure array schemas still route to ArrayType
        String yaml = """
            type: array
            items:
              type: string
            """;

        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);

        assertThat(schema.model()).isInstanceOf(ArrayType.class);
        assertThat(schema.model()).isNotInstanceOf(UnionType.class);
    }

    @Test
    void should_return_true_when_nullable() {
        String yaml = """
            type: [string, null]
            """;

        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        UnionType unionType = (UnionType) schema.model();

        assertThat(unionType.isNullable()).isTrue();
    }

    @Test
    void should_return_false_when_not_nullable() {
        String yaml = """
            type: [string, integer]
            """;

        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        UnionType unionType = (UnionType) schema.model();

        assertThat(unionType.isNullable()).isFalse();
    }

    @Test
    void should_resolve_types_lazily() {
        String yaml = """
            type: [string, integer, null]
            """;

        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        UnionType unionType = (UnionType) schema.model();

        List<HasType> resolved = unionType.getResolvedTypes();

        assertThat(resolved).hasSize(3);
        assertThat(resolved.get(0)).isInstanceOf(StringType.class);
        assertThat(resolved.get(1)).isInstanceOf(IntegerType.class);
        assertThat(resolved.get(2)).isInstanceOf(NullType.class);
    }

    @Test
    void should_handle_single_element_union() {
        String yaml = """
            type: [string]
            """;
        
        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        UnionType unionType = (UnionType) schema.model();
        
        assertThat(unionType.types()).containsExactly("string");
        assertThat(unionType.isNullable()).isFalse();
    }

    @Test
    void should_preserve_type_order() {
        String yaml = """
            type: [integer, string, boolean]
            """;
        
        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        UnionType unionType = (UnionType) schema.model();
        
        assertThat(unionType.types()).containsExactly("integer", "string", "boolean");
    }

    @Test
    void should_throw_on_empty_union() {
        String yaml = """
            type: []
            """;

        Parser parser = new Parser();

        assertThatThrownBy(() -> parser.parseYaml(yaml).model())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Union type must contain at least one type");
    }

    @Test
    void should_handle_all_primitive_types() {
        String yaml = """
            type: [string, number, integer, boolean, null, object, array]
            """;
        
        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        UnionType unionType = (UnionType) schema.model();
        
        assertThat(unionType.types()).hasSize(7);
        assertThat(unionType.isNullable()).isTrue();
    }

    @Test
    void should_allow_duplicate_types() {
        // Parser doesn't validate - schema validator's job
        String yaml = """
            type: [string, string]
            """;

        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        UnionType unionType = (UnionType) schema.model();

        assertThat(unionType.types()).containsExactly("string", "string");
    }

    @Test
    void should_route_to_UnionType_not_StringType() {
        // Single-element union goes to UnionType, not StringType
        String yaml = """
            type: [string]
            """;

        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);

        assertThat(schema.model()).isInstanceOf(UnionType.class);
    }

    @Test
    void should_throw_on_unknown_type_in_union_during_resolution() {
        String yaml = """
            type: [string, unknownType]
            """;

        Parser parser = new Parser();
        SchemaDefinition schema = parser.parseYaml(yaml);
        UnionType unionType = (UnionType) schema.model();

        assertThatThrownBy(() -> unionType.getResolvedTypes())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Type not supported");
    }
}
