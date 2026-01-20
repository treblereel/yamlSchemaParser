package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

public record NullType(JsonNode node) implements HasType {
}
