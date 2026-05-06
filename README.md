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
    Map<String, HasType> props = userType.properties();
    
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

### Array Contains Validation

The `contains` keyword specifies that an array must contain at least one element matching a schema. Can be combined with `minContains` and `maxContains` to control the number of matching elements.

```java
SchemaDefinition definition = parser.parse("array-schema.yaml");
ArrayType arrayType = (ArrayType) definition.model();

// Check if contains constraint is present
if (arrayType.contains().isPresent()) {
    HasType containsSchema = arrayType.contains().get();
    // Array must contain at least one element matching this schema
    
    // Check min/max constraints
    if (arrayType.minContains().isPresent()) {
        int min = arrayType.minContains().get(); // Minimum matching elements
    }
    if (arrayType.maxContains().isPresent()) {
        int max = arrayType.maxContains().get(); // Maximum matching elements
    }
}
```

Example schema - at least one number >= 5:
```yaml
type: array
contains:
  type: number
  minimum: 5
```

Example with constraints - 2 to 5 strings starting with 'test':
```yaml
type: array
contains:
  type: string
  pattern: "^test"
minContains: 2
maxContains: 5
```

### Unevaluated Items Control

The `unevaluatedItems` keyword controls whether array elements not covered by `items` or `prefixItems` are allowed (JSON Schema Draft 2019-09+).

```java
SchemaDefinition definition = parser.parse("array-schema.yaml");
ArrayType arrayType = (ArrayType) definition.model();

// Check unevaluatedItems constraint
if (arrayType.unevaluatedItems().isPresent()) {
    boolean allowAdditional = arrayType.unevaluatedItems().get();
    // false = reject additional items beyond prefixItems
    // true = allow additional items
}
```

Example schema - exactly 2 elements (string, number):
```yaml
type: array
prefixItems:
  - type: string
  - type: number
unevaluatedItems: false  # Only 2 elements allowed
```

Example schema - first 2 elements typed, rest must be boolean:
```yaml
type: array
prefixItems:
  - type: string
  - type: integer
items:
  type: boolean
unevaluatedItems: false
# First element: string, second: integer, remaining: boolean
```

### Example Values (Metadata)

The `examples` keyword provides example values for documentation purposes (JSON Schema Draft 6+). These are metadata only and do not affect validation.

```java
SchemaDefinition definition = parser.parse("schema.yaml");
StringType stringType = (StringType) definition.model();

// Access example values
if (stringType.examples().isPresent()) {
    List<JsonNode> examples = stringType.examples().get();
    for (JsonNode example : examples) {
        String exampleValue = example.asText();
        // Use for documentation, code generation, etc.
    }
}
```

Example schema with examples:
```yaml
type: string
pattern: "^[a-z]+$"
examples:
  - "hello"
  - "world"
  - "test"
```

Works with all types - strings, integers, objects, arrays:
```yaml
type: object
properties:
  name:
    type: string
  age:
    type: integer
examples:
  - name: "Alice"
    age: 30
  - name: "Bob"
    age: 25
```

### Deprecated Metadata

The `deprecated` keyword indicates that a schema is deprecated and should not be used (JSON Schema Draft 2019-09+). This is metadata only and does not affect validation.

```java
SchemaDefinition definition = parser.parse("schema.yaml");
StringType stringType = (StringType) definition.model();

// Check if schema is deprecated
if (stringType.deprecated().isPresent()) {
    boolean isDeprecated = stringType.deprecated().get();
    if (isDeprecated) {
        // Warn users, generate migration guides, etc.
    }
}
```

Example schema marked as deprecated:
```yaml
type: string
deprecated: true
description: This field is deprecated and will be removed in v2.0
```

Works with all types - strings, integers, numbers, objects, arrays, booleans:
```yaml
type: object
deprecated: true
properties:
  oldField:
    type: string
  newField:
    type: string
description: Use the new API instead
```

### ReadOnly / WriteOnly Metadata

The `readOnly` and `writeOnly` keywords indicate whether a property should appear in requests or responses (JSON Schema Draft 7+, OpenAPI). These are metadata only and do not affect validation.

**readOnly**: Property should only appear in responses (server-generated fields like IDs, timestamps)

**writeOnly**: Property should only appear in requests (sensitive fields like passwords)

```java
SchemaDefinition definition = parser.parse("schema.yaml");
ObjectType objectType = (ObjectType) definition.model();

// Check readOnly property
StringType idField = (StringType) objectType.properties().get("id");
if (idField.readOnly().isPresent() && idField.readOnly().get()) {
    // This field is server-generated, should not be in requests
}

// Check writeOnly property  
StringType passwordField = (StringType) objectType.properties().get("password");
if (passwordField.writeOnly().isPresent() && passwordField.writeOnly().get()) {
    // This field is sensitive, should not be in responses
}
```

Example schema with readOnly fields:
```yaml
type: object
properties:
  id:
    type: string
    readOnly: true
  createdAt:
    type: string
    format: date-time
    readOnly: true
  name:
    type: string
```

Example schema with writeOnly field:
```yaml
type: object
properties:
  username:
    type: string
  password:
    type: string
    writeOnly: true
```

### Nested Objects

```java
SchemaDefinition definition = parser.parse("nested-schema.yaml");
ObjectType root = (ObjectType) definition.model();

// Navigate nested structure
ObjectType userType = (ObjectType) root.properties().get("user");
StringType userName = (StringType) userType.properties().get("name");
IntegerType userAge = (IntegerType) userType.properties().get("age");
```

### References ($ref)

```java
SchemaDefinition definition = parser.parse("schema-with-refs.yaml");
ObjectType root = (ObjectType) definition.model();

// Get reference
RefType addressRef = (RefType) root.properties().get("address");
String refPath = addressRef.ref(); // "#/$defs/Address"

// Resolve reference
ObjectType addressType = (ObjectType) addressRef.resolve();
Map<String, HasType> addressProps = addressType.properties();

// Access definitions
Map<String, ObjectType> defs = definition.definitions();
ObjectType addressDef = defs.get("Address");
```

### String Types with Constraints

```java
StringType emailType = (StringType) objectType.properties().get("email");

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
emailType.deprecated();  // Optional<Boolean>
emailType.readOnly();    // Optional<Boolean>
emailType.writeOnly();   // Optional<Boolean>
emailType.examples();    // Optional<List<JsonNode>>
```

### Number and Integer Types

```java
IntegerType portType = (IntegerType) objectType.properties().get("port");

// Range constraints
portType.minimum();      // Optional<Integer>
portType.maximum();      // Optional<Integer>

// Metadata
portType.deprecated();   // Optional<Boolean>
portType.readOnly();     // Optional<Boolean>
portType.writeOnly();    // Optional<Boolean>
portType.examples();     // Optional<List<JsonNode>>

NumberType priceType = (NumberType) objectType.properties().get("price");
priceType.minimum();     // Optional<Double>
priceType.maximum();     // Optional<Double>

// Metadata
priceType.deprecated();  // Optional<Boolean>
priceType.readOnly();    // Optional<Boolean>
priceType.writeOnly();   // Optional<Boolean>
priceType.examples();    // Optional<List<JsonNode>>
```

### Exclusive Bounds (Draft 4 vs Draft 6+ Syntax)

yamlSchemaParser supports both JSON Schema Draft 4 boolean syntax and Draft 6+ numeric syntax for exclusive bounds.

#### Draft 6+ Numeric Syntax (Recommended)

In Draft 6 and later, `exclusiveMinimum` and `exclusiveMaximum` are numeric values that replace `minimum`/`maximum`:

```java
IntegerType ageType = (IntegerType) schema.model();

// Draft 6+ numeric syntax
ageType.exclusiveMinimum();  // Optional<Integer> - value must be > this
ageType.exclusiveMaximum();  // Optional<Integer> - value must be < this
```

Example schema (Draft 6+):
```yaml
type: integer
exclusiveMinimum: 0    # value > 0 (not >= 0)
exclusiveMaximum: 100  # value < 100 (not <= 100)
```

#### Draft 4 Boolean Syntax (Legacy Support)

In Draft 4, `exclusiveMinimum` and `exclusiveMaximum` were boolean flags that modified `minimum`/`maximum`:

```java
IntegerType portType = (IntegerType) schema.model();

// Draft 4 boolean syntax
portType.minimum();               // Optional<Integer> - the boundary value
portType.isMinimumExclusive();    // Optional<Boolean> - true = exclusive, false = inclusive
portType.maximum();               // Optional<Integer> - the boundary value
portType.isMaximumExclusive();    // Optional<Boolean> - true = exclusive, false = inclusive
```

Example schema (Draft 4):
```yaml
type: integer
minimum: 0
exclusiveMinimum: true   # value > 0 (not >= 0)
maximum: 100
exclusiveMaximum: false  # value <= 100 (inclusive)
```

#### Determining Which Syntax Is Used

The library automatically detects which syntax is used based on the JsonNode type:
- **Boolean value** → Draft 4 syntax, use `isMinimumExclusive()` / `isMaximumExclusive()`
- **Numeric value** → Draft 6+ syntax, use `exclusiveMinimum()` / `exclusiveMaximum()`

```java
IntegerType valueType = (IntegerType) schema.model();

// Check for Draft 6+ numeric syntax
if (valueType.exclusiveMinimum().isPresent()) {
    Integer exclusiveMin = valueType.exclusiveMinimum().get();
    // Value must be > exclusiveMin
}

// Check for Draft 4 boolean syntax
if (valueType.isMinimumExclusive().isPresent()) {
    Integer min = valueType.minimum().get();
    boolean exclusive = valueType.isMinimumExclusive().get();
    
    if (exclusive) {
        // Value must be > min
    } else {
        // Value must be >= min
    }
}
```

Both syntaxes work with `IntegerType` and `NumberType`:

```java
NumberType temperatureType = (NumberType) schema.model();

// Draft 6+ numeric syntax
temperatureType.exclusiveMinimum();  // Optional<Double>
temperatureType.exclusiveMaximum();  // Optional<Double>

// Draft 4 boolean syntax
temperatureType.minimum();           // Optional<Double>
temperatureType.isMinimumExclusive(); // Optional<Boolean>
temperatureType.maximum();           // Optional<Double>
temperatureType.isMaximumExclusive(); // Optional<Boolean>
```

### Pattern Properties

Pattern properties allow defining schemas for object properties whose names match regex patterns.

```java
ObjectType objectType = (ObjectType) definition.model();

// Get pattern properties
if (objectType.patternProperties().isPresent()) {
    Map<String, HasType> patterns = objectType.patternProperties();
    
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
    Map<String, List<String>> deps = objectType.dependentRequired();
    
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
    Map<String, HasType> deps = objectType.dependentSchemas();
    
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
ObjectType document = (ObjectType) root.properties().get("document");
StringType dsl = (StringType) document.properties().get("dsl");
String pattern = dsl.pattern().get(); // Semver pattern

// Navigate to tasks
RefType doRef = (RefType) root.properties().get("do");
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
- `contains()` - Schema for array contains validation (Optional<HasType>)
- `minContains()` - Minimum matching elements (Optional<Integer>)
- `maxContains()` - Maximum matching elements (Optional<Integer>)
- `minItems()` - Minimum items (Optional<Integer>)
- `maxItems()` - Maximum items (Optional<Integer>)
- `uniqueItems()` - Unique constraint (Optional<Boolean>)
- `additionalItems()` - Additional items allowed (Optional<Boolean>)
- `unevaluatedItems()` - Unevaluated items allowed (Optional<Boolean>)
- `examples()` - Example values (Optional<List<JsonNode>>)
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
- `examples()` - Example values (Optional<List<JsonNode>>)
- `not()` - not combinator (Optional<HasType>)

### IntegerType

Represents integer schemas.

**Methods:**
- `minimum()` - Minimum value (Optional<Integer>)
- `maximum()` - Maximum value (Optional<Integer>)
- `exclusiveMinimum()` - Exclusive minimum (Draft 6+ numeric syntax) (Optional<Integer>)
- `exclusiveMaximum()` - Exclusive maximum (Draft 6+ numeric syntax) (Optional<Integer>)
- `isMinimumExclusive()` - Whether minimum is exclusive (Draft 4 boolean syntax) (Optional<Boolean>)
- `isMaximumExclusive()` - Whether maximum is exclusive (Draft 4 boolean syntax) (Optional<Boolean>)
- `enumValues()` - Allowed values (Optional<List<Integer>>)
- `constValue()` - Constant value (Optional<Integer>)
- `deprecated()` - Deprecated metadata (Optional<Boolean>)
- `readOnly()` - ReadOnly metadata (Optional<Boolean>)
- `writeOnly()` - WriteOnly metadata (Optional<Boolean>)
- `examples()` - Example values (Optional<List<JsonNode>>)
- `not()` - not combinator (Optional<HasType>)

### NumberType

Represents number (floating-point) schemas.

**Methods:**
- `minimum()` - Minimum value (Optional<Double>)
- `maximum()` - Maximum value (Optional<Double>)
- `exclusiveMinimum()` - Exclusive minimum (Draft 6+ numeric syntax) (Optional<Double>)
- `exclusiveMaximum()` - Exclusive maximum (Draft 6+ numeric syntax) (Optional<Double>)
- `isMinimumExclusive()` - Whether minimum is exclusive (Draft 4 boolean syntax) (Optional<Boolean>)
- `isMaximumExclusive()` - Whether maximum is exclusive (Draft 4 boolean syntax) (Optional<Boolean>)
- `multipleOf()` - Multiple constraint (Optional<Double>)
- `enumValues()` - Allowed values (Optional<List<Double>>)
- `constValue()` - Constant value (Optional<Double>)
- `deprecated()` - Deprecated metadata (Optional<Boolean>)
- `readOnly()` - ReadOnly metadata (Optional<Boolean>)
- `writeOnly()` - WriteOnly metadata (Optional<Boolean>)
- `examples()` - Example values (Optional<List<JsonNode>>)
- `not()` - not combinator (Optional<HasType>)

### BooleanType

Represents boolean schemas.

**Methods:**
- `constValue()` - Constant value (Optional<Boolean>)
- `defaultValue()` - Default value (Optional<Boolean>)
- `examples()` - Example values (Optional<List<JsonNode>>)
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
