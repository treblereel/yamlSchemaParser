package org.treblereel.yaml.schema.generator;

import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.Parser;
import org.treblereel.yaml.schema.model.AllOfType;
import org.treblereel.yaml.schema.model.ArrayType;
import org.treblereel.yaml.schema.model.BooleanType;
import org.treblereel.yaml.schema.model.HasType;
import org.treblereel.yaml.schema.model.IntegerType;
import org.treblereel.yaml.schema.model.NumberType;
import org.treblereel.yaml.schema.model.ObjectType;
import org.treblereel.yaml.schema.model.RefType;
import org.treblereel.yaml.schema.model.SchemaDefinition;
import org.treblereel.yaml.schema.model.StringType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ObjectParserTest {

  private static final String BASE_PATH = "src/test/resources/";

  @Test
  public void testSimpleStringProps() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "simple-string-props.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertEquals(2, props.size());
    assertTrue(props.containsKey("firstName"));
    assertTrue(props.containsKey("lastName"));
    assertInstanceOf(StringType.class, props.get("firstName"));
    assertInstanceOf(StringType.class, props.get("lastName"));
  }

  @Test
  public void testSimpleIntegerProps() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "simple-integer-props.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertEquals(2, props.size());
    assertInstanceOf(IntegerType.class, props.get("age"));
    assertInstanceOf(IntegerType.class, props.get("count"));
  }

  @Test
  public void testSimpleBooleanProps() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "simple-boolean-props.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertEquals(2, props.size());
    assertInstanceOf(BooleanType.class, props.get("active"));
    assertInstanceOf(BooleanType.class, props.get("verified"));
  }

  @Test
  public void testSimpleNumberProps() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "simple-number-props.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertEquals(2, props.size());
    assertInstanceOf(NumberType.class, props.get("price"));
    assertInstanceOf(NumberType.class, props.get("rate"));
  }

  @Test
  public void testMixedTypesProps() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "mixed-types-props.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertEquals(4, props.size());
    assertInstanceOf(StringType.class, props.get("name"));
    assertInstanceOf(IntegerType.class, props.get("age"));
    assertInstanceOf(NumberType.class, props.get("score"));
    assertInstanceOf(BooleanType.class, props.get("active"));
  }

  @Test
  public void testRequiredFields() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "required-fields.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.required().isPresent());
    List<String> required = objectType.required().get();
    assertEquals(2, required.size());
    assertTrue(required.contains("id"));
    assertTrue(required.contains("name"));
    assertFalse(required.contains("description"));
  }

  @Test
  public void testAllRequired() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "all-required.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.required().isPresent());
    assertTrue(objectType.properties().isPresent());
    assertEquals(objectType.properties().get().size(), objectType.required().get().size());
  }

  @Test
  public void testNestedObject() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "nested-object.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertTrue(props.containsKey("user"));
    assertInstanceOf(ObjectType.class, props.get("user"));
    ObjectType userType = (ObjectType) props.get("user");
    assertTrue(userType.properties().isPresent());
    assertTrue(userType.properties().get().containsKey("name"));
    assertTrue(userType.properties().get().containsKey("age"));
  }

  @Test
  public void testDeepNestedObject() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "deep-nested-object.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType level0 = (ObjectType) type;
    assertTrue(level0.properties().isPresent());
    ObjectType level1 = (ObjectType) level0.properties().get().get("level1");
    assertTrue(level1.properties().isPresent());
    ObjectType level2 = (ObjectType) level1.properties().get().get("level2");
    assertTrue(level2.properties().isPresent());
    ObjectType level3 = (ObjectType) level2.properties().get().get("level3");
    assertTrue(level3.properties().isPresent());
    assertInstanceOf(StringType.class, level3.properties().get().get("value"));
  }

  @Test
  public void testWithArrayProp() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "with-array-prop.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertInstanceOf(ArrayType.class, props.get("tags"));
    assertInstanceOf(ArrayType.class, props.get("scores"));
    ArrayType tagsArray = (ArrayType) props.get("tags");
    assertInstanceOf(StringType.class, tagsArray.getItems()[0].getType());
    ArrayType scoresArray = (ArrayType) props.get("scores");
    assertInstanceOf(IntegerType.class, scoresArray.getItems()[0].getType());
  }

  @Test
  public void testSimpleRef() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "simple-ref.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertTrue(props.containsKey("address"));
    assertInstanceOf(RefType.class, props.get("address"));
    RefType refType = (RefType) props.get("address");
    assertEquals("#/$defs/Address", refType.ref());
    ObjectType addressType = (ObjectType) refType.resolve();
    assertTrue(addressType.properties().isPresent());
    assertTrue(addressType.properties().get().containsKey("street"));
    assertTrue(addressType.properties().get().containsKey("city"));
    assertTrue(addressType.properties().get().containsKey("zipCode"));
  }

  @Test
  public void testMultipleRefs() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "multiple-refs.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertEquals(3, props.size());
    assertInstanceOf(RefType.class, props.get("billing"));
    assertInstanceOf(RefType.class, props.get("shipping"));
    assertInstanceOf(RefType.class, props.get("contact"));
    RefType billingRef = (RefType) props.get("billing");
    RefType contactRef = (RefType) props.get("contact");
    assertEquals("#/$defs/Address", billingRef.ref());
    assertEquals("#/$defs/Person", contactRef.ref());
  }

  @Test
  public void testNestedRef() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "nested-ref.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    RefType companyRef = (RefType) objectType.properties().get().get("company");
    ObjectType companyType = (ObjectType) companyRef.resolve();
    assertTrue(companyType.properties().isPresent());
    assertInstanceOf(RefType.class, companyType.properties().get().get("ceo"));
    RefType ceoRef = (RefType) companyType.properties().get().get("ceo");
    ObjectType personType = (ObjectType) ceoRef.resolve();
    assertTrue(personType.properties().isPresent());
    assertTrue(personType.properties().get().containsKey("firstName"));
  }

  @Test
  public void testRefInArray() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "ref-in-array.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    ArrayType employeesArray = (ArrayType) objectType.properties().get().get("employees");
    assertInstanceOf(RefType.class, employeesArray.getItems()[0].getType());
    RefType employeeRef = (RefType) employeesArray.getItems()[0].getType();
    ObjectType employeeType = (ObjectType) employeeRef.resolve();
    assertTrue(employeeType.properties().isPresent());
    assertTrue(employeeType.properties().get().containsKey("id"));
    assertTrue(employeeType.properties().get().containsKey("name"));
    assertTrue(employeeType.properties().get().containsKey("department"));
  }

  @Test
  public void testSelfRef() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "self-ref.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    assertInstanceOf(StringType.class, objectType.properties().get().get("name"));
    assertInstanceOf(ArrayType.class, objectType.properties().get().get("children"));
    ArrayType childrenArray = (ArrayType) objectType.properties().get().get("children");
    assertInstanceOf(RefType.class, childrenArray.getItems()[0].getType());
    RefType nodeRef = (RefType) childrenArray.getItems()[0].getType();
    assertEquals("#/$defs/Node", nodeRef.ref());
  }

  @Test
  public void testAdditionalPropsFalse() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "additional-props-false.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.additionalProperties().isPresent());
    assertFalse(objectType.additionalProperties().get().isAllowed());
    assertFalse(objectType.additionalProperties().get().getType().isPresent());
  }

  @Test
  public void testAdditionalPropsTrue() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "additional-props-true.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.additionalProperties().isPresent());
    assertTrue(objectType.additionalProperties().get().isAllowed());
    assertFalse(objectType.additionalProperties().get().getType().isPresent());
  }

  @Test
  public void testAdditionalPropsTyped() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "additional-props-typed.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.additionalProperties().isPresent());
    assertTrue(objectType.additionalProperties().get().isAllowed());
    assertTrue(objectType.additionalProperties().get().getType().isPresent());
    assertInstanceOf(StringType.class, objectType.additionalProperties().get().getType().get());
  }

  @Test
  public void testWithAllOf() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "with-allof.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(AllOfType.class, type);
    AllOfType allOfType = (AllOfType) type;
    List<HasType> allOfList = allOfType.getAllOf();
    assertEquals(3, allOfList.size());
    assertInstanceOf(ObjectType.class, allOfList.get(0));
    assertInstanceOf(ObjectType.class, allOfList.get(1));
    assertInstanceOf(ObjectType.class, allOfList.get(2));
  }

  @Test
  public void testWithAnyOf() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "with-anyof.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    ObjectType contactType = (ObjectType) objectType.properties().get().get("contact");
    assertTrue(contactType.anyOf().isPresent());
    assertEquals(2, contactType.anyOf().get().size());
  }

  @Test
  public void testWithOneOf() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "with-oneof.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    ObjectType paymentType = (ObjectType) objectType.properties().get().get("payment");
    assertTrue(paymentType.oneOf().isPresent());
    assertEquals(2, paymentType.oneOf().get().size());
  }

  @Test
  public void testWithTitleDescription() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "with-title-description.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.getTitle().isPresent());
    assertEquals("User Profile", objectType.getTitle().get());
    assertTrue(objectType.getDescription().isPresent());
    assertTrue(objectType.getDescription().get().contains("user profile"));
  }

  @Test
  public void testEmptyObject() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "empty-object.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertFalse(objectType.properties().isPresent());
  }

  @Test
  public void testOnlyRequired() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "only-required.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.required().isPresent());
    assertEquals(2, objectType.required().get().size());
    assertFalse(objectType.properties().isPresent());
  }

  @Test
  public void testUnevaluatedProps() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "unevaluated-props.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.unevaluatedProperties().isPresent());
    assertFalse(objectType.unevaluatedProperties().get());
  }

  @Test
  public void testComplexNested() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "complex-nested.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType root = (ObjectType) type;
    assertTrue(root.properties().isPresent());
    ObjectType order = (ObjectType) root.properties().get().get("order");
    assertTrue(order.properties().isPresent());
    assertInstanceOf(IntegerType.class, order.properties().get().get("id"));
    ObjectType customer = (ObjectType) order.properties().get().get("customer");
    assertTrue(customer.properties().isPresent());
    ObjectType address = (ObjectType) customer.properties().get().get("address");
    assertTrue(address.properties().isPresent());
    ArrayType items = (ArrayType) order.properties().get().get("items");
    assertNotNull(items);
  }

  @Test
  public void testAllOfWithRef() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "allof-with-ref.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(AllOfType.class, type);
    AllOfType allOfType = (AllOfType) type;
    List<HasType> allOfList = allOfType.getAllOf();
    assertEquals(2, allOfList.size());
    assertInstanceOf(RefType.class, allOfList.get(0));
    assertInstanceOf(ObjectType.class, allOfList.get(1));
    RefType baseRef = (RefType) allOfList.get(0);
    ObjectType baseType = (ObjectType) baseRef.resolve();
    assertTrue(baseType.properties().isPresent());
    assertTrue(baseType.properties().get().containsKey("id"));
  }

  @Test
  public void testRefWithRequired() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "ref-with-required.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.required().isPresent());
    assertTrue(objectType.required().get().contains("person"));
    RefType personRef = (RefType) objectType.properties().get().get("person");
    ObjectType personType = (ObjectType) personRef.resolve();
    assertTrue(personType.required().isPresent());
    assertTrue(personType.required().get().contains("name"));
  }

  @Test
  public void testMultipleDefs() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "multiple-defs.schema.yaml");
    Map<String, ObjectType> defs = definition.definitions();
    assertEquals(3, defs.size());
    assertTrue(defs.containsKey("User"));
    assertTrue(defs.containsKey("Product"));
    assertTrue(defs.containsKey("Order"));
    ObjectType userDef = defs.get("User");
    assertTrue(userDef.properties().isPresent());
    assertTrue(userDef.properties().get().containsKey("id"));
  }

  @Test
  public void testArrayOfObjects() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "array-of-objects.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    ArrayType usersArray = (ArrayType) objectType.properties().get().get("users");
    ObjectType userType = (ObjectType) usersArray.getItems()[0].getType();
    assertTrue(userType.required().isPresent());
    assertTrue(userType.required().get().contains("id"));
    assertTrue(userType.properties().isPresent());
    assertInstanceOf(ArrayType.class, userType.properties().get().get("roles"));
  }

  @Test
  public void testObjectInAllOfItems() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "object-in-allof-items.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    AllOfType allOfType = (AllOfType) objectType.properties().get().get("data");
    List<HasType> allOfList = allOfType.getAllOf();
    assertEquals(2, allOfList.size());
    assertInstanceOf(ObjectType.class, allOfList.get(0));
    assertInstanceOf(ObjectType.class, allOfList.get(1));
  }

  @Test
  public void testRefArrayNested() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "ref-array-nested.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    ArrayType departmentsArray = (ArrayType) objectType.properties().get().get("departments");
    RefType deptRef = (RefType) departmentsArray.getItems()[0].getType();
    ObjectType deptType = (ObjectType) deptRef.resolve();
    assertTrue(deptType.properties().isPresent());
    ArrayType employeesArray = (ArrayType) deptType.properties().get().get("employees");
    assertInstanceOf(RefType.class, employeesArray.getItems()[0].getType());
  }

  @Test
  public void testSingleProperty() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "single-property.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    assertEquals(1, objectType.properties().get().size());
    assertTrue(objectType.properties().get().containsKey("value"));
  }

  @Test
  public void testManyProperties() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "many-properties.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    assertEquals(10, objectType.properties().get().size());
  }

  @Test
  public void testNestedRequired() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "nested-required.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType root = (ObjectType) type;
    assertTrue(root.required().isPresent());
    assertTrue(root.required().get().contains("outer"));
    ObjectType outer = (ObjectType) root.properties().get().get("outer");
    assertTrue(outer.required().isPresent());
    assertTrue(outer.required().get().contains("inner"));
    ObjectType inner = (ObjectType) outer.properties().get().get("inner");
    assertTrue(inner.required().isPresent());
    assertTrue(inner.required().get().contains("value"));
  }

  @Test
  public void testObjectWithIdSchema() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "object-with-id-schema.schema.yaml");
    assertTrue(definition.id().isPresent());
    assertEquals("https://example.com/user.schema.json", definition.id().get());
    assertTrue(definition.schema().isPresent());
    assertTrue(definition.title().isPresent());
    assertEquals("User", definition.title().get());
  }

  @Test
  public void testMixedRefsAndInline() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "mixed-refs-and-inline.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertInstanceOf(ObjectType.class, props.get("inlineObj"));
    assertInstanceOf(RefType.class, props.get("refObj"));
    assertInstanceOf(ObjectType.class, props.get("anotherInline"));
  }

  @Test
  public void testAllOfMerge() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "allof-merge.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(AllOfType.class, type);
    AllOfType allOfType = (AllOfType) type;
    List<HasType> allOfList = allOfType.getAllOf();
    assertEquals(2, allOfList.size());
    ObjectType first = (ObjectType) allOfList.get(0);
    assertTrue(first.required().isPresent());
    assertTrue(first.required().get().contains("id"));
    ObjectType second = (ObjectType) allOfList.get(1);
    assertTrue(second.required().isPresent());
    assertTrue(second.required().get().contains("name"));
  }

  @Test
  public void testNestedArraysInObject() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "nested-arrays-in-object.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    ArrayType matrixArray = (ArrayType) objectType.properties().get().get("matrix");
    assertInstanceOf(ArrayType.class, matrixArray.getItems()[0].getType());
    ArrayType innerArray = (ArrayType) matrixArray.getItems()[0].getType();
    assertInstanceOf(IntegerType.class, innerArray.getItems()[0].getType());
  }

  @Test
  public void testObjectAnyOfOneOf() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "object-anyof-oneof.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    ObjectType configType = (ObjectType) objectType.properties().get().get("config");
    assertTrue(configType.anyOf().isPresent());
    assertEquals(2, configType.anyOf().get().size());
    ObjectType settingType = (ObjectType) objectType.properties().get().get("setting");
    assertTrue(settingType.oneOf().isPresent());
    assertEquals(2, settingType.oneOf().get().size());
  }

  @Test
  public void testTripleRefChain() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "triple-ref-chain.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    RefType aRef = (RefType) objectType.properties().get().get("a");
    ObjectType typeA = (ObjectType) aRef.resolve();
    RefType bRef = (RefType) typeA.properties().get().get("b");
    ObjectType typeB = (ObjectType) bRef.resolve();
    RefType cRef = (RefType) typeB.properties().get().get("c");
    ObjectType typeC = (ObjectType) cRef.resolve();
    assertTrue(typeC.properties().isPresent());
    assertInstanceOf(StringType.class, typeC.properties().get().get("value"));
  }

  @Test
  public void testAllPrimitiveTypes() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "all-primitive-types.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertEquals(6, props.size());
    assertInstanceOf(StringType.class, props.get("stringProp"));
    assertInstanceOf(IntegerType.class, props.get("integerProp"));
    assertInstanceOf(NumberType.class, props.get("numberProp"));
    assertInstanceOf(BooleanType.class, props.get("booleanProp"));
    assertInstanceOf(ArrayType.class, props.get("arrayProp"));
    assertInstanceOf(ObjectType.class, props.get("objectProp"));
  }

  @Test
  public void testRefOnly() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "ref-only.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(RefType.class, type);
    RefType refType = (RefType) type;
    assertEquals("#/$defs/MainType", refType.ref());
    ObjectType mainType = (ObjectType) refType.resolve();
    assertTrue(mainType.properties().isPresent());
    assertTrue(mainType.properties().get().containsKey("id"));
    assertTrue(mainType.properties().get().containsKey("data"));
  }

  @Test
  public void testPartialRequired() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "partial-required.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.required().isPresent());
    assertEquals(1, objectType.required().get().size());
    assertTrue(objectType.required().get().contains("id"));
    assertTrue(objectType.properties().isPresent());
    assertEquals(5, objectType.properties().get().size());
  }

  @Test
  public void testCombinedConstraints() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "combined-constraints.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.getTitle().isPresent());
    assertTrue(objectType.getDescription().isPresent());
    assertTrue(objectType.additionalProperties().isPresent());
    assertFalse(objectType.additionalProperties().get().isAllowed());
    assertTrue(objectType.required().isPresent());
    assertEquals(2, objectType.required().get().size());
    assertTrue(objectType.properties().isPresent());
    assertEquals(3, objectType.properties().get().size());
  }

  // ============ Additional Tests ============

  @Test
  public void testWithEnum() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "with-enum.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertEquals(2, props.size());
    assertInstanceOf(StringType.class, props.get("status"));
    assertInstanceOf(IntegerType.class, props.get("priority"));
  }

  @Test
  public void testWithConst() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "with-const.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    assertEquals(3, objectType.properties().get().size());
  }

  @Test
  public void testWithDefault() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "with-default.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertEquals(4, props.size());
    assertInstanceOf(StringType.class, props.get("name"));
    assertInstanceOf(IntegerType.class, props.get("count"));
    assertInstanceOf(BooleanType.class, props.get("enabled"));
    assertInstanceOf(ArrayType.class, props.get("tags"));
  }

  @Test
  public void testMinMaxProperties() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "min-max-properties.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    assertEquals(3, objectType.properties().get().size());
  }

  @Test
  public void testPatternProperties() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "pattern-properties.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.additionalProperties().isPresent());
    assertFalse(objectType.additionalProperties().get().isAllowed());
  }

  @Test
  public void testPropertyNames() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "property-names.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.additionalProperties().isPresent());
    assertTrue(objectType.additionalProperties().get().isAllowed());
    assertTrue(objectType.additionalProperties().get().getType().isPresent());
    assertInstanceOf(StringType.class, objectType.additionalProperties().get().getType().get());
  }

  @Test
  public void testDependentRequired() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "dependent-required.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    assertEquals(3, objectType.properties().get().size());
  }

  @Test
  public void testDependentSchemas() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "dependent-schemas.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    assertEquals(3, objectType.properties().get().size());
  }

  @Test
  public void testIfThenElse() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "if-then-else.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    assertEquals(2, objectType.properties().get().size());
    assertInstanceOf(StringType.class, objectType.properties().get().get("country"));
    assertInstanceOf(StringType.class, objectType.properties().get().get("postalCode"));
  }

  @Test
  public void testWithNot() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "with-not.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    assertEquals(2, objectType.properties().get().size());
  }

  @Test
  public void testWithFormat() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "with-format.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertEquals(5, props.size());
    assertInstanceOf(StringType.class, props.get("email"));
    assertInstanceOf(StringType.class, props.get("website"));
    assertInstanceOf(StringType.class, props.get("birthDate"));
    assertInstanceOf(StringType.class, props.get("createdAt"));
    assertInstanceOf(StringType.class, props.get("uuid"));
  }

  @Test
  public void testReadWriteOnly() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "read-write-only.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertEquals(3, props.size());
    assertInstanceOf(IntegerType.class, props.get("id"));
    assertInstanceOf(StringType.class, props.get("password"));
    assertInstanceOf(StringType.class, props.get("name"));
  }

  @Test
  public void testImplicitObject() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "implicit-object.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    assertTrue(objectType.required().isPresent());
    assertTrue(objectType.required().get().contains("name"));
    assertEquals(2, objectType.properties().get().size());
  }

  @Test
  public void testRootAnyOf() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "root-anyof.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.anyOf().isPresent());
    assertEquals(2, objectType.anyOf().get().size());
    assertInstanceOf(ObjectType.class, objectType.anyOf().get().get(0));
    assertInstanceOf(ObjectType.class, objectType.anyOf().get().get(1));
  }

  @Test
  public void testRootOneOf() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "root-oneof.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.oneOf().isPresent());
    assertEquals(2, objectType.oneOf().get().size());
    ObjectType cardType = (ObjectType) objectType.oneOf().get().get(0);
    assertTrue(cardType.properties().isPresent());
    assertTrue(cardType.required().isPresent());
  }

  @Test
  public void testRefWithInline() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "ref-with-inline.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    assertInstanceOf(RefType.class, objectType.properties().get().get("base"));
    assertInstanceOf(StringType.class, objectType.properties().get().get("extra"));
  }

  @Test
  public void testCircularRefs() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "circular-refs.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    RefType personRef = (RefType) objectType.properties().get().get("person");
    ObjectType personType = (ObjectType) personRef.resolve();
    assertTrue(personType.properties().isPresent());
    assertInstanceOf(RefType.class, personType.properties().get().get("friend"));
    assertInstanceOf(RefType.class, personType.properties().get().get("employer"));
    RefType employerRef = (RefType) personType.properties().get().get("employer");
    ObjectType companyType = (ObjectType) employerRef.resolve();
    assertTrue(companyType.properties().isPresent());
    ArrayType employeesArray = (ArrayType) companyType.properties().get().get("employees");
    assertInstanceOf(RefType.class, employeesArray.getItems()[0].getType());
  }

  @Test
  public void testEmptySchema() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "empty-schema.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertFalse(objectType.properties().isPresent());
    assertFalse(objectType.required().isPresent());
  }

  @Test
  public void testOnlyAdditionalProps() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "only-additional-props.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertFalse(objectType.properties().isPresent());
    assertTrue(objectType.additionalProperties().isPresent());
    assertTrue(objectType.additionalProperties().get().isAllowed());
    assertTrue(objectType.additionalProperties().get().getType().isPresent());
    assertInstanceOf(IntegerType.class, objectType.additionalProperties().get().getType().get());
  }

  @Test
  public void testApiResponse() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "api-response.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.required().isPresent());
    assertTrue(objectType.required().get().contains("success"));
    assertTrue(objectType.required().get().contains("data"));
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertEquals(4, props.size());
    assertInstanceOf(BooleanType.class, props.get("success"));
    assertInstanceOf(ObjectType.class, props.get("data"));
    assertInstanceOf(ObjectType.class, props.get("meta"));
    assertInstanceOf(ArrayType.class, props.get("errors"));
    Map<String, ObjectType> defs = definition.definitions();
    assertEquals(2, defs.size());
    assertTrue(defs.containsKey("Item"));
    assertTrue(defs.containsKey("Error"));
  }

  @Test
  public void testDiscriminator() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "discriminator.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.required().isPresent());
    assertTrue(objectType.required().get().contains("type"));
    assertTrue(objectType.oneOf().isPresent());
    assertEquals(3, objectType.oneOf().get().size());
  }

  @Test
  public void testErrorResponse() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "error-response.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.required().isPresent());
    assertTrue(objectType.required().get().contains("error"));
    assertTrue(objectType.properties().isPresent());
    ObjectType errorType = (ObjectType) objectType.properties().get().get("error");
    assertTrue(errorType.required().isPresent());
    assertTrue(errorType.required().get().contains("code"));
    assertTrue(errorType.required().get().contains("message"));
    assertTrue(errorType.properties().isPresent());
    assertEquals(4, errorType.properties().get().size());
  }

  @Test
  public void testConfigSchema() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "config-schema.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.required().isPresent());
    assertTrue(objectType.required().get().contains("version"));
    assertTrue(objectType.required().get().contains("database"));
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertEquals(4, props.size());
    ObjectType dbType = (ObjectType) props.get("database");
    assertTrue(dbType.required().isPresent());
    assertTrue(dbType.required().get().contains("host"));
    assertTrue(dbType.required().get().contains("port"));
  }

  @Test
  public void testAllOfAnyOfCombined() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "allof-anyof-combined.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(AllOfType.class, type);
    AllOfType allOfType = (AllOfType) type;
    List<HasType> allOfList = allOfType.getAllOf();
    assertEquals(2, allOfList.size());
    assertInstanceOf(ObjectType.class, allOfList.get(0));
    assertInstanceOf(ObjectType.class, allOfList.get(1));
    ObjectType secondPart = (ObjectType) allOfList.get(1);
    assertTrue(secondPart.anyOf().isPresent());
    assertEquals(2, secondPart.anyOf().get().size());
  }

  @Test
  public void testNestedAllOfOneOf() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "nested-allof-oneof.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    AllOfType dataAllOf = (AllOfType) objectType.properties().get().get("data");
    List<HasType> allOfList = dataAllOf.getAllOf();
    assertEquals(2, allOfList.size());
    assertInstanceOf(ObjectType.class, allOfList.get(0));
    ObjectType secondPart = (ObjectType) allOfList.get(1);
    assertTrue(secondPart.oneOf().isPresent());
    assertEquals(2, secondPart.oneOf().get().size());
  }

  @Test
  public void testStringConstraints() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "string-constraints.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertEquals(3, props.size());
    assertInstanceOf(StringType.class, props.get("username"));
    assertInstanceOf(StringType.class, props.get("bio"));
    assertInstanceOf(StringType.class, props.get("slug"));
  }

  @Test
  public void testNumericConstraints() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "numeric-constraints.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertEquals(5, props.size());
    assertInstanceOf(IntegerType.class, props.get("age"));
    assertInstanceOf(NumberType.class, props.get("price"));
    assertInstanceOf(NumberType.class, props.get("discount"));
    assertInstanceOf(IntegerType.class, props.get("quantity"));
    assertInstanceOf(NumberType.class, props.get("rating"));
  }

  @Test
  public void testDeprecatedProps() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "deprecated-props.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    assertEquals(4, objectType.properties().get().size());
  }

  @Test
  public void testWithExamples() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "with-examples.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertEquals(3, props.size());
    assertInstanceOf(StringType.class, props.get("name"));
    assertInstanceOf(StringType.class, props.get("email"));
    assertInstanceOf(IntegerType.class, props.get("age"));
  }

  @Test
  public void testNullableProps() {
    SchemaDefinition definition = new Parser().parse(BASE_PATH + "nullable-props.schema.yaml");
    HasType type = definition.model();
    assertInstanceOf(ObjectType.class, type);
    ObjectType objectType = (ObjectType) type;
    assertTrue(objectType.properties().isPresent());
    Map<String, HasType> props = objectType.properties().get();
    assertEquals(4, props.size());
    assertInstanceOf(StringType.class, props.get("name"));
  }
}