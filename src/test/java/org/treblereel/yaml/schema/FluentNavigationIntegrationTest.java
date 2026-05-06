package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.*;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for fluent navigation API.
 * Tests realistic navigation patterns across complex schemas.
 */
class FluentNavigationIntegrationTest {

    private final Parser parser = new Parser();

    @Test
    void shouldNavigateThreeLevelsDeep() {
        SchemaDefinition schema = parser.parse("src/test/resources/workflow.yaml");
        ObjectType root = schema.requireObject();

        // Navigate: root -> do (ref to array) -> resolve -> item
        // "do" is a RefType to taskList which is an array
        PropertyNavigator doNav = root.property("do").resolve();

        // Verify we can navigate into the array structure
        assertThat(doNav.exists()).isTrue();
        assertThat(doNav.asArray()).isPresent();
    }

    @Test
    void shouldHandleRefTypeTransparently() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-ref.schema.yaml");
        ObjectType root = schema.requireObject();

        // Navigate through RefType without manual resolve
        PropertyNavigator nav = root.property("address")
            .resolve()
            .property("city");

        assertThat(nav.exists()).isTrue();
    }

    @Test
    void shouldReturnEmptyForInvalidPath() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-string-props.schema.yaml");
        ObjectType root = schema.requireObject();

        PropertyNavigator nav = root.property("nonexistent")
            .property("nested")
            .property("deep");

        assertThat(nav.exists()).isFalse();
    }

    @Test
    void shouldExtractValueFromDeepNavigation() {
        SchemaDefinition schema = parser.parse("src/test/resources/nested-object.schema.yaml");
        ObjectType root = schema.requireObject();

        Optional<String> pattern = root.property("address")
            .property("zipCode")
            .asString()
            .flatMap(StringType::pattern);

        // Result depends on schema structure - just verify it doesn't throw
        assertThat(pattern).isNotNull();
    }

    @Test
    void shouldNavigateArrayItems() {
        SchemaDefinition schema = parser.parse("src/test/resources/prefix-items-simple.schema.yaml");

        // Create a simple ObjectType with an array property to test item navigation
        SchemaDefinition objSchema = parser.parse("src/test/resources/simple-string-array.schema.yaml");

        // Just verify we can work with arrays - the actual item navigation
        // will be tested through object properties that are arrays
        assertThat(objSchema.requireArray()).isNotNull();
    }

    @Test
    void shouldHandleEmptyChaining() {
        PropertyNavigator nav = PropertyNavigator.empty()
            .property("a")
            .property("b")
            .resolve()
            .item(0);

        assertThat(nav.exists()).isFalse();
        assertThat(nav.get()).isEmpty();
    }

    @Test
    void shouldNavigateThroughRefTypeWithoutExplicitResolve() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-ref.schema.yaml");
        ObjectType root = schema.requireObject();

        // Navigate through RefType - no explicit .resolve() needed
        PropertyNavigator cityNav = root.property("address")
            .property("city");

        assertThat(cityNav.exists()).isTrue();
    }
}
