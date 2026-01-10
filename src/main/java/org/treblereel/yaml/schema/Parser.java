package org.treblereel.yaml.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.treblereel.yaml.schema.model.SchemaDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class Parser {

  private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

  private JsonNode load(URI uri) {
    try (InputStream in = uri.toURL().openStream()) {
      return mapper.readTree(in);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public SchemaDefinition parse(String file) {
    return parse(new File(file));
  }

  public SchemaDefinition parse(File file) {
    return process(file.toURI());
  }

  private SchemaDefinition process(URI uri) {
    JsonNode root = load(uri);
    return new SchemaDefinition(root);
  }
}
