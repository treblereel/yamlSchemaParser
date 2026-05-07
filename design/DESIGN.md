# yamlSchemaParser Design

## Overview

yamlSchemaParser is a Java library that parses YAML schema files (JSON Schema in YAML format) and converts them into a strongly-typed Java object model. It provides a convenient API for programmatically inspecting schema structure, types, constraints, and relationships, making it easier to build tools that consume or validate JSON Schema definitions.

**Target audience:** Java developers building schema-aware applications, code generators, validators, or documentation tools that need to introspect JSON Schema programmatically.

## Architecture

```
┌─────────────┐
│   Parser    │  Entry point - loads YAML and creates JsonNode
└──────┬──────┘
       │
       ▼
┌──────────────────┐
│ SchemaDefinition │  Root schema container ($id, $schema, model, $defs)
└────────┬─────────┘
         │
         ▼
    ┌────────────┐
    │ NodeFactory│  Type resolution - maps JsonNode to HasType implementations
    └─────┬──────┘
          │
          ▼
     ┌─────────┐
     │ HasType │  Type hierarchy interface
     └────┬────┘
          │
    ┌─────┴────────────────────────────┐
    │                                  │
    ▼                                  ▼
ObjectType                       ArrayType, StringType,
(with Applicators)               IntegerType, NumberType,
                                 BooleanType, NullType,
                                 RefType, AllOfType,
                                 UnionType
```

**Key design principles:**
- **Immutability**: All type classes use `final` fields and record-based structures
- **Type-safe API** (v2.0): Collections return `Map`/`List` directly (not `Optional<Map>`); type-safe accessors (`requireObject()`, `modelAsArray()`) eliminate casting; fluent navigation via `PropertyNavigator`
- **Lazy resolution**: References ($ref) are resolved on-demand, with auto-delegation in v2.0 for transparent navigation
- **LinkedHashMap for ordering**: Collections use LinkedHashMap to preserve schema insertion order (v2.1+)

## Components

### Core Components

| Component | Responsibility |
|-----------|----------------|
| **Parser** | Entry point - loads YAML files and creates SchemaDefinition. Supports Builder pattern for configuration (v2.0) |
| **SchemaDefinition** | Root schema container - provides access to $id, $schema, title, model, and $defs. Type-safe accessors: `modelAsObject()`, `requireObject()` etc. (v2.0) |
| **NodeFactory** | Type resolution factory - maps JsonNode "type" field to appropriate HasType implementation |
| **HasType** | Marker interface for all schema types |

### Type Implementations

| Type | Purpose |
|------|---------|
| **ObjectType** | Represents object schemas with properties, required fields, pattern properties, property names validation, dependent required/schemas, deprecated/readOnly/writeOnly metadata, examples metadata |
| **ArrayType** | Represents array schemas with items, prefixItems (tuple validation), contains validation (minContains/maxContains), unevaluatedItems control, min/max items, unique items constraints, deprecated/readOnly/writeOnly metadata, examples metadata |
| **StringType** | String schemas with pattern, format, min/max length, enum, const, deprecated/readOnly/writeOnly metadata, examples metadata |
| **IntegerType** | Integer schemas with min/max, exclusive bounds, enum, const, deprecated/readOnly/writeOnly metadata, examples metadata |
| **NumberType** | Number (double) schemas with min/max, exclusive bounds, multipleOf, enum, const, deprecated/readOnly/writeOnly metadata, examples metadata |
| **BooleanType** | Boolean schemas with const, default values, deprecated/readOnly/writeOnly metadata, examples metadata |
| **NullType** | Null type schemas |
| **RefType** | $ref references - resolves via `resolve()` or auto-delegates to resolved type (v2.0: `properties()`, `property()`, `pattern()` etc.) |
| **AllOfType** | allOf combinator - must satisfy all schemas |
| **UnionType** | Represents union types (type: [string, null]) with isNullable() detection, lazy type resolution via getResolvedTypes() |

### Supporting Classes

| Class | Purpose |
|-------|---------|
| **Applicators** | Encapsulates schema combinators (allOf, anyOf, oneOf, not, if/then/else) |
| **AdditionalProperties** | Configuration for properties not defined in schema |
| **ArrayItemType** | Wrapper for array item schemas (supports tuple validation) |
| **TypeMismatchException** | Runtime exception thrown by `require*()` methods when type assertion fails (v2.0) |
| **PropertyNavigator** | Fluent navigation API for traversing schema trees - supports chaining: `root.property("address").property("city")` (v2.0) |
| **Discriminator** | OpenAPI polymorphism metadata - propertyName and mapping (full $ref paths) for oneOf/anyOf schemas |

## API

### Public Entry Point

```java
// Simple usage
Parser parser = new Parser();
SchemaDefinition schema = parser.parse("path/to/schema.yaml");

// Configurable (v2.0+)
Parser parser = Parser.builder()
    .strictMode(true)
    .resolveReferences(false)
    .build();
SchemaDefinition schema = parser.parse("path/to/schema.yaml");
```

**Supported input sources:**
- File path (String)
- File object
- URI

### Type-Safe Navigation (v2.0)

```java
// Type-safe root access - no casting
ObjectType root = schema.requireObject();

// Safe map access - collections return Map/List directly
Map<String, HasType> props = root.properties();  // Not Optional<Map>
List<String> required = root.required();  // Not Optional<List>

// Type-safe property access
Optional<StringType> name = root.getPropertyAsString("name");
Optional<String> pattern = name.flatMap(StringType::pattern);

// Fluent navigation - chain 3+ levels
Optional<String> zipPattern = root
    .property("address")
    .property("zipCode")
    .asString()
    .flatMap(StringType::pattern);

// Check property existence
if (root.hasProperty("email")) {
    // ...
}
```

### Reference Resolution (v2.0)

```java
// Auto-resolving RefType - no explicit resolve() needed
RefType addressRef = (RefType) root.properties().get("address");

// Direct navigation through RefType
addressRef.property("city");  // Auto-delegates to resolved type
Map<String, HasType> addressProps = addressRef.properties();  // Auto-resolves

// Type-safe resolution when needed
Optional<ObjectType> address = addressRef.resolveAs(ObjectType.class);
```

## Data Model

### Type Hierarchy

```
HasType (interface)
├── ObjectType       - objects with properties
├── ArrayType        - arrays with items
├── StringType       - strings with constraints
├── IntegerType      - integers with range
├── NumberType       - doubles with range
├── BooleanType      - boolean values
├── NullType         - null type
├── RefType          - $ref references
├── AllOfType        - allOf combinator
└── UnionType        - union types (type: [string, null])
```

### ObjectType Features

**Implemented (as of 2026-05-06):**
- Basic properties, required fields, additional properties
- Pattern properties - regex-based property schemas
- Property names validation - schema for ALL property names
- Dependent required - conditional required fields based on property presence
- Dependent schemas - conditional schema application based on property presence
- Schema combinators: allOf, anyOf, oneOf, not
- Conditional schemas: if/then/else
- Min/max properties constraints

### Schema Combinator Support

| Combinator | Implementation | Location |
|------------|----------------|----------|
| `allOf` | List&lt;HasType&gt; | Applicators → ObjectType, AllOfType |
| `anyOf` | Optional&lt;List&lt;HasType&gt;&gt; | Applicators → ObjectType |
| `oneOf` | Optional&lt;List&lt;HasType&gt;&gt; | Applicators → ObjectType |
| `not` | Optional&lt;HasType&gt; | Applicators → all types |
| `if/then/else` | Optional&lt;HasType&gt; per branch | Applicators → ObjectType |

## Dependencies

### Production Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| **jackson-dataformat-yaml** | 2.20.0 | YAML parsing to JsonNode |
| **json-schema-validator** | 1.5.9 | JSON Schema specification reference (transitive) |

### Test Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| **junit-jupiter-engine** | 5.13.4 | Test framework |
| **junit-jupiter-params** | 5.13.4 | Parameterized tests |
| **assertj-core** | 3.27.4 | Fluent assertions |

**Java version:** 17+ (required)

## Configuration

This is a library with no runtime configuration. All behavior is controlled via:
- Input schema files (YAML format)
- Parser API calls

## Testing Strategy

### Test Coverage Categories

1. **Happy path tests** - Valid schemas parse correctly
2. **Edge case tests** - Empty values, missing optional fields, minimal schemas
3. **Integration tests** - Complex real-world schemas (workflow.yaml)
4. **Correctness tests** - Verify constraints (pattern, min/max, required)
5. **Robustness tests** - Invalid input handling

### Test Organization

| Test Class | Coverage |
|------------|----------|
| **ObjectParserTest** | 75+ tests covering objects, properties, required, refs, combinators |
| **ArrayParserTest** | 20+ tests covering arrays, tuples, nested arrays, constraints |
| **PropertyNamesTest** | 12 tests covering property name validation |
| **DependentRequiredTest** | 14 tests covering conditional required fields |
| **DependentSchemasTest** | 16 tests covering conditional schema application |
| **ConditionalSchemaTest** | 11 tests covering if/then/else |
| **NotCombinatorTest** | 14 tests covering not combinator |
| **PatternPropertiesTest** | 17 tests covering regex-based properties |
| **PrefixItemsTest** | 18 tests covering modern tuple validation with prefixItems |
| **ContainsTest** | 15 tests covering contains, minContains, maxContains array validation |
| **UnevaluatedItemsTest** | 11 tests covering unevaluatedItems constraint for arrays |
| **ExamplesTest** | 12 tests covering examples metadata across all types |
| **DeprecatedTest** | 11 tests covering deprecated metadata flag across all types |
| **ReadOnlyWriteOnlyTest** | 11 tests covering readOnly/writeOnly OpenAPI metadata across all types |
| **ExclusiveBooleanTest** | 10 tests covering Draft 4 boolean syntax for exclusive bounds on integer/number types |
| **WorkflowTest** | 71 tests validating real-world Serverless Workflow DSL schema |
| **WorkflowComprehensiveTest** | 52 tests systematically covering ALL JSON Schema features used in workflow.yaml |
| **ParserErrorHandlingTest** | 12 tests covering error cases |
| **TypeResolutionTest** | 13 tests covering type resolution edge cases |

**Total test count:** 522 tests (as of 2026-05-06, v2.1 release)

## Design Decisions

### Why TreeMap for Property Collections?

**Decision:** Use `TreeMap<>(String::compareTo)` for all property maps (properties, patternProperties, dependentRequired, dependentSchemas)

**Rationale:** Ensures deterministic alphabetical ordering across test runs. Without this, HashMap's iteration order is non-deterministic, causing flaky tests where assertion order depends on JVM hashCode implementation.

**Alternative considered:** LinkedHashMap (insertion order) - rejected because order depends on schema file order, not semantic meaning.

**v2.1 Change:** Reversed to LinkedHashMap for code generation use cases. Preserving schema order enables generating Java code that matches the schema structure exactly, which is more valuable for tooling than alphabetical reproducibility. Users needing alphabetical order can wrap results in TreeMap.

### Why Optional-First API?

**Decision:** All nullable values return `Optional<T>` instead of null

**Rationale:** Forces callers to explicitly handle absence, preventing NullPointerExceptions. Aligns with modern Java best practices (Java 8+).

### Why Lazy $ref Resolution?

**Decision:** RefType stores the reference path as a string; resolution happens on-demand via `resolve()`

**Rationale:** Avoids circular reference issues during parsing. Allows references to be introspected before resolution. Matches JSON Schema's late-binding semantics.

### Why Immutable Records?

**Decision:** Use Java records and final fields for all model classes

**Rationale:** Thread-safe by default. Prevents accidental mutation. Clearer intent - types represent parsed schema, not mutable builders.

## v2.1 Features (2026-05-06)

### Code Generation API Enhancements

**Union Types:**
- `UnionType` class handles `type: ["string", "null"]`
- `getTypes()` returns list of type strings
- `isNullable()` convenience method checks for "null" in types
- `getResolvedTypes()` lazily resolves to concrete HasType instances

**Extension Metadata:**
- `HasType.getExtensions()` returns all x- prefixed properties
- `HasType.getExtension(String key)` returns specific extension
- All types support extensions via `getRawNode()` pattern
- RefType delegates to resolved type

**Property Order Preservation (BREAKING):**
- TreeMap → LinkedHashMap in all ObjectType collections
- Properties, patternProperties, dependentSchemas, dependentRequired
- Schema order preserved, not alphabetical
- Migration: wrap in TreeMap if alphabetical order needed

**Discriminator Support:**
- `Discriminator` record with propertyName and mapping
- `ObjectType.discriminator()` returns Optional<Discriminator>
- Mapping contains full $ref paths
- Supports OpenAPI polymorphism (oneOf/anyOf)

**Test Coverage:** 522 tests (actual count from Task 23)

### v2.0 API Breaking Changes

**Decision:** Remove `Optional<Map>` / `Optional<List>` wrapping from collection-returning methods

**Rationale:** Empty collections are more idiomatic Java than `Optional.empty()`. The pattern `properties().orElse(Map.of())` is verbose; returning `Map.of()` directly is cleaner. Collections are never null in our model, so Optional adds no safety - just ceremony.

**Migration:** Remove `.get()` calls: `properties().get()` → `properties()`

**Alternative considered:** Keep Optional wrapping for consistency - rejected because it creates friction (chained `.get().get()` patterns) without benefit.

### Why Type-Safe Accessors?

**Decision:** Add `modelAsObject()`, `requireObject()` etc. to SchemaDefinition for type-safe access

**Rationale:** Eliminates unsafe casting: `(ObjectType) schema.model()`. The `require*()` methods throw `TypeMismatchException` with descriptive messages when type doesn't match, catching errors early. Optional-returning `modelAs*()` methods enable safe conditional logic.

**Example:**
```java
// Old: unsafe cast
ObjectType root = (ObjectType) schema.model();  // ClassCastException if not ObjectType

// New: type-safe
ObjectType root = schema.requireObject();  // TypeMismatchException with clear message
Optional<ObjectType> maybeRoot = schema.modelAsObject();  // Safe check
```

### Why Fluent Navigation?

**Decision:** Add `PropertyNavigator` interface with `property()`, `item()`, `as*()` methods for chain navigation

**Rationale:** Reduces verbosity for deep property access. Pattern `root.property("a").property("b").asString()` is clearer than manual navigation with intermediate variables and type checks. Integrates with RefType auto-delegation for transparent traversal.

**Example:**
```java
// Old: verbose with intermediate variables
Map<String, HasType> props = ((ObjectType) schema.model()).properties().get();
HasType addressType = props.get("address");
ObjectType address = (ObjectType) ((RefType) addressType).resolve();
Map<String, HasType> addressProps = address.properties().get();
StringType city = (StringType) addressProps.get("city");
Optional<String> pattern = city.pattern();

// New: fluent chain
Optional<String> pattern = schema.requireObject()
    .property("address")
    .property("city")
    .asString()
    .flatMap(StringType::pattern);
```

**Code reduction:** ~60% fewer lines for deep navigation patterns in tests.

## Open Questions / Future Work

### Missing JSON Schema Features

See `MISSING_FEATURES.md` for tracking. Summary:

**All features implemented! (13 total):**
- ✅ if/then/else conditional schemas
- ✅ not combinator
- ✅ patternProperties
- ✅ propertyNames
- ✅ dependentRequired
- ✅ dependentSchemas
- ✅ prefixItems
- ✅ contains / minContains / maxContains
- ✅ unevaluatedItems
- ✅ examples
- ✅ deprecated (metadata)
- ✅ readOnly / writeOnly (OpenAPI metadata)
- ✅ exclusiveMinimum/exclusiveMaximum boolean syntax (Draft 4 compatibility)

### Future Enhancements

1. **Validation API** - Add schema validation against instances (currently only parsing)
2. **Schema generation** - Generate schemas from Java classes
3. **Multi-file schema support** - Handle schemas split across multiple files
4. **Custom format validators** - Plugin system for format validation
5. **Performance optimization** - Lazy parsing for large schemas

---

**Last updated:** 2026-05-06
**Version:** 2.1 (BREAKING from 2.0)

**v2.1 Breaking Changes:**
- Property iteration order changed from alphabetical (TreeMap) to schema order (LinkedHashMap)
- Affects: properties(), patternProperties(), dependentRequired(), dependentSchemas()
- Migration: If alphabetical order needed, wrap result in TreeMap

**v2.1 New Features:**
- Union type support (UnionType class)
- Extension metadata access (getExtensions() / getExtension())
- Discriminator support (Discriminator record)

**v2.0 Breaking Changes:**
- Collections return `Map`/`List` directly (not `Optional<Map>`/`Optional<List>`)
- Migration: remove `.get()` calls on `properties()`, `enum()`, etc.
