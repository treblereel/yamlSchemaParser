package org.treblereel.yaml.schema;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.model.Discriminator;

import java.util.LinkedHashMap;
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
}
