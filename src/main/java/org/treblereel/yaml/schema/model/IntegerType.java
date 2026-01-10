package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

public record IntegerType(JsonNode node) implements HasType {
}
