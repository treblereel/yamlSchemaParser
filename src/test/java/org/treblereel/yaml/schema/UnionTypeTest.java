package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.UnionType;
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
        assertThat(unionType.getTypes()).containsExactly("string", "null");
    }
}
