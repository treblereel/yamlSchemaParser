package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.UnionType;
import org.treblereel.yaml.schema.model.SchemaDefinition;
import org.treblereel.yaml.schema.model.ArrayType;
import static org.assertj.core.api.Assertions.assertThat;

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
}
