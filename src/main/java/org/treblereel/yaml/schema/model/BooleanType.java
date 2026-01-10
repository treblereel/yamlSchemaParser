package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

public record BooleanType(JsonNode node) implements HasType {
}
