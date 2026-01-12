package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ObjectType implements HasType {

    private final SchemaDefinition schema;
    private final JsonNode node;

    private final Applicators applicators;


    public ObjectType(SchemaDefinition schema, JsonNode node) {
        this.schema = schema;
        this.node = node;
        applicators = new Applicators(node);
    }

    public boolean additionalProperties() {
        return node.has("additionalProperties") && node.get("additionalProperties").asBoolean();
    }

    public boolean hasUnevaluatedProperties() {
        return node.get("unevaluatedProperties") != null;
    }

    public boolean unevaluatedProperties() {
        return node.get("unevaluatedProperties").asBoolean();
    }

    public boolean hasRequired() {
        return node.get("required") != null;
    }

    public List<String> getRequired() {
        List<String> required = new ArrayList<>();
        if (hasRequired()) {
            for (JsonNode req : node.get("required")) {
                required.add(req.asText());
            }
        }
        return required;
    }

    public boolean hasOneOf() {
        return applicators.hasOneOf();
    }

    public List<List<String>> getOneOf() {
        return applicators.getOneOf();
    }

    public boolean hasAnyOf() {
        return applicators.hasAnyOf();
    }

    public List<List<String>> getAnyOf() {
        return applicators.getAnyOf();
    }

    public boolean hasAllOf() {
        return applicators.hasAllOf();
    }

    public List<List<String>> getAllOf() {
        return applicators.getAllOf();
    }

    public boolean hasNot() {
        return node.get("not") != null;
    }

    public Applicators getNot() {
        if (hasNot()) {
            return new Applicators(node.get("not"));
        }
        return null;
    }


    public Map<String, HasType> properties() {
        Map<String, HasType> properties = new TreeMap<>(String::compareTo);
        if (node != null && node.get("properties") != null) {
            if (node.has("properties") && node.get("properties") != null) {
                node.get("properties").fields().forEachRemaining(p -> properties.put(p.getKey(), NodeFactory.resolveType(schema, p.getValue())));
            }
        }
        return properties;
    }

    public static class AdditionalProperties {

        private final JsonNode node;

        public AdditionalProperties(JsonNode node) {
            this.node = node;
        }

        public boolean isBoolean() {
            return node.isBoolean();
        }

        public boolean asBoolean() {
            return node.asBoolean();
        }

        public boolean isRef() {
            return node.has("$ref");
        }

        public String getRef() {
            return node.get("$ref").asText();
        }

    }

}
