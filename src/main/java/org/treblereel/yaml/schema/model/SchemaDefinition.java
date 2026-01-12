package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

public record SchemaDefinition(JsonNode node) {

    public String getId() {
        if (node.get("$id") == null) {
            return null;
        }
        return node.get("$id").asText();
    }

    public String getSchema() {
        if (node.get("$schema") == null) {
            return null;
        }
        return node.get("$schema").asText();
    }

    public String getTitle() {
        if (node.get("title") == null) {
            return null;
        }
        return node.get("title").asText();
    }

    public ObjectType getObjectDefinition() {
        return new ObjectType(this, node);
    }

    public Map<String, ObjectType> getDefinitions() {
        if (node.get("$defs") == null) {
            return Collections.emptyMap();
        }
        Map<String, ObjectType> results = new HashMap<>();
        JsonNode definitionsNode = node.get("$defs");
        Iterator<Map.Entry<String, JsonNode>> iter = definitionsNode.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> entry = iter.next();
            results.put(entry.getKey(), new ObjectType(this, entry.getValue()));
        }
        return results;
    }

}
