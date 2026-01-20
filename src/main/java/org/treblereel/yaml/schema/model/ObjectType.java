package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class ObjectType implements HasType {

  private final SchemaDefinition schema;
  private final JsonNode node;

  private final Applicators applicators;


  public ObjectType(SchemaDefinition schema, JsonNode node) {
    this.schema = schema;
    this.node = node;
    applicators = new Applicators(schema, node);
  }

  public Optional<Boolean> unevaluatedProperties() {
    return Optional.ofNullable(node.get("unevaluatedProperties")).map(JsonNode::asBoolean);
  }

  public Optional<List<HasType>> oneOf() {
    return applicators.oneOf();
  }

  public Optional<List<HasType>> anyOf() {
    return applicators.anyOf();
  }

  public Optional<List<HasType>> allOf() {
    return applicators.allOf();
  }

  public Optional<Map<String, HasType>> properties() {
    return Optional.ofNullable(node.get("properties"))
            .filter(JsonNode::isObject)
            .map(propsNode -> {
              Map<String, HasType> properties = new TreeMap<>(String::compareTo);
              propsNode.fields().forEachRemaining(entry ->
                      properties.put(entry.getKey(), NodeFactory.resolveType(schema, entry.getValue())));
              return properties;
            });
  }

  public Optional<AdditionalProperties> additionalProperties() {
    return Optional.ofNullable(node.get("additionalProperties")).map(AdditionalProperties::new);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(node.get("description")).map(JsonNode::asText);
  }

  public Optional<String> getTitle() {
    return Optional.ofNullable(node.get("title")).map(JsonNode::asText);
  }

  public Optional<List<String>> required() {
    return Optional.ofNullable(node.get("required")).filter(n -> n.isArray())
            .map(array -> {
              List<String> required = new ArrayList<>();
              array.forEach(p -> required.add(p.asText()));
              return required;
            });
  }

}
