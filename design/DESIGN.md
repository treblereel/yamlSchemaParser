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
                                 RefType, AllOfType
```

**Key design principles:**
- **Immutability**: All type classes use `final` fields and record-based structures
- **Optional-first API**: All nullable values return `Optional<T>` to prevent NullPointerExceptions
- **Lazy resolution**: References ($ref) are resolved on-demand via `RefType.resolve()`
- **TreeMap for ordering**: Collections use `TreeMap` for deterministic alphabetical ordering (reproducibility)

## Components

### Core Components

| Component | Responsibility |
|-----------|----------------|
| **Parser** | Entry point - loads YAML files and creates SchemaDefinition |
| **SchemaDefinition** | Root schema container - provides access to $id, $schema, title, model, and $defs |
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
| **RefType** | $ref references - resolves to target type via `resolve()` |
| **AllOfType** | allOf combinator - must satisfy all schemas |

### Supporting Classes

| Class | Purpose |
|-------|---------|
| **Applicators** | Encapsulates schema combinators (allOf, anyOf, oneOf, not, if/then/else) |
| **AdditionalProperties** | Configuration for properties not defined in schema |
| **ArrayItemType** | Wrapper for array item schemas (supports tuple validation) |

## API

### Public Entry Point

```java
Parser parser = new Parser();
SchemaDefinition schema = parser.parse("path/to/schema.yaml");
```

**Supported input sources:**
- File path (String)
- File object
- URI

### Type-Safe Navigation

```java
// Access root type
ObjectType root = (ObjectType) schema.model();

// Navigate properties
Map<String, HasType> props = root.properties().get();
StringType name = (StringType) props.get("name");

// Check constraints
List<String> required = root.required().get();
Optional<String> pattern = name.pattern();
```

### Reference Resolution

```java
// Get reference
RefType ref = (RefType) props.get("address");
String refPath = ref.ref();  // "#/$defs/Address"

// Resolve to target type
ObjectType address = (ObjectType) ref.resolve();
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
└── AllOfType        - allOf combinator
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
| **ParserErrorHandlingTest** | 12 tests covering error cases |
| **TypeResolutionTest** | 13 tests covering type resolution edge cases |

**Total test count:** 363 tests (as of 2026-05-06)

## Design Decisions

### Why TreeMap for Property Collections?

**Decision:** Use `TreeMap<>(String::compareTo)` for all property maps (properties, patternProperties, dependentRequired, dependentSchemas)

**Rationale:** Ensures deterministic alphabetical ordering across test runs. Without this, HashMap's iteration order is non-deterministic, causing flaky tests where assertion order depends on JVM hashCode implementation.

**Alternative considered:** LinkedHashMap (insertion order) - rejected because order depends on schema file order, not semantic meaning.

### Why Optional-First API?

**Decision:** All nullable values return `Optional<T>` instead of null

**Rationale:** Forces callers to explicitly handle absence, preventing NullPointerExceptions. Aligns with modern Java best practices (Java 8+).

### Why Lazy $ref Resolution?

**Decision:** RefType stores the reference path as a string; resolution happens on-demand via `resolve()`

**Rationale:** Avoids circular reference issues during parsing. Allows references to be introspected before resolution. Matches JSON Schema's late-binding semantics.

### Why Immutable Records?

**Decision:** Use Java records and final fields for all model classes

**Rationale:** Thread-safe by default. Prevents accidental mutation. Clearer intent - types represent parsed schema, not mutable builders.

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
**Version:** 1.0-SNAPSHOT
