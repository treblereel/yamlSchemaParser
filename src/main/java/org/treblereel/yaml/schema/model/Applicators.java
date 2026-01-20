package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

record Applicators(SchemaDefinition schema, JsonNode node) {

  public Optional<List<HasType>> oneOf() {
    return Optional.ofNullable(node.get("oneOf"))
            .map(oneOf -> {
              if (!oneOf.isArray()) {
                throw new IllegalStateException("Expected 'oneOf' to be an array");
              }

              List<HasType> results = new ArrayList<>();
              for (JsonNode element : oneOf) {
                results.add(NodeFactory.resolveType(schema, element));
              }
              return results;
            });
  }

  Optional<List<HasType>> anyOf() {
    return Optional.ofNullable(node.get("anyOf"))
            .map(anyOf -> {
              if (!anyOf.isArray()) {
                throw new IllegalStateException("Expected 'anyOf' to be an array");
              }

              List<HasType> results = new ArrayList<>();
              Iterator<JsonNode> iterator = anyOf.elements();

              while (iterator.hasNext()) {
                results.add(
                        NodeFactory.resolveType(schema, iterator.next())
                );
              }

              return results;
            });
  }

  Optional<List<HasType>> allOf() {
    return Optional.ofNullable(node.get("allOf"))
            .map(allOf -> {
              if (!allOf.isArray()) {
                throw new IllegalStateException("Expected 'allOf' to be an array");
              }

              List<HasType> results = new ArrayList<>();
              Iterator<JsonNode> iterator = allOf.elements();

              while (iterator.hasNext()) {
                results.add(
                        NodeFactory.resolveType(schema, iterator.next())
                );
              }

              return results;
            });
  }

  Optional<HasType> getNot() {
    return Optional.ofNullable(node.get("not"))
            .map(n -> NodeFactory.resolveType(schema, n));
  }
}
