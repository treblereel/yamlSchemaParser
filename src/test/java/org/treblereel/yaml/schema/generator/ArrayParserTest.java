package org.treblereel.yaml.schema.generator;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.Parser;
import org.treblereel.yaml.schema.model.ArrayItemType;
import org.treblereel.yaml.schema.model.ArrayType;
import org.treblereel.yaml.schema.model.BooleanType;
import org.treblereel.yaml.schema.model.HasType;
import org.treblereel.yaml.schema.model.IntegerType;
import org.treblereel.yaml.schema.model.NullType;
import org.treblereel.yaml.schema.model.NumberType;
import org.treblereel.yaml.schema.model.ObjectType;
import org.treblereel.yaml.schema.model.SchemaDefinition;
import org.treblereel.yaml.schema.model.StringType;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArrayParserTest {

  @Test
  public void testSimpleStringArray() {
    String schema = "src/test/resources/simple-string-array.schema.yaml";
    SchemaDefinition definition = new Parser().parse(schema);
    HasType type = definition.model();
    assertInstanceOf(ArrayType.class, type);
    ArrayType arrayType = (ArrayType) type;
    assertEquals(1, arrayType.getItems().length);
    ArrayItemType itemType = arrayType.getItems()[0];
    assertInstanceOf(StringType.class, itemType.getType());
  }


  @Test
  public void testBoundedIntegerArray() {
    String schema = "src/test/resources/bounded-integer-array.schema.yaml";
    SchemaDefinition definition = new Parser().parse(schema);
    HasType type = definition.model();
    assertInstanceOf(ArrayType.class, type);
    ArrayType arrayType = (ArrayType) type;
    assertTrue(arrayType.minItems().isPresent());
    assertEquals(1, arrayType.minItems().get());
    assertTrue(arrayType.maxItems().isPresent());
    assertEquals(5, arrayType.maxItems().get());
    assertEquals(1, arrayType.getItems().length);
    ArrayItemType itemType = arrayType.getItems()[0];
    assertInstanceOf(IntegerType.class, itemType.getType());
  }

  @Test
  public void testObjectArrayWithRequiredFields() {
    String schema = "src/test/resources/object-array-required-fields.schema.yaml";
    SchemaDefinition definition = new Parser().parse(schema);
    HasType type = definition.model();
    assertInstanceOf(ArrayType.class, type);
    ArrayType arrayType = (ArrayType) type;
    assertEquals(1, arrayType.getItems().length);
    ArrayItemType itemType = arrayType.getItems()[0];
    assertInstanceOf(ObjectType.class, itemType.getType());
    ObjectType objectType = (ObjectType) itemType.getType();
    assertTrue(objectType.properties().isPresent());
    assertEquals(3, objectType.properties().get().size());
    assertTrue(objectType.required().isPresent());
    assertEquals(2, objectType.required().get().size());
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> properties = objectType.properties().get();
    assertTrue(properties.containsKey("id"));
    assertInstanceOf(IntegerType.class, properties.get("id"));
    assertTrue(properties.containsKey("name"));
    assertInstanceOf(StringType.class, properties.get("name"));
    assertTrue(properties.containsKey("active"));
    assertInstanceOf(BooleanType.class, properties.get("active"));
  }

  @Test
  public void testNestedNumberArray() {
    String schema = "src/test/resources/nested-number-array.schema.yaml";
    SchemaDefinition definition = new Parser().parse(schema);
    HasType type = definition.model();
    assertInstanceOf(ArrayType.class, type);
    ArrayType arrayType = (ArrayType) type;
    assertEquals(1, arrayType.getItems().length);
    ArrayItemType itemType = arrayType.getItems()[0];
    assertInstanceOf(ArrayType.class, itemType.getType());
    ArrayType nestedArrayType = (ArrayType) itemType.getType();
    assertEquals(1, nestedArrayType.getItems().length);
    ArrayItemType nestedItemType = nestedArrayType.getItems()[0];
    assertInstanceOf(NumberType.class, nestedItemType.getType());
  }

  @Test
  public void testTupleArrayValidation() {
    String schema = "src/test/resources/tuple-array-validation.schema.yaml";
    SchemaDefinition definition = new Parser().parse(schema);
    HasType type = definition.model();
    assertInstanceOf(ArrayType.class, type);
    ArrayType arrayType = (ArrayType) type;
    assertEquals(3, arrayType.getItems().length);
    assertInstanceOf(StringType.class, arrayType.getItems()[0].getType());
    assertInstanceOf(IntegerType.class, arrayType.getItems()[1].getType());
    assertInstanceOf(BooleanType.class, arrayType.getItems()[2].getType());
    assertTrue(arrayType.additionalItems().isPresent());
    assertFalse(arrayType.additionalItems().get());
  }

  @Test
  public void testObjectArrayWithNestedArrays() {
    String schema = "src/test/resources/object-array-with-nested-arrays.schema.yaml";
    SchemaDefinition definition = new Parser().parse(schema);
    HasType type = definition.model();
    assertInstanceOf(ArrayType.class, type);
    ArrayType arrayType = (ArrayType) type;
    assertEquals(1, arrayType.getItems().length);
    ArrayItemType itemType = arrayType.getItems()[0];
    assertInstanceOf(ObjectType.class, itemType.getType());
    ObjectType objectType = (ObjectType) itemType.getType();
    assertTrue(objectType.properties().isPresent());
    assertEquals(2, objectType.properties().get().size());
    Map<String, HasType> properties = objectType.properties().get();
    assertTrue(properties.containsKey("tags"));
    assertInstanceOf(ArrayType.class, properties.get("tags"));
    ArrayType tagsArray = (ArrayType) properties.get("tags");
    assertEquals(1, tagsArray.getItems().length);
    assertInstanceOf(StringType.class, tagsArray.getItems()[0].getType());
    assertTrue(properties.containsKey("scores"));
    assertInstanceOf(ArrayType.class, properties.get("scores"));
    ArrayType scoresArray = (ArrayType) properties.get("scores");
    assertEquals(1, scoresArray.getItems().length);
    assertInstanceOf(NumberType.class, scoresArray.getItems()[0].getType());
  }

  @Test
  public void testArrayWithOneOfItems() {
    String schema = "src/test/resources/array-with-oneof-items.schema.yaml";
    SchemaDefinition definition = new Parser().parse(schema);
    HasType type = definition.model();
    assertInstanceOf(ArrayType.class, type);
    ArrayType arrayType = (ArrayType) type;
    assertEquals(1, arrayType.getItems().length);
    ArrayItemType itemType = arrayType.getItems()[0];
    assertTrue(itemType.oneOf().isPresent());
    assertEquals(2, itemType.oneOf().get().size());
  }

  @Test
  public void testUniqueItemsArray() {
    String schema = "src/test/resources/unique-items-array.schema.yaml";
    SchemaDefinition definition = new Parser().parse(schema);
    HasType type = definition.model();
    assertInstanceOf(ArrayType.class, type);
    ArrayType arrayType = (ArrayType) type;
    assertTrue(arrayType.uniqueItems().isPresent());
    assertTrue(arrayType.uniqueItems().get());
    assertEquals(1, arrayType.getItems().length);
    ArrayItemType itemType = arrayType.getItems()[0];
    assertInstanceOf(StringType.class, itemType.getType());
  }

  @Test
  public void testDeepNestedArrayStructure() {
    String schema = "src/test/resources/deep-nested-array-structure.schema.yaml";
    SchemaDefinition definition = new Parser().parse(schema);
    HasType type = definition.model();
    assertInstanceOf(ArrayType.class, type);
    ArrayType arrayType = (ArrayType) type;
    assertEquals(1, arrayType.getItems().length);
    ArrayItemType itemType = arrayType.getItems()[0];
    assertInstanceOf(ObjectType.class, itemType.getType());
    ObjectType objectType = (ObjectType) itemType.getType();
    assertTrue(objectType.properties().isPresent());
    assertEquals(1, objectType.properties().get().size());
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> properties = objectType.properties().get();
    assertTrue(properties.containsKey("matrix"));
    assertInstanceOf(ArrayType.class, properties.get("matrix"));
    ArrayType level1 = (ArrayType) properties.get("matrix");
    assertEquals(1, level1.getItems().length);
    assertInstanceOf(ArrayType.class, level1.getItems()[0].getType());
    ArrayType level2 = (ArrayType) level1.getItems()[0].getType();
    assertEquals(1, level2.getItems().length);
    assertInstanceOf(ArrayType.class, level2.getItems()[0].getType());
    ArrayType level3 = (ArrayType) level2.getItems()[0].getType();
    assertEquals(1, level3.getItems().length);
    assertInstanceOf(IntegerType.class, level3.getItems()[0].getType());
  }

  @Test
  public void testNullableItemsArray() {
    String schema = "src/test/resources/nullable-items-array.schema.yaml";
    SchemaDefinition definition = new Parser().parse(schema);
    HasType type = definition.model();
    assertInstanceOf(ArrayType.class, type);
    ArrayType arrayType = (ArrayType) type;
    assertEquals(1, arrayType.getItems().length);
    ArrayItemType itemType = arrayType.getItems()[0];
    assertInstanceOf(ArrayType.class, itemType.getType());
  }

  @Test
  public void testBooleanArray() {
    String schema = "src/test/resources/boolean-array.schema.yaml";
    SchemaDefinition definition = new Parser().parse(schema);
    HasType type = definition.model();
    assertInstanceOf(ArrayType.class, type);
    ArrayType arrayType = (ArrayType) type;
    assertEquals(1, arrayType.getItems().length);
    ArrayItemType itemType = arrayType.getItems()[0];
    assertInstanceOf(BooleanType.class, itemType.getType());
  }

  @Test
  public void testEmptyArrayConstraint() {
    String schema = "src/test/resources/empty-array-constraint.schema.yaml";
    SchemaDefinition definition = new Parser().parse(schema);
    HasType type = definition.model();
    assertInstanceOf(ArrayType.class, type);
    ArrayType arrayType = (ArrayType) type;
    assertTrue(arrayType.minItems().isPresent());
    assertEquals(0, arrayType.minItems().get());
    assertTrue(arrayType.maxItems().isPresent());
    assertEquals(0, arrayType.maxItems().get());
    assertEquals(1, arrayType.getItems().length);
    assertInstanceOf(StringType.class, arrayType.getItems()[0].getType());
  }

  @Test
  public void testArrayWithAnyOfItems() {
    String schema = "src/test/resources/array-with-anyof-items.schema.yaml";
    SchemaDefinition definition = new Parser().parse(schema);
    HasType type = definition.model();
    assertInstanceOf(ArrayType.class, type);
    ArrayType arrayType = (ArrayType) type;
    assertEquals(1, arrayType.getItems().length);
    ArrayItemType itemType = arrayType.getItems()[0];
    assertTrue(itemType.anyOf().isPresent());
    assertEquals(3, itemType.anyOf().get().size());
    assertInstanceOf(StringType.class, itemType.anyOf().get().get(0));
    assertInstanceOf(NumberType.class, itemType.anyOf().get().get(1));
    assertInstanceOf(BooleanType.class, itemType.anyOf().get().get(2));
  }

  @Test
  public void testMixedTupleTypes() {
    String schema = "src/test/resources/mixed-tuple-types.schema.yaml";
    SchemaDefinition definition = new Parser().parse(schema);
    HasType type = definition.model();
    assertInstanceOf(ArrayType.class, type);
    ArrayType arrayType = (ArrayType) type;
    assertEquals(5, arrayType.getItems().length);
    assertInstanceOf(StringType.class, arrayType.getItems()[0].getType());
    assertInstanceOf(NumberType.class, arrayType.getItems()[1].getType());
    assertInstanceOf(BooleanType.class, arrayType.getItems()[2].getType());
    assertInstanceOf(IntegerType.class, arrayType.getItems()[3].getType());
    assertInstanceOf(ObjectType.class, arrayType.getItems()[4].getType());
    ObjectType objectType = (ObjectType) arrayType.getItems()[4].getType();
    assertTrue(objectType.properties().isPresent());
    assertTrue(objectType.properties().get().containsKey("name"));
  }

  @Test
  public void testArrayMinMaxConstraints() {
    String schema = "src/test/resources/array-minmax-constraints.schema.yaml";
    SchemaDefinition definition = new Parser().parse(schema);
    HasType type = definition.model();
    assertInstanceOf(ArrayType.class, type);
    ArrayType arrayType = (ArrayType) type;
    assertTrue(arrayType.minItems().isPresent());
    assertEquals(2, arrayType.minItems().get());
    assertTrue(arrayType.maxItems().isPresent());
    assertEquals(10, arrayType.maxItems().get());
    assertTrue(arrayType.uniqueItems().isPresent());
    assertTrue(arrayType.uniqueItems().get());
    assertEquals(1, arrayType.getItems().length);
    assertInstanceOf(IntegerType.class, arrayType.getItems()[0].getType());
  }

  @Test
  public void testNestedObjectArray() {
    String schema = "src/test/resources/nested-object-array.schema.yaml";
    SchemaDefinition definition = new Parser().parse(schema);
    HasType type = definition.model();
    assertInstanceOf(ArrayType.class, type);
    ArrayType arrayType = (ArrayType) type;
    assertEquals(1, arrayType.getItems().length);
    assertInstanceOf(ObjectType.class, arrayType.getItems()[0].getType());
    ObjectType objectType = (ObjectType) arrayType.getItems()[0].getType();
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> properties = objectType.properties().get();
    assertEquals(2, properties.size());
    assertTrue(properties.containsKey("user"));
    assertInstanceOf(ObjectType.class, properties.get("user"));
    ObjectType userObject = (ObjectType) properties.get("user");
    assertTrue(userObject.properties().isPresent());
    assertTrue(userObject.properties().get().containsKey("id"));
    assertTrue(userObject.properties().get().containsKey("email"));
    assertTrue(userObject.required().isPresent());
    assertTrue(userObject.required().get().contains("id"));
    assertTrue(properties.containsKey("roles"));
    assertInstanceOf(ArrayType.class, properties.get("roles"));
  }

  @Test
  public void testArrayWithAllOfItems() {
    String schema = "src/test/resources/array-with-allof-items.schema.yaml";
    SchemaDefinition definition = new Parser().parse(schema);
    HasType type = definition.model();
    assertInstanceOf(ArrayType.class, type);
    ArrayType arrayType = (ArrayType) type;
    assertEquals(1, arrayType.getItems().length);
    ArrayItemType itemType = arrayType.getItems()[0];
    assertTrue(itemType.allOf().isPresent());
    assertEquals(2, itemType.allOf().get().size());
    assertInstanceOf(ObjectType.class, itemType.allOf().get().get(0));
    assertInstanceOf(ObjectType.class, itemType.allOf().get().get(1));
  }

  @Test
  public void testArrayOnlyMinItems() {
    String schema = "src/test/resources/array-only-minItems.schema.yaml";
    SchemaDefinition definition = new Parser().parse(schema);
    HasType type = definition.model();
    assertInstanceOf(ArrayType.class, type);
    ArrayType arrayType = (ArrayType) type;
    assertTrue(arrayType.minItems().isPresent());
    assertEquals(5, arrayType.minItems().get());
    assertFalse(arrayType.maxItems().isPresent());
    assertEquals(1, arrayType.getItems().length);
    assertInstanceOf(NumberType.class, arrayType.getItems()[0].getType());
  }

  @Test
  public void testArrayOnlyMaxItems() {
    String schema = "src/test/resources/array-only-maxItems.schema.yaml";
    SchemaDefinition definition = new Parser().parse(schema);
    HasType type = definition.model();
    assertInstanceOf(ArrayType.class, type);
    ArrayType arrayType = (ArrayType) type;
    assertFalse(arrayType.minItems().isPresent());
    assertTrue(arrayType.maxItems().isPresent());
    assertEquals(100, arrayType.maxItems().get());
    assertEquals(1, arrayType.getItems().length);
    assertInstanceOf(StringType.class, arrayType.getItems()[0].getType());
  }

  @Test
  public void testTripleNestedArray() {
    String schema = "src/test/resources/triple-nested-array.schema.yaml";
    SchemaDefinition definition = new Parser().parse(schema);
    HasType type = definition.model();
    assertInstanceOf(ArrayType.class, type);
    ArrayType level1 = (ArrayType) type;
    assertEquals(1, level1.getItems().length);
    assertInstanceOf(ArrayType.class, level1.getItems()[0].getType());
    ArrayType level2 = (ArrayType) level1.getItems()[0].getType();
    assertEquals(1, level2.getItems().length);
    assertInstanceOf(ArrayType.class, level2.getItems()[0].getType());
    ArrayType level3 = (ArrayType) level2.getItems()[0].getType();
    assertEquals(1, level3.getItems().length);
    assertInstanceOf(StringType.class, level3.getItems()[0].getType());
  }
}
