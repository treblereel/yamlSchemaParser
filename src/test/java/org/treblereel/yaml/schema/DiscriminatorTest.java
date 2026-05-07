package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.Discriminator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscriminatorTest {

    @Test
    void should_create_discriminator_with_propertyName_and_mapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("cat", "#/components/schemas/Cat");
        mapping.put("dog", "#/components/schemas/Dog");

        Discriminator disc = new Discriminator("petType", mapping);

        assertThat(disc.propertyName()).isEqualTo("petType");
        assertThat(disc.mapping()).containsEntry("cat", "#/components/schemas/Cat");
        assertThat(disc.mapping()).containsEntry("dog", "#/components/schemas/Dog");
    }

    @Test
    void should_throw_when_propertyName_is_null() {
        assertThatThrownBy(() -> new Discriminator(null, Map.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Discriminator must have propertyName field");
    }

    @Test
    void should_throw_when_propertyName_is_blank() {
        assertThatThrownBy(() -> new Discriminator("", Map.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Discriminator must have propertyName field");
        
        assertThatThrownBy(() -> new Discriminator("   ", Map.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Discriminator must have propertyName field");
    }

    @Test
    void should_accept_empty_mapping() {
        // mapping is optional per OpenAPI spec
        Discriminator disc = new Discriminator("petType", Map.of());
        
        assertThat(disc.propertyName()).isEqualTo("petType");
        assertThat(disc.mapping()).isEmpty();
    }

    @Test
    void should_accept_null_mapping_as_empty() {
        Discriminator disc = new Discriminator("petType", null);
        
        assertThat(disc.mapping()).isEmpty();
        assertThat(disc.mapping()).isEqualTo(Map.of());
    }

    @Test
    void should_preserve_mapping_order() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("zebra", "#/components/schemas/Zebra");
        mapping.put("apple", "#/components/schemas/Apple");
        mapping.put("middle", "#/components/schemas/Middle");
        
        Discriminator disc = new Discriminator("type", mapping);
        
        List<String> keys = new ArrayList<>(disc.mapping().keySet());
        assertThat(keys).containsExactly("zebra", "apple", "middle");
    }
}
