# YAML Schema Parser

A Java library that provides a convenient API for parsing and analyzing YAML schemas (JSON Schema in YAML format).

## Overview

This parser converts YAML schema definitions into a strongly-typed Java object model, making it easy to programmatically inspect schema structure, types, constraints, and relationships.

## Features

- **Full JSON Schema Support**: Parses all JSON Schema types (object, array, string, integer, number, boolean, null)
- **Reference Resolution**: Automatic resolution of `$ref` references including `$defs` definitions
- **Schema Combinators**: Support for `allOf`, `anyOf`, `oneOf` combinators
- **Type-Safe API**: Strongly-typed model classes for each schema type
- **Nested Structures**: Deep navigation through nested objects and arrays
- **Constraint Access**: Easy access to validation constraints (required, minItems, maxItems, pattern, etc.)
- **Metadata Extraction**: Access to title, description, format, default values, and enums

## Requirements

- Java 17 or higher
- Maven

## Dependencies

```xml
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-yaml</artifactId>
    <version>2.20.0</version>
</dependency>
<dependency>
    <groupId>com.networknt</groupId>
    <artifactId>json-schema-validator</artifactId>
    <version>1.5.9</version>
</dependency>
```

## Installation

Add this dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.treblereel.yaml.schema</groupId>
    <artifactId>parser</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

### Basic Usage

```java
import org.treblereel.yaml.schema.Parser;
import org.treblereel.yaml.schema.model.SchemaDefinition;
import org.treblereel.yaml.schema.model.ObjectType;

// Parse schema from file
Parser parser = new Parser();
SchemaDefinition schema = parser.parse("path/to/schema.yaml");

// Access schema metadata
schema.id();     // Optional<String>
schema.schema(); // Optional<String>
schema.title();  // Optional<String>

// Get the root type
ObjectType root = (ObjectType) schema.model();
```

### Parsing from Different Sources

```java
// From file path (String)
SchemaDefinition schema1 = parser.parse("schema.yaml");

// From File object
SchemaDefinition schema2 = parser.parse(new File("schema.yaml"));

// From URI
SchemaDefinition schema3 = parser.parse(URI.create("file:///path/to/schema.yaml"));
```

## API Examples

### Working with Objects

```java
SchemaDefinition definition = parser.parse("user-schema.yaml");
ObjectType userType = (ObjectType) definition.model();

// Get properties
if (userType.properties().isPresent()) {
    Map<String, HasType> props = userType.properties().get();
    
    // Check property types
    StringType firstName = (StringType) props.get("firstName");
    IntegerType age = (IntegerType) props.get("age");
    BooleanType active = (BooleanType) props.get("active");
}

// Check required fields
if (userType.required().isPresent()) {
    List<String> required = userType.required().get();
    boolean isNameRequired = required.contains("name");
}

// Access metadata
userType.getTitle();        // Optional<String>
userType.getDescription();  // Optional<String>

// Additional properties
if (userType.additionalProperties().isPresent()) {
    AdditionalProperties addProps = userType.additionalProperties().get();
    boolean allowed = addProps.isAllowed();
    Optional<HasType> type = addProps.getType();
}
```

### Working with Arrays

```java
SchemaDefinition definition = parser.parse("tags-schema.yaml");
ArrayType tagsArray = (ArrayType) definition.model();

// Get item type
ArrayItemType[] items = tagsArray.getItems();
StringType itemType = (StringType) items[0].getType();

// Array constraints
tagsArray.minItems();    // Optional<Integer>
tagsArray.maxItems();    // Optional<Integer>
tagsArray.uniqueItems(); // Optional<Boolean>

// Tuple validation (multiple item types)
ArrayType tupleArray = (ArrayType) parser.parse("tuple.yaml").model();
ArrayItemType[] tupleItems = tupleArray.getItems();
// tupleItems[0] -> StringType
// tupleItems[1] -> IntegerType
// tupleItems[2] -> BooleanType
```

### Prefix Items (Modern Tuple Validation)

Prefix items provide a modern (JSON Schema Draft 2020-12+) way to define tuple validation, offering a more explicit alternative to array-based items.

```java
SchemaDefinition definition = parser.parse("tuple-schema.yaml");
ArrayType tupleArray = (ArrayType) definition.model();

// Get prefix items
if (tupleArray.prefixItems().isPresent()) {
    ArrayItemType[] prefixItems = tupleArray.prefixItems().get();
    StringType first = (StringType) prefixItems[0].getType();
    NumberType second = (NumberType) prefixItems[1].getType();
    BooleanType third = (BooleanType) prefixItems[2].getType();
}
```

Example schema:
```yaml
type: array
prefixItems:
  - type: string
    minLength: 1
  - type: number
    minimum: 0
  - type: boolean
```

Prefix items can be combined with items for additional elements:
```yaml
type: array
prefixItems:
  - type: string
  - type: number
items:
  type: string  # Additional items must be strings
```

### Nested Objects

```java
SchemaDefinition definition = parser.parse("nested-schema.yaml");
ObjectType root = (ObjectType) definition.model();

// Navigate nested structure
ObjectType userType = (ObjectType) root.properties().get().get("user");
StringType userName = (StringType) userType.properties().get().get("name");
IntegerType userAge = (IntegerType) userType.properties().get().get("age");
```

### References ($ref)

```java
SchemaDefinition definition = parser.parse("schema-with-refs.yaml");
ObjectType root = (ObjectType) definition.model();

// Get reference
RefType addressRef = (RefType) root.properties().get().get("address");
String refPath = addressRef.ref(); // "#/$defs/Address"

// Resolve reference
ObjectType addressType = (ObjectType) addressRef.resolve();
Map<String, HasType> addressProps = addressType.properties().get();

// Access definitions
Map<String, ObjectType> defs = definition.definitions();
ObjectType addressDef = defs.get("Address");
```

### String Types with Constraints

```java
StringType emailType = (StringType) objectType.properties().get().get("email");

// Pattern validation
emailType.pattern();     // Optional<String>

// Format
emailType.format();      // Optional<String> - "email", "uri", "date-time", etc.

// Length constraints
emailType.minLength();   // Optional<Integer>
emailType.maxLength();   // Optional<Integer>

// Enum values
emailType.enumValues();  // Optional<List<String>>

// Const value
emailType.constValue();  // Optional<String>

// Default value
emailType.defaultValue(); // Optional<String>

// Metadata
emailType.title();       // Optional<String>
emailType.description(); // Optional<String>
```

### Number and Integer Types

```java
IntegerType portType = (IntegerType) objectType.properties().get().get("port");

// Range constraints
portType.minimum();      // Optional<Integer>
portType.maximum();      // Optional<Integer>

NumberType priceType = (NumberType) objectType.properties().get().get("price");
priceType.minimum();     // Optional<Double>
priceType.maximum();     // Optional<Double>
```

### Pattern Properties

Pattern properties allow defining schemas for object properties whose names match regex patterns.

```java
ObjectType objectType = (ObjectType) definition.model();

// Get pattern properties
if (objectType.patternProperties().isPresent()) {
    Map<String, HasType> patterns = objectType.patternProperties().get();
    
    // Check if schema defines pattern for properties starting with "s_"
    if (patterns.containsKey("^s_")) {
        HasType stringPattern = patterns.get("^s_");
        // Properties matching ^s_ must be strings
    }
    
    // Iterate over all patterns
    for (Map.Entry<String, HasType> entry : patterns.entrySet()) {
        String pattern = entry.getKey();      // Regex pattern
        HasType schema = entry.getValue();    // Schema for matching properties
    }
}
```

Example schema:
```yaml
type: object
properties:
  name: { type: string }
patternProperties:
  "^s_":
    type: string
  "^i_":
    type: integer
  "^config_":
    type: object
    properties:
      enabled: { type: boolean }
```

### Property Names Validation

Property names validation ensures all property names in an object validate against a schema.

```java
ObjectType objectType = (ObjectType) definition.model();

// Check if property name validation is defined
if (objectType.propertyNames().isPresent()) {
    HasType nameSchema = objectType.propertyNames().get();
    
    // Example: names must be valid identifiers
    if (nameSchema instanceof ObjectType) {
        ObjectType nameRules = (ObjectType) nameSchema;
        // Check pattern, length constraints, etc.
    }
}
```

Example schema:
```yaml
type: object
propertyNames:
  pattern: "^[a-zA-Z_][a-zA-Z0-9_]*$"  # Valid identifiers only
properties:
  user_id: { type: integer }
  user_name: { type: string }
```

With length constraints:
```yaml
type: object
propertyNames:
  type: string
  minLength: 3
  maxLength: 20
```

### Dependent Required

Dependent required specifies conditional property requirements: if property X is present, then properties Y and Z must also be present.

```java
ObjectType objectType = (ObjectType) definition.model();

// Check if dependent requirements are defined
if (objectType.dependentRequired().isPresent()) {
    Map<String, List<String>> deps = objectType.dependentRequired().get();
    
    // Check what's required when creditCard is present
    if (deps.containsKey("creditCard")) {
        List<String> required = deps.get("creditCard");
        // If creditCard is present, these properties must also be present
        // e.g., [billingAddress, cvv]
    }
    
    // Iterate over all dependencies
    for (Map.Entry<String, List<String>> entry : deps.entrySet()) {
        String triggerProperty = entry.getKey();
        List<String> requiredProperties = entry.getValue();
        // If triggerProperty is present, requiredProperties must be present
    }
}
```

Example schema:
```yaml
type: object
properties:
  name: { type: string }
  creditCard: { type: string }
  billingAddress: { type: string }
  cvv: { type: string }
dependentRequired:
  creditCard: [billingAddress, cvv]
```

Multiple dependencies:
```yaml
type: object
dependentRequired:
  email: [password]
  password: [confirmPassword]
  securityQuestion: [securityAnswer]
```

### Dependent Schemas

Dependent schemas apply additional schema validation when a property is present. More flexible than dependentRequired as it allows defining complete schemas, not just required properties.

```java
ObjectType objectType = (ObjectType) definition.model();

// Check if dependent schemas are defined
if (objectType.dependentSchemas().isPresent()) {
    Map<String, HasType> deps = objectType.dependentSchemas().get();
    
    // Get schema that applies when creditCard is present
    if (deps.containsKey("creditCard")) {
        HasType schema = deps.get("creditCard");
        
        // Usually an ObjectType with additional properties and constraints
        if (schema instanceof ObjectType) {
            ObjectType additionalSchema = (ObjectType) schema;
            // Check what properties are required
            // Check what validations apply
        }
    }
}
```

Example schema:
```yaml
type: object
properties:
  name: { type: string }
  creditCard: { type: string }
dependentSchemas:
  creditCard:
    properties:
      billingAddress:
        type: string
      cvv:
        type: string
        pattern: "^[0-9]{3,4}$"
    required: [billingAddress, cvv]
```

Multiple dependent schemas:
```yaml
type: object
dependentSchemas:
  email:
    properties:
      emailVerified: { type: boolean }
    required: [emailVerified]
  isPremium:
    properties:
      subscriptionId: { type: string }
      expiryDate: { type: string, format: date }
    required: [subscriptionId, expiryDate]
```

### Schema Combinators

#### allOf

```java
AllOfType allOfType = (AllOfType) definition.model();
List<HasType> allOfList = allOfType.getAllOf();

for (HasType type : allOfList) {
    // Each type must be satisfied
    ObjectType obj = (ObjectType) type;
    // Process merged properties
}
```

#### anyOf

```java
ObjectType objectType = (ObjectType) definition.model();
if (objectType.anyOf().isPresent()) {
    List<HasType> anyOfList = objectType.anyOf().get();
    // At least one must be satisfied
}
```

#### oneOf

```java
ObjectType objectType = (ObjectType) definition.model();
if (objectType.oneOf().isPresent()) {
    List<HasType> oneOfList = objectType.oneOf().get();
    // Exactly one must be satisfied
}
```

### Conditional Schemas (if/then/else)

Conditional schemas allow applying different validation rules based on the instance data.

```java
ObjectType objectType = (ObjectType) definition.model();

// Check if conditional validation is present
if (objectType.ifCondition().isPresent()) {
    HasType ifCondition = objectType.ifCondition().get();
    
    // Applied when if condition validates
    if (objectType.thenSchema().isPresent()) {
        HasType thenSchema = objectType.thenSchema().get();
        // Process then branch
    }
    
    // Applied when if condition does NOT validate
    if (objectType.elseSchema().isPresent()) {
        HasType elseSchema = objectType.elseSchema().get();
        // Process else branch
    }
}
```

Example schema:
```yaml
type: object
properties:
  country: { type: string }
  postalCode: { type: string }
if:
  properties:
    country: { const: "USA" }
then:
  properties:
    postalCode:
      type: string
      pattern: "^[0-9]{5}$"
else:
  properties:
    postalCode:
      type: string
      pattern: "^[A-Z][0-9][A-Z] [0-9][A-Z][0-9]$"
```

### Complex Example: Workflow Schema

```java
SchemaDefinition definition = parser.parse("workflow.yaml");
ObjectType root = (ObjectType) definition.model();

// Check required fields
List<String> required = root.required().get();
// required: ["document", "do"]

// Access document metadata
ObjectType document = (ObjectType) root.properties().get().get("document");
StringType dsl = (StringType) document.properties().get().get("dsl");
String pattern = dsl.pattern().get(); // Semver pattern

// Navigate to tasks
RefType doRef = (RefType) root.properties().get().get("do");
ArrayType taskList = (ArrayType) doRef.resolve();

// Task items with min/max properties constraint
ArrayItemType taskItem = taskList.getItems()[0];
ObjectType taskObject = (ObjectType) taskItem.getType();
Integer minProps = taskObject.minProperties().get(); // 1
Integer maxProps = taskObject.maxProperties().get(); // 1

// Get task definition through additionalProperties
RefType taskRef = (RefType) taskObject.additionalProperties().get().getType().get();
ObjectType task = (ObjectType) taskRef.resolve();

// Task has oneOf with multiple task types
List<HasType> taskTypes = task.oneOf().get();
// callTask, doTask, forkTask, emitTask, forTask, listenTask, etc.
```

## Object Model

### Type Hierarchy

```
HasType (interface)
├── ObjectType
├── ArrayType
├── StringType
├── IntegerType
├── NumberType
├── BooleanType
├── NullType
├── RefType
└── AllOfType
```

### SchemaDefinition

Main entry point for parsed schema.

**Methods:**
- `id()` - Schema $id (Optional<String>)
- `schema()` - Schema $schema version (Optional<String>)
- `title()` - Schema title (Optional<String>)
- `model()` - Root type (HasType)
- `definitions()` - $defs map (Map<String, ObjectType>)

### ObjectType

Represents object schemas.

**Methods:**
- `properties()` - Property map (Optional<Map<String, HasType>>)
- `patternProperties()` - Pattern-based property map (Optional<Map<String, HasType>>)
- `propertyNames()` - Property name validation schema (Optional<HasType>)
- `required()` - Required fields (Optional<List<String>>)
- `dependentRequired()` - Conditional required fields (Optional<Map<String, List<String>>>)
- `dependentSchemas()` - Conditional schema application (Optional<Map<String, HasType>>)
- `additionalProperties()` - Additional properties config (Optional<AdditionalProperties>)
- `minProperties()` - Minimum properties (Optional<Integer>)
- `maxProperties()` - Maximum properties (Optional<Integer>)
- `anyOf()` - anyOf alternatives (Optional<List<HasType>>)
- `oneOf()` - oneOf alternatives (Optional<List<HasType>>)
- `allOf()` - allOf items (Optional<List<HasType>>)
- `not()` - not schema (Optional<HasType>)
- `ifCondition()` - if condition schema (Optional<HasType>)
- `thenSchema()` - then schema (Optional<HasType>)
- `elseSchema()` - else schema (Optional<HasType>)
- `getTitle()`, `getDescription()` - Metadata

### ArrayType

Represents array schemas.

**Methods:**
- `getItems()` - Item types (ArrayItemType[])
- `prefixItems()` - Prefix items for tuple validation (Optional<ArrayItemType[]>)
- `minItems()` - Minimum items (Optional<Integer>)
- `maxItems()` - Maximum items (Optional<Integer>)
- `uniqueItems()` - Unique constraint (Optional<Boolean>)
- `additionalItems()` - Additional items allowed (Optional<Boolean>)
- `not()` - not combinator (Optional<HasType>)

### StringType

Represents string schemas.

**Methods:**
- `pattern()` - Regex pattern (Optional<String>)
- `format()` - Format constraint (Optional<String>)
- `minLength()` - Minimum length (Optional<Integer>)
- `maxLength()` - Maximum length (Optional<Integer>)
- `enumValues()` - Allowed values (Optional<List<String>>)
- `constValue()` - Constant value (Optional<String>)
- `not()` - not combinator (Optional<HasType>)

### IntegerType

Represents integer schemas.

**Methods:**
- `minimum()` - Minimum value (Optional<Integer>)
- `maximum()` - Maximum value (Optional<Integer>)
- `exclusiveMinimum()` - Exclusive minimum (Optional<Integer>)
- `exclusiveMaximum()` - Exclusive maximum (Optional<Integer>)
- `enumValues()` - Allowed values (Optional<List<Integer>>)
- `constValue()` - Constant value (Optional<Integer>)
- `not()` - not combinator (Optional<HasType>)

### NumberType

Represents number (floating-point) schemas.

**Methods:**
- `minimum()` - Minimum value (Optional<Double>)
- `maximum()` - Maximum value (Optional<Double>)
- `exclusiveMinimum()` - Exclusive minimum (Optional<Double>)
- `exclusiveMaximum()` - Exclusive maximum (Optional<Double>)
- `multipleOf()` - Multiple constraint (Optional<Double>)
- `enumValues()` - Allowed values (Optional<List<Double>>)
- `constValue()` - Constant value (Optional<Double>)
- `not()` - not combinator (Optional<HasType>)

### BooleanType

Represents boolean schemas.

**Methods:**
- `constValue()` - Constant value (Optional<Boolean>)
- `defaultValue()` - Default value (Optional<Boolean>)
- `not()` - not combinator (Optional<HasType>)

### RefType

Represents $ref references.

**Methods:**
- `ref()` - Reference path (String)
- `resolve()` - Resolve to target type (HasType)

## Test Examples

The library includes comprehensive tests demonstrating all features:

- **ObjectParserTest**: 80+ tests covering objects, properties, required fields, refs, allOf/anyOf/oneOf
- **ArrayParserTest**: 20+ tests covering arrays, tuples, nested arrays, constraints
- **WorkflowTest**: Real-world complex schema validation (Serverless Workflow DSL)

## Building from Source

```bash
mvn clean install
```

## Running Tests

```bash
mvn test
```

## License

[Your License Here]

## Contributing

Contributions are welcome! Please submit issues and pull requests.

## Author

Dmitrii Tikhomirov (@treblereel)
