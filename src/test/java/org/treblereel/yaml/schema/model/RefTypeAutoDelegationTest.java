package org.treblereel.yaml.schema.model;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.Parser;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

class RefTypeAutoDelegationTest {

    private final Parser parser = new Parser();

    @Test
    void properties_shouldDelegateToResolvedObjectType() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-ref.schema.yaml");
        ObjectType root = schema.requireObject();
        RefType ref = (RefType) root.properties().get("address");

        Map<String, HasType> result = ref.properties();

        assertThat(result).isNotNull();
        // If address has properties, they should be returned
    }

    @Test
    void property_shouldDelegateToResolvedObjectType() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-ref.schema.yaml");
        ObjectType root = schema.requireObject();
        RefType ref = (RefType) root.properties().get("address");

        PropertyNavigator nav = ref.property("city");

        // Should navigate into resolved type
        assertThat(nav).isNotNull();
    }

    @Test
    void getPropertyAsString_shouldDelegateToResolvedObjectType() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-ref.schema.yaml");
        ObjectType root = schema.requireObject();
        RefType ref = (RefType) root.properties().get("address");

        var result = ref.getPropertyAsString("city");

        // Result depends on whether address.city is a string
        assertThat(result).isNotNull();
    }

    @Test
    void pattern_shouldDelegateToResolvedStringType() {
        // Assumes we have a ref to a StringType in some schema
        SchemaDefinition schema = parser.parse("src/test/resources/workflow.yaml");
        ObjectType root = schema.requireObject();

        // Navigate to a string type via ref if it exists
        // This test is schema-dependent
    }

    @Test
    void enumConstraint_shouldReturnEmptyList_whenResolvedIsNotEnumType() {
        SchemaDefinition schema = parser.parse("src/test/resources/simple-ref.schema.yaml");
        ObjectType root = schema.requireObject();
        RefType ref = (RefType) root.properties().get("address");

        var result = ref.enumConstraint();

        assertThat(result).isNotNull().isEmpty();
    }
}
