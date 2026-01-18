package org.treblereel.yaml.schema.generator;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.Parser;
import org.treblereel.yaml.schema.model.AllOfType;
import org.treblereel.yaml.schema.model.Applicators;
import org.treblereel.yaml.schema.model.ArrayType;
import org.treblereel.yaml.schema.model.HasType;
import org.treblereel.yaml.schema.model.RefType;
import org.treblereel.yaml.schema.model.SchemaDefinition;
import org.treblereel.yaml.schema.model.SimpleType;

import java.util.List;

public class ParserTest {

  @Test
  public void generatorTest() {
    //String schema = "src/main/resources/schema.yaml";
    String schema = "src/main/resources/workflow.yaml";
    SchemaDefinition definition = new Parser().parse(schema);


    System.out.println(definition.getId());
    System.out.println(definition.getSchema());
    System.out.println(definition.getTitle());
    System.out.println(definition.getObjectDefinition().getRequired());

    List<List<String>> oneOf = definition.getObjectDefinition().getOneOf();

    for (List<String> req : oneOf) {
      System.out.println("OneOf:");
      for (String r : req) {
        System.out.println(r);
      }
    }

    List<HasType> anyOf = definition.getObjectDefinition().getAnyOf();

    List<HasType> allOf = definition.getObjectDefinition().getAllOf();



    Applicators none = definition.getObjectDefinition().getNot();

    System.out.println("*******************");

    definition.getObjectDefinition().properties().forEach((k, v) -> {
      System.out.println("Property: " + k + " " + v.getClass().getSimpleName());
      if(v instanceof AllOfType allOfType) {
        inspect(allOfType);
      } else if(v instanceof ArrayType arrayType) {
        inspect(arrayType);
      } else if(v instanceof SimpleType simpleType) {
        System.out.println("SimpleType with type: " + simpleType.getType());
      } else if(v instanceof RefType refType) {
        System.out.println("RefType with ref: " + refType.getRefType());
      }

    });

  }


  private void inspect(AllOfType allOfType) {
    for (HasType hasType : allOfType.getAllOf()) {
      if(hasType instanceof SimpleType simpleType) {
        System.out.println("SimpleType found with type: " + simpleType.getDescription());
      } else if (hasType instanceof RefType refType) {
        System.out.println("RefType found with ref: " + refType.getRefType());
      } else if( hasType instanceof ArrayType arrayType) {
        inspect(arrayType);
      } else {
        inspect(hasType);
      }

      System.out.println("? " + hasType.getClass().getSimpleName());
    }
  }

  private void inspect(ArrayType arrayType) {
    System.out.println("ArrayType found " + arrayType.getItems());
  }

  private void inspect(HasType hasType) {

  }

}
