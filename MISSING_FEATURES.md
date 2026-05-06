# Missing JSON Schema Features

Анализ workflow.yaml показал следующие JSON Schema фичи, которые используются в реальных схемах, но еще не реализованы в парсере.

## 📊 Сводка

**Всего нереализовано:** 11 фич  
**Реализовано:** 2 фичи (if/then/else, not combinator)  
**Приоритет:** High (4), Medium (5), Low (2)

---

## 🔴 High Priority (используются в workflow.yaml)

### 1. ✨ Conditional Schemas (if/then/else)
**Статус:** ✅ Реализовано  
**Использование в workflow.yaml:** 2 if, 3 then, 1 else  
**JSON Schema Spec:** Draft 2019-09+

**Описание:**
Условные схемы позволяют применять разные валидационные правила в зависимости от значений в данных.

**Пример из workflow.yaml:**
```yaml
if:
  properties:
    type:
      const: credit_card
then:
  required: [cardNumber, cvv]
else:
  required: [bankAccount]
```

**API который нужно добавить:**
```java
// ObjectType.java
public Optional<HasType> ifCondition();
public Optional<HasType> thenSchema();
public Optional<HasType> elseSchema();
```

**Сложность:** Medium  
**Приоритет:** High (используется 6 раз в workflow.yaml)

---

### 2. 🚫 not Combinator (полная реализация)
**Статус:** ✅ Реализовано  
**Использование в workflow.yaml:** 2 раза  
**JSON Schema Spec:** Draft 4+

**Описание:**
Схема not требует, чтобы данные НЕ соответствовали указанной схеме.

**Реализация:**
```java
// Applicators.java
Optional<HasType> getNot(); // ✅ Есть

// Все типы теперь поддерживают not():
// ObjectType, StringType, ArrayType, IntegerType, NumberType, BooleanType
public Optional<HasType> not(); // ✅ Реализовано
```

**Сложность:** Low  
**Приоритет:** High

---

### 3. 📝 patternProperties
**Статус:** ❌ Не реализовано  
**Использование в workflow.yaml:** Нет (но важно для полноты)  
**JSON Schema Spec:** Draft 4+

**Описание:**
Позволяет определить схемы для свойств, чьи имена соответствуют regex паттерну.

**Пример:**
```yaml
patternProperties:
  "^[Ss]_": 
    type: string
  "^[Ii]_":
    type: integer
```

**API:**
```java
// ObjectType.java
public Optional<Map<String, HasType>> patternProperties();
```

**Сложность:** Medium  
**Приоритет:** High (стандартная фича для dynamic properties)

---

### 4. 📋 propertyNames
**Статус:** ❌ Не реализовано  
**Использование в workflow.yaml:** Нет  
**JSON Schema Spec:** Draft 6+

**Описание:**
Определяет схему, которой должны соответствовать ВСЕ имена свойств объекта.

**Пример:**
```yaml
propertyNames:
  pattern: "^[a-zA-Z_][a-zA-Z0-9_]*$"  # Valid identifiers only
```

**API:**
```java
// ObjectType.java
public Optional<HasType> propertyNames();
```

**Сложность:** Low  
**Приоритет:** Medium

---

### 5. 🔗 dependentRequired
**Статус:** ❌ Не реализовано  
**Использование в workflow.yaml:** Нет  
**JSON Schema Spec:** Draft 2019-09+

**Описание:**
Если property X присутствует, то properties Y и Z тоже required.

**Пример:**
```yaml
dependentRequired:
  creditCard: [billingAddress, cvv]
```

**API:**
```java
// ObjectType.java
public Optional<Map<String, List<String>>> dependentRequired();
```

**Сложность:** Low  
**Приоритет:** Medium

---

### 6. 🔗 dependentSchemas
**Статус:** ❌ Не реализовано  
**Использование в workflow.yaml:** Нет  
**JSON Schema Spec:** Draft 2019-09+

**Описание:**
Если property X присутствует, применить дополнительную схему.

**Пример:**
```yaml
dependentSchemas:
  creditCard:
    properties:
      billingAddress:
        type: string
    required: [billingAddress]
```

**API:**
```java
// ObjectType.java
public Optional<Map<String, ObjectType>> dependentSchemas();
```

**Сложность:** Medium  
**Приоритет:** Medium

---

## 🟡 Medium Priority (полезные фичи)

### 7. 📦 prefixItems (tuple validation улучшение)
**Статус:** ❌ Не реализовано  
**Использование в workflow.yaml:** Нет  
**JSON Schema Spec:** Draft 2020-12

**Описание:**
Замена устаревшего синтаксиса tuple validation. Более явная альтернатива array-based items.

**Текущий способ (реализован):**
```yaml
items: [string, number, boolean]  # Old style
```

**Новый способ (prefixItems):**
```yaml
prefixItems:
  - type: string
  - type: number
  - type: boolean
```

**API:**
```java
// ArrayType.java
public Optional<ArrayItemType[]> prefixItems();
```

**Сложность:** Low  
**Приоритет:** Medium

---

### 8. 🔍 contains
**Статус:** ❌ Не реализовано  
**Использование в workflow.yaml:** Нет  
**JSON Schema Spec:** Draft 6+

**Описание:**
Массив должен содержать хотя бы один элемент, соответствующий схеме.

**Пример:**
```yaml
type: array
contains:
  type: number
  minimum: 5
```

**API:**
```java
// ArrayType.java
public Optional<HasType> contains();
public Optional<Integer> minContains();
public Optional<Integer> maxContains();
```

**Сложность:** Low  
**Приоритет:** Medium

---

### 9. 🔍 unevaluatedItems
**Статус:** ❌ Не реализовано  
**Использование в workflow.yaml:** Нет  
**JSON Schema Spec:** Draft 2019-09+

**Описание:**
Контролирует элементы массива, не покрытые items/prefixItems.

**Пример:**
```yaml
prefixItems:
  - type: string
  - type: number
unevaluatedItems: false  # Только 2 элемента разрешены
```

**API:**
```java
// ArrayType.java
public Optional<Boolean> unevaluatedItems();
```

**Сложность:** Low  
**Приоритет:** Medium

---

### 10. 🔢 exclusiveMinimum/exclusiveMaximum (boolean syntax)
**Статус:** ⚠️ Частично реализовано  
**Текущая реализация:** Только numeric syntax (Draft 6+)  
**JSON Schema Spec:** Draft 4 boolean syntax

**Описание:**
Draft 4 использовал boolean флаги, Draft 6+ использует numeric значения.

**Draft 4 (не реализовано):**
```yaml
minimum: 0
exclusiveMinimum: true  # > 0, not >= 0
```

**Draft 6+ (реализовано):**
```yaml
exclusiveMinimum: 0  # > 0
```

**Сложность:** Low  
**Приоритет:** Low (Draft 4 устарел)

---

### 11. 📊 maxLength (String)
**Статус:** ✅ Реализовано  
**Примечание:** Уже есть в StringType, но не используется в workflow.yaml

---

## 🔵 Low Priority (metadata, редко используемые)

### 12. 📝 examples
**Статус:** ❌ Не реализовано  
**Использование в workflow.yaml:** Нет  
**JSON Schema Spec:** Draft 6+

**Описание:**
Массив примеров валидных значений.

**API:**
```java
// Все типы
public Optional<List<JsonNode>> examples();
```

**Сложность:** Low  
**Приоритет:** Low (только metadata)

---

### 13. ⚠️ deprecated
**Статус:** ❌ Не реализовано  
**Использование в workflow.yaml:** Нет  
**JSON Schema Spec:** Draft 2019-09+

**API:**
```java
// Все типы
public Optional<Boolean> deprecated();
```

**Сложность:** Low  
**Приоритет:** Low (только metadata)

---

### 14. 🔒 readOnly / writeOnly
**Статус:** ❌ Не реализовано  
**Использование в workflow.yaml:** Нет  
**JSON Schema Spec:** Draft 7+

**API:**
```java
// Все типы
public Optional<Boolean> readOnly();
public Optional<Boolean> writeOnly();
```

**Сложность:** Low  
**Приоритет:** Low (только metadata для OpenAPI)

---

## 📈 Implementation Roadmap

### Phase 1: Critical Features (workflow.yaml support)
1. ✨ **if/then/else** - Conditional schemas
2. 🚫 **not** - Complete implementation for all types
3. 📝 **patternProperties** - Dynamic property validation

### Phase 2: Standard Features
4. 📋 **propertyNames** - Property name validation
5. 🔗 **dependentRequired** - Dependent requirements
6. 🔗 **dependentSchemas** - Dependent schemas
7. 📦 **prefixItems** - Modern tuple validation

### Phase 3: Array Enhancements
8. 🔍 **contains/minContains/maxContains** - Array contains validation
9. 🔍 **unevaluatedItems** - Unevaluated items control

### Phase 4: Metadata & Polish
10. 📝 **examples** - Value examples
11. ⚠️ **deprecated** - Deprecation flag
12. 🔒 **readOnly/writeOnly** - OpenAPI metadata

---

## 🧪 Test Coverage Requirements

Для каждой новой фичи нужны тесты:
- ✅ Happy path test
- ✅ Edge case test
- ✅ Error handling test
- ✅ Integration test с другими фичами

---

## 📚 References

- [JSON Schema Draft 2020-12](https://json-schema.org/draft/2020-12/json-schema-validation.html)
- [JSON Schema Draft 2019-09](https://json-schema.org/draft/2019-09/json-schema-validation.html)
- [Serverless Workflow Spec](https://serverlessworkflow.io/)
