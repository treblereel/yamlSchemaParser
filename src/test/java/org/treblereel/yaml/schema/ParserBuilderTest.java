package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.SchemaDefinition;
import static org.assertj.core.api.Assertions.*;

class ParserBuilderTest {

    @Test
    void builder_shouldCreateParser() {
        Parser parser = Parser.builder().build();

        assertThat(parser).isNotNull();
    }

    @Test
    void builder_shouldSupportStrictMode() {
        Parser parser = Parser.builder()
            .strictMode(true)
            .build();

        assertThat(parser).isNotNull();
    }

    @Test
    void builder_shouldSupportResolveReferences() {
        Parser parser = Parser.builder()
            .resolveReferences(false)
            .build();

        assertThat(parser).isNotNull();
    }

    @Test
    void builder_shouldSupportChaining() {
        Parser parser = Parser.builder()
            .strictMode(true)
            .resolveReferences(false)
            .build();

        assertThat(parser).isNotNull();
    }

    @Test
    void defaultConstructor_shouldStillWork() {
        Parser parser = new Parser();
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");

        assertThat(schema).isNotNull();
    }

    @Test
    void builtParser_shouldParse() {
        Parser parser = Parser.builder().build();
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");

        assertThat(schema).isNotNull();
    }
}
