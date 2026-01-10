package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

public record NumberType(JsonNode node) implements HasType {
}
