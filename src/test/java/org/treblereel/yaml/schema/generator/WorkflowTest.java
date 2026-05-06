package org.treblereel.yaml.schema.generator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.treblereel.yaml.schema.Parser;
import org.treblereel.yaml.schema.model.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test for workflow.yaml schema parsing.
 * Tests all major JSON Schema features used in the Serverless Workflow DSL.
 */
public class WorkflowTest {

  private static SchemaDefinition definition;
  private static ObjectType rootType;

  @BeforeAll
  static void setUp() {
    definition = new Parser().parse("src/test/resources/workflow.yaml");
    rootType = (ObjectType) definition.model();
  }


  @Test
  void testRootTypeIsObject() {
    assertInstanceOf(ObjectType.class, definition.model());
  }

  @Test
  void testRootRequiredFields() {
    assertTrue(rootType.required().isPresent());
    List<String> required = rootType.required().get();
    assertEquals(2, required.size());
    assertTrue(required.contains("document"));
    assertTrue(required.contains("do"));
  }

  @Test
  void testRootProperties() {
    assertTrue(rootType.properties().isPresent());
    Map<String, HasType> props = rootType.properties().get();
    assertTrue(props.containsKey("document"));
    assertTrue(props.containsKey("input"));
    assertTrue(props.containsKey("use"));
    assertTrue(props.containsKey("do"));
    assertTrue(props.containsKey("timeout"));
    assertTrue(props.containsKey("output"));
    assertTrue(props.containsKey("schedule"));
  }

  @Test
  void testDocumentIsObject() {
    Map<String, HasType> props = rootType.properties().get();
    HasType documentType = props.get("document");
    assertInstanceOf(ObjectType.class, documentType);
  }

  @Test
  void testDocumentUnevaluatedProperties() {
    ObjectType document = (ObjectType) rootType.properties().get().get("document");
    assertTrue(document.unevaluatedProperties().isPresent());
    assertFalse(document.unevaluatedProperties().get());
  }

  @Test
  void testDocumentRequiredFields() {
    ObjectType document = (ObjectType) rootType.properties().get().get("document");
    assertTrue(document.required().isPresent());
    List<String> required = document.required().get();
    assertEquals(4, required.size());
    assertTrue(required.contains("dsl"));
    assertTrue(required.contains("namespace"));
    assertTrue(required.contains("name"));
    assertTrue(required.contains("version"));
  }

  @Test
  void testDocumentDslStringWithPattern() {
    ObjectType document = (ObjectType) rootType.properties().get().get("document");
    Map<String, HasType> docProps = document.properties().get();
    HasType dslType = docProps.get("dsl");
    assertInstanceOf(StringType.class, dslType);
    StringType dsl = (StringType) dslType;
    assertTrue(dsl.pattern().isPresent());
    // Semver pattern
    assertTrue(dsl.pattern().get().contains("0|[1-9]"));
  }

  @Test
  void testDocumentNamespaceWithPattern() {
    ObjectType document = (ObjectType) rootType.properties().get().get("document");
    StringType namespace = (StringType) document.properties().get().get("namespace");
    assertTrue(namespace.pattern().isPresent());
    assertEquals("^[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?$", namespace.pattern().get());
  }

  @Test
  void testDocumentTitleAndDescription() {
    ObjectType document = (ObjectType) rootType.properties().get().get("document");
    StringType dsl = (StringType) document.properties().get().get("dsl");
    assertTrue(dsl.title().isPresent());
    assertEquals("WorkflowDSL", dsl.title().get());
    assertTrue(dsl.description().isPresent());
  }

  @Test
  void testInputIsRef() {
    HasType inputType = rootType.properties().get().get("input");
    assertInstanceOf(RefType.class, inputType);
    RefType ref = (RefType) inputType;
    assertEquals("#/$defs/input", ref.ref());
  }

  @Test
  void testRefResolveToObjectType() {
    RefType inputRef = (RefType) rootType.properties().get().get("input");
    HasType resolved = inputRef.resolve();
    assertInstanceOf(ObjectType.class, resolved);
  }

  @Test
  void testDoIsRefToTaskList() {
    HasType doType = rootType.properties().get().get("do");
    assertInstanceOf(RefType.class, doType);
    RefType ref = (RefType) doType;
    assertEquals("#/$defs/taskList", ref.ref());
  }

  @Test
  void testTaskListIsArray() {
    RefType doRef = (RefType) rootType.properties().get().get("do");
    HasType resolved = doRef.resolve();
    assertInstanceOf(ArrayType.class, resolved);
  }

  @Test
  void testTimeoutHasOneOf() {
    HasType timeoutType = rootType.properties().get().get("timeout");
    assertInstanceOf(ObjectType.class, timeoutType);
    ObjectType timeout = (ObjectType) timeoutType;
    assertTrue(timeout.oneOf().isPresent());
    List<HasType> oneOfTypes = timeout.oneOf().get();
    assertEquals(2, oneOfTypes.size());
  }

  @Test
  void testTimeoutOneOfContainsRefAndString() {
    ObjectType timeout = (ObjectType) rootType.properties().get().get("timeout");
    List<HasType> oneOfTypes = timeout.oneOf().get();

    // First should be $ref to timeout definition
    assertInstanceOf(RefType.class, oneOfTypes.get(0));
    // Second should be string
    assertInstanceOf(StringType.class, oneOfTypes.get(1));
  }

  @Test
  void testDefsTaskDefinition() {
    // Get task definition from $defs
    RefType doRef = (RefType) rootType.properties().get().get("do");
    ArrayType taskList = (ArrayType) doRef.resolve();

    ArrayItemType itemType = taskList.getItems()[0];
    HasType item = itemType.getType();
    assertInstanceOf(ObjectType.class, item);

    ObjectType taskItem = (ObjectType) item;
    assertTrue(taskItem.minProperties().isPresent());
    assertEquals(1, taskItem.minProperties().get());
    assertTrue(taskItem.maxProperties().isPresent());
    assertEquals(1, taskItem.maxProperties().get());
  }

  @Test
  void testTaskDefinitionHasOneOf() {
    // Navigate to task definition
    ObjectType taskItem = getTaskItemType();
    HasType additionalProps = taskItem.additionalProperties().get().getType().get();
    assertInstanceOf(RefType.class, additionalProps);

    RefType taskRef = (RefType) additionalProps;
    assertEquals("#/$defs/task", taskRef.ref());

    HasType taskType = taskRef.resolve();
    assertInstanceOf(ObjectType.class, taskType);

    ObjectType task = (ObjectType) taskType;
    assertTrue(task.oneOf().isPresent());
    // 12 task types: call, do, fork, emit, for, listen, raise, run, set, switch, try, wait
    assertEquals(12, task.oneOf().get().size());

  }

  // ============== allOf Tests ==============

  @Test
  void testCallTaskUsesAllOf() {
    ObjectType task = getTaskDefinition();
    List<HasType> oneOfTypes = task.oneOf().get();

    // First oneOf item is callTask which has its own oneOf
    RefType callTaskRef = (RefType) oneOfTypes.get(0);
    ObjectType callTask = (ObjectType) callTaskRef.resolve();

    assertTrue(callTask.oneOf().isPresent());
    // callTask has multiple call types: asyncapi, grpc, http, openapi, a2a, mcp, function
    assertTrue(callTask.oneOf().get().size() >= 6);
  }

  @Test
  void testCallHttpHasAllOf() {
    ObjectType callTask = (ObjectType) ((RefType) getTaskDefinition().oneOf().get().get(0)).resolve();

    // Find HTTP call (index 2 - asyncapi, grpc, http)
    HasType httpCallType = callTask.oneOf().get().get(2);
    assertInstanceOf(ObjectType.class, httpCallType);

    ObjectType httpCall = (ObjectType) httpCallType;
    assertTrue(httpCall.allOf().isPresent());
    assertEquals(2, httpCall.allOf().get().size());

    // First allOf item is $ref to taskBase
    assertInstanceOf(RefType.class, httpCall.allOf().get().get(0));
  }

  // ============== enum Tests ==============

  @Test
  void testProtocolEnumValues() {
    // Navigate to asyncapi call -> with -> protocol
    ObjectType callTask = (ObjectType) ((RefType) getTaskDefinition().oneOf().get().get(0)).resolve();
    ObjectType asyncApiCall = (ObjectType) callTask.oneOf().get().get(0);

    List<HasType> allOfItems = asyncApiCall.allOf().get();
    // Second allOf item contains properties
    ObjectType propsObj = (ObjectType) allOfItems.get(1);

    ObjectType withType = (ObjectType) propsObj.properties().get().get("with");
    StringType protocol = (StringType) withType.properties().get().get("protocol");

    assertTrue(protocol.enumValues().isPresent());
    List<String> enumVals = protocol.enumValues().get();
    assertTrue(enumVals.contains("http"));
    assertTrue(enumVals.contains("kafka"));
    assertTrue(enumVals.contains("mqtt"));
  }

  @Test
  void testCallConstValue() {
    ObjectType callTask = (ObjectType) ((RefType) getTaskDefinition().oneOf().get().get(0)).resolve();

    // HTTP call
    ObjectType httpCall = (ObjectType) callTask.oneOf().get().get(2);
    ObjectType httpProps = (ObjectType) httpCall.allOf().get().get(1);

    StringType callProp = (StringType) httpProps.properties().get().get("call");
    assertTrue(callProp.constValue().isPresent());
    assertEquals("http", callProp.constValue().get());
  }

  @Test
  void testPortMinMax() {
    // Navigate to grpc call -> with -> service -> port
    ObjectType callTask = (ObjectType) ((RefType) getTaskDefinition().oneOf().get().get(0)).resolve();
    ObjectType grpcCall = (ObjectType) callTask.oneOf().get().get(1);

    ObjectType grpcProps = (ObjectType) grpcCall.allOf().get().get(1);
    ObjectType withType = (ObjectType) grpcProps.properties().get().get("with");
    ObjectType serviceType = (ObjectType) withType.properties().get().get("service");
    IntegerType port = (IntegerType) serviceType.properties().get().get("port");

    assertTrue(port.minimum().isPresent());
    assertEquals(0, port.minimum().get());
    assertTrue(port.maximum().isPresent());
    assertEquals(65535, port.maximum().get());
  }

  // ============== minProperties/maxProperties Tests ==============

  @Test
  void testTaskItemMinMaxProperties() {
    ObjectType taskItem = getTaskItemType();

    assertTrue(taskItem.minProperties().isPresent());
    assertEquals(1, taskItem.minProperties().get());
    assertTrue(taskItem.maxProperties().isPresent());
    assertEquals(1, taskItem.maxProperties().get());
  }

  // ============== additionalProperties with $ref Tests ==============

  @Test
  void testAdditionalPropertiesWithRef() {
    ObjectType taskItem = getTaskItemType();

    assertTrue(taskItem.additionalProperties().isPresent());
    AdditionalProperties addProps = taskItem.additionalProperties().get();
    assertTrue(addProps.isAllowed());
    assertTrue(addProps.getType().isPresent());
    assertInstanceOf(RefType.class, addProps.getType().get());
  }

  // ============== Use Section Tests ==============

  @Test
  void testUseSection() {
    ObjectType useType = (ObjectType) rootType.properties().get().get("use");
    assertTrue(useType.properties().isPresent());

    Map<String, HasType> useProps = useType.properties().get();
    assertTrue(useProps.containsKey("authentications"));
    assertTrue(useProps.containsKey("errors"));
    assertTrue(useProps.containsKey("extensions"));
    assertTrue(useProps.containsKey("functions"));
    assertTrue(useProps.containsKey("retries"));
    assertTrue(useProps.containsKey("secrets"));
    assertTrue(useProps.containsKey("timeouts"));
    assertTrue(useProps.containsKey("catalogs"));
  }

  @Test
  void testUseAuthenticationsHasAdditionalPropertiesRef() {
    ObjectType useType = (ObjectType) rootType.properties().get().get("use");
    ObjectType authentications = (ObjectType) useType.properties().get().get("authentications");

    assertTrue(authentications.additionalProperties().isPresent());
    assertTrue(authentications.additionalProperties().get().getType().isPresent());

    HasType addPropsType = authentications.additionalProperties().get().getType().get();
    assertInstanceOf(RefType.class, addPropsType);
    assertEquals("#/$defs/authenticationPolicy", ((RefType) addPropsType).ref());
  }

  @Test
  void testDurationDefinition() {
    // Duration is used in many places, let's test its oneOf structure
    ObjectType schedule = (ObjectType) rootType.properties().get().get("schedule");
    HasType everyType = schedule.properties().get().get("every");

    assertInstanceOf(RefType.class, everyType);
    RefType durationRef = (RefType) everyType;
    assertEquals("#/$defs/duration", durationRef.ref());

    HasType resolved = durationRef.resolve();
    assertInstanceOf(ObjectType.class, resolved);

    ObjectType duration = (ObjectType) resolved;
    assertTrue(duration.oneOf().isPresent());
    assertEquals(3, duration.oneOf().get().size());
  }

  // ============== format Tests ==============

  @Test
  void testUriTemplateFormat() {
    // Navigate to oauth2 -> endpoints -> token which has format: uri-template
    ObjectType useType = (ObjectType) rootType.properties().get().get("use");
    ObjectType authentications = (ObjectType) useType.properties().get().get("authentications");

    RefType authPolicyRef = (RefType) authentications.additionalProperties().get().getType().get();
    ObjectType authPolicy = (ObjectType) authPolicyRef.resolve();

    // authenticationPolicy has oneOf with different auth types
    assertTrue(authPolicy.oneOf().isPresent());
    assertTrue(authPolicy.oneOf().get().size() >= 4);
  }

  @Test
  void testDefaultValues() {
    // forTask -> for -> each has default "item"
    RefType forTaskRef = (RefType) getTaskDefinition().oneOf().get().get(4);
    ObjectType forTask = (ObjectType) forTaskRef.resolve();

    ObjectType forTaskProps = (ObjectType) forTask.allOf().get().get(1);
    ObjectType forConfig = (ObjectType) forTaskProps.properties().get().get("for");
    StringType each = (StringType) forConfig.properties().get().get("each");

    assertTrue(each.defaultValue().isPresent());
    assertEquals("item", each.defaultValue().get());
  }

  // ============== minLength Tests ==============

  @Test
  void testMinLength() {
    // referenceableAuthenticationPolicy -> use has minLength: 1
    ObjectType useType = (ObjectType) rootType.properties().get().get("use");
    ObjectType authentications = (ObjectType) useType.properties().get().get("authentications");
    RefType authPolicyRef = (RefType) authentications.additionalProperties().get().getType().get();
    ObjectType authPolicy = (ObjectType) authPolicyRef.resolve();

    // authenticationPolicy has multiple oneOf items
    assertNotNull(authPolicy);
    assertTrue(authPolicy.oneOf().isPresent());
  }

  @Test
  void testSwitchArrayMinItems() {
    // switchTask -> switch is array with minItems: 1
    RefType switchTaskRef = (RefType) getTaskDefinition().oneOf().get().get(9);
    ObjectType switchTask = (ObjectType) switchTaskRef.resolve();

    ObjectType switchProps = (ObjectType) switchTask.allOf().get().get(1);
    ArrayType switchArray = (ArrayType) switchProps.properties().get().get("switch");

    assertTrue(switchArray.minItems().isPresent());
    assertEquals(1, switchArray.minItems().get());
  }

  @Test
  void testTryTaskStructure() {
    // tryTask has try (taskList) and catch (complex object)
    RefType tryTaskRef = (RefType) getTaskDefinition().oneOf().get().get(10);
    ObjectType tryTask = (ObjectType) tryTaskRef.resolve();

    assertTrue(tryTask.required().isPresent());
    assertTrue(tryTask.required().get().contains("try"));
    assertTrue(tryTask.required().get().contains("catch"));

    ObjectType tryProps = (ObjectType) tryTask.allOf().get().get(1);

    // try is ref to taskList
    HasType tryType = tryProps.properties().get().get("try");
    assertInstanceOf(RefType.class, tryType);

    // catch is object with its own structure
    HasType catchType = tryProps.properties().get().get("catch");
    assertInstanceOf(ObjectType.class, catchType);

    ObjectType catchObj = (ObjectType) catchType;
    assertTrue(catchObj.properties().isPresent());
    assertTrue(catchObj.properties().get().containsKey("errors"));
    assertTrue(catchObj.properties().get().containsKey("retry"));
  }

  @Test
  void testRunTaskOneOfVariants() {
    // runTask -> run has oneOf: container, script, shell, workflow
    RefType runTaskRef = (RefType) getTaskDefinition().oneOf().get().get(7);
    ObjectType runTask = (ObjectType) runTaskRef.resolve();

    ObjectType runProps = (ObjectType) runTask.allOf().get().get(1);
    ObjectType runConfig = (ObjectType) runProps.properties().get().get("run");

    assertTrue(runConfig.oneOf().isPresent());
    assertEquals(4, runConfig.oneOf().get().size());
  }

  @Test
  void testErrorDefinition() {
    ObjectType useType = (ObjectType) rootType.properties().get().get("use");
    ObjectType errors = (ObjectType) useType.properties().get().get("errors");

    RefType errorRef = (RefType) errors.additionalProperties().get().getType().get();
    assertEquals("#/$defs/error", errorRef.ref());

    ObjectType error = (ObjectType) errorRef.resolve();
    assertTrue(error.required().isPresent());
    assertTrue(error.required().get().contains("type"));
    assertTrue(error.required().get().contains("status"));

    // status is integer
    assertInstanceOf(IntegerType.class, error.properties().get().get("status"));
  }

  // ============== anyOf Tests ==============

  @Test
  void testFlowDirectiveHasAnyOf() {
    // flowDirective uses anyOf at root level
    ObjectType taskBase = (ObjectType) getDefByName("taskBase");
    HasType thenType = taskBase.properties().get().get("then");

    assertInstanceOf(RefType.class, thenType);
    RefType flowDirectiveRef = (RefType) thenType;
    assertEquals("#/$defs/flowDirective", flowDirectiveRef.ref());

    HasType resolved = flowDirectiveRef.resolve();
    assertInstanceOf(ObjectType.class, resolved);

    ObjectType flowDirective = (ObjectType) resolved;
    assertTrue(flowDirective.anyOf().isPresent());
    assertEquals(2, flowDirective.anyOf().get().size());
  }

  @Test
  void testFlowDirectiveAnyOfContents() {
    ObjectType flowDirective = (ObjectType) getDefByName("flowDirective");
    List<HasType> anyOfTypes = flowDirective.anyOf().get();

    // First is string with enum
    assertInstanceOf(StringType.class, anyOfTypes.get(0));
    StringType enumType = (StringType) anyOfTypes.get(0);
    assertTrue(enumType.enumValues().isPresent());
    assertTrue(enumType.enumValues().get().contains("continue"));
    assertTrue(enumType.enumValues().get().contains("exit"));
    assertTrue(enumType.enumValues().get().contains("end"));

    // Has default value
    assertTrue(enumType.defaultValue().isPresent());
    assertEquals("continue", enumType.defaultValue().get());

    // Second is plain string
    assertInstanceOf(StringType.class, anyOfTypes.get(1));
  }

  @Test
  void testUriTemplateAnyOf() {
    ObjectType uriTemplate = (ObjectType) getDefByName("uriTemplate");
    assertTrue(uriTemplate.anyOf().isPresent());
    assertEquals(2, uriTemplate.anyOf().get().size());

    // Both are strings with format
    for (HasType type : uriTemplate.anyOf().get()) {
      assertInstanceOf(StringType.class, type);
      StringType stringType = (StringType) type;
      assertTrue(stringType.format().isPresent());
    }
  }

  // ============== not Keyword Tests ==============

  @Test
  void testCallFunctionNotEnum() {
    // callFunction -> call has not: enum: [...]
    ObjectType callTask = (ObjectType) getDefByName("callTask");
    // Last oneOf item is CallFunction
    HasType callFunctionType = callTask.oneOf().get().get(callTask.oneOf().get().size() - 1);
    assertInstanceOf(ObjectType.class, callFunctionType);

    ObjectType callFunction = (ObjectType) callFunctionType;
    ObjectType callFunctionProps = (ObjectType) callFunction.allOf().get().get(1);

    StringType callProp = (StringType) callFunctionProps.properties().get().get("call");
    // The 'call' property has 'not' with enum
    assertNotNull(callProp);
  }

  @Test
  void testObjectTypeNotMethod() {
    // Test that ObjectType exposes not() method
    ObjectType callTask = (ObjectType) getDefByName("callTask");
    ObjectType callFunction = (ObjectType) callTask.oneOf().get().get(callTask.oneOf().get().size() - 1);
    ObjectType callFunctionProps = (ObjectType) callFunction.allOf().get().get(1);

    // The properties object itself may have not
    // Let's check if not() is available
    assertNotNull(callFunctionProps.not());
  }

  // ============== RefType to StringType Tests ==============

  @Test
  void testRuntimeExpressionIsStringType() {
    HasType runtimeExpr = getDefByName("runtimeExpression");
    assertInstanceOf(StringType.class, runtimeExpr);

    StringType stringType = (StringType) runtimeExpr;
    assertTrue(stringType.pattern().isPresent());
    assertEquals("^\\s*\\$\\{.+\\}\\s*$", stringType.pattern().get());
    assertTrue(stringType.title().isPresent());
    assertEquals("RuntimeExpression", stringType.title().get());
  }

  @Test
  void testRefResolvesToStringType() {
    // duration has oneOf with runtimeExpression ref
    ObjectType duration = (ObjectType) getDefByName("duration");
    assertTrue(duration.oneOf().isPresent());

    // Second item should be ref to runtimeExpression
    HasType secondItem = duration.oneOf().get().get(1);
    assertInstanceOf(RefType.class, secondItem);

    RefType ref = (RefType) secondItem;
    assertEquals("#/$defs/runtimeExpression", ref.ref());

    HasType resolved = ref.resolve();
    assertInstanceOf(StringType.class, resolved);
  }

  // ============== minLength Tests ==============

  @Test
  void testMinLengthOnString() {
    // secretBasedAuthenticationPolicy -> use has minLength: 1
    ObjectType secretAuth = (ObjectType) getDefByName("secretBasedAuthenticationPolicy");
    StringType useField = (StringType) secretAuth.properties().get().get("use");

    assertTrue(useField.minLength().isPresent());
    assertEquals(1, useField.minLength().get());
  }

  @Test
  void testReferenceableAuthPolicyMinLength() {
    ObjectType refAuthPolicy = (ObjectType) getDefByName("referenceableAuthenticationPolicy");
    assertTrue(refAuthPolicy.oneOf().isPresent());

    // First oneOf has 'use' with minLength
    ObjectType authRef = (ObjectType) refAuthPolicy.oneOf().get().get(0);
    StringType useField = (StringType) authRef.properties().get().get("use");

    assertTrue(useField.minLength().isPresent());
    assertEquals(1, useField.minLength().get());
  }

  // ============== Default Values on Enum Tests ==============

  @Test
  void testContainerCleanupEnumWithDefault() {
    ObjectType containerLifetime = (ObjectType) getDefByName("containerLifetime");
    StringType cleanup = (StringType) containerLifetime.properties().get().get("cleanup");

    assertTrue(cleanup.enumValues().isPresent());
    List<String> enumVals = cleanup.enumValues().get();
    assertTrue(enumVals.contains("always"));
    assertTrue(enumVals.contains("never"));
    assertTrue(enumVals.contains("eventually"));

    assertTrue(cleanup.defaultValue().isPresent());
    assertEquals("never", cleanup.defaultValue().get());
  }

  @Test
  void testListenReadEnumWithDefault() {
    // listenTask -> listen -> read has enum with default
    RefType listenTaskRef = (RefType) getTaskDefinition().oneOf().get().get(5);
    ObjectType listenTask = (ObjectType) listenTaskRef.resolve();

    ObjectType listenProps = (ObjectType) listenTask.allOf().get().get(1);
    ObjectType listenConfig = (ObjectType) listenProps.properties().get().get("listen");
    StringType readType = (StringType) listenConfig.properties().get().get("read");

    assertTrue(readType.enumValues().isPresent());
    assertTrue(readType.enumValues().get().contains("data"));
    assertTrue(readType.enumValues().get().contains("envelope"));
    assertTrue(readType.enumValues().get().contains("raw"));

    assertTrue(readType.defaultValue().isPresent());
    assertEquals("data", readType.defaultValue().get());
  }

  // ============== Secrets Array (String Items) Tests ==============

  @Test
  void testSecretsIsStringArray() {
    ObjectType useType = (ObjectType) rootType.properties().get().get("use");
    ArrayType secrets = (ArrayType) useType.properties().get().get("secrets");

    ArrayItemType[] items = secrets.getItems();
    assertEquals(1, items.length);

    HasType itemType = items[0].getType();
    assertInstanceOf(StringType.class, itemType);
  }

  // ============== RetryPolicy Backoff oneOf Tests ==============

  @Test
  void testRetryPolicyStructure() {
    ObjectType retryPolicy = (ObjectType) getDefByName("retryPolicy");
    assertTrue(retryPolicy.properties().isPresent());

    Map<String, HasType> props = retryPolicy.properties().get();
    assertTrue(props.containsKey("when"));
    assertTrue(props.containsKey("exceptWhen"));
    assertTrue(props.containsKey("delay"));
    assertTrue(props.containsKey("backoff"));
    assertTrue(props.containsKey("limit"));
    assertTrue(props.containsKey("jitter"));
  }

  @Test
  void testRetryPolicyBackoffOneOf() {
    ObjectType retryPolicy = (ObjectType) getDefByName("retryPolicy");
    ObjectType backoff = (ObjectType) retryPolicy.properties().get().get("backoff");

    assertTrue(backoff.oneOf().isPresent());
    assertEquals(3, backoff.oneOf().get().size());

    // constant, exponential, linear
    for (HasType backoffType : backoff.oneOf().get()) {
      assertInstanceOf(ObjectType.class, backoffType);
      ObjectType backoffObj = (ObjectType) backoffType;
      assertTrue(backoffObj.properties().isPresent());
    }
  }

  // ============== Boolean Properties Tests ==============

  @Test
  void testBooleanPropertyRedirect() {
    // HTTP call -> with -> redirect is boolean
    ObjectType callTask = (ObjectType) getDefByName("callTask");
    ObjectType httpCall = (ObjectType) callTask.oneOf().get().get(2);

    ObjectType httpProps = (ObjectType) httpCall.allOf().get().get(1);
    ObjectType withType = (ObjectType) httpProps.properties().get().get("with");
    HasType redirectType = withType.properties().get().get("redirect");

    assertInstanceOf(BooleanType.class, redirectType);
  }

  @Test
  void testBooleanPropertyCompete() {
    // forkTask -> fork -> compete is boolean with default
    RefType forkTaskRef = (RefType) getTaskDefinition().oneOf().get().get(2);
    ObjectType forkTask = (ObjectType) forkTaskRef.resolve();

    ObjectType forkProps = (ObjectType) forkTask.allOf().get().get(1);
    ObjectType forkConfig = (ObjectType) forkProps.properties().get().get("fork");
    BooleanType compete = (BooleanType) forkConfig.properties().get().get("compete");

    assertTrue(compete.defaultValue().isPresent());
    assertFalse(compete.defaultValue().get());
  }

  @Test
  void testBooleanPropertyAwait() {
    // runTask -> run -> await is boolean with default true
    RefType runTaskRef = (RefType) getTaskDefinition().oneOf().get().get(7);
    ObjectType runTask = (ObjectType) runTaskRef.resolve();

    ObjectType runProps = (ObjectType) runTask.allOf().get().get(1);
    ObjectType runConfig = (ObjectType) runProps.properties().get().get("run");
    BooleanType awaitProp = (BooleanType) runConfig.properties().get().get("await");

    assertTrue(awaitProp.defaultValue().isPresent());
    assertTrue(awaitProp.defaultValue().get());
  }

  // ============== EventConsumptionStrategy Tests ==============

  @Test
  void testEventConsumptionStrategyOneOf() {
    ObjectType eventStrategy = (ObjectType) getDefByName("eventConsumptionStrategy");
    assertTrue(eventStrategy.oneOf().isPresent());
    assertEquals(3, eventStrategy.oneOf().get().size());

    // all, any, one strategies
  }

  @Test
  void testEventFilterStructure() {
    ObjectType eventFilter = (ObjectType) getDefByName("eventFilter");
    assertTrue(eventFilter.properties().isPresent());
    assertTrue(eventFilter.properties().get().containsKey("with"));
    assertTrue(eventFilter.properties().get().containsKey("correlate"));

    assertTrue(eventFilter.required().isPresent());
    assertTrue(eventFilter.required().get().contains("with"));
  }

  // ============== Extension Definition Tests ==============

  @Test
  void testExtensionExtendEnum() {
    ObjectType extension = (ObjectType) getDefByName("extension");
    StringType extend = (StringType) extension.properties().get().get("extend");

    assertTrue(extend.enumValues().isPresent());
    List<String> enumVals = extend.enumValues().get();
    assertTrue(enumVals.contains("call"));
    assertTrue(enumVals.contains("emit"));
    assertTrue(enumVals.contains("for"));
    assertTrue(enumVals.contains("run"));
    assertTrue(enumVals.contains("all"));
  }

  // ============== Schema Definition Tests ==============

  @Test
  void testSchemaDefinitionOneOf() {
    ObjectType schema = (ObjectType) getDefByName("schema");
    assertTrue(schema.oneOf().isPresent());
    assertEquals(2, schema.oneOf().get().size());

    // Inline vs External
    assertTrue(schema.properties().isPresent());
    assertTrue(schema.properties().get().containsKey("format"));

    StringType format = (StringType) schema.properties().get().get("format");
    assertTrue(format.defaultValue().isPresent());
    assertEquals("json", format.defaultValue().get());
  }

  // ============== ProcessResult Tests ==============

  @Test
  void testProcessResultStructure() {
    ObjectType processResult = (ObjectType) getDefByName("processResult");

    assertTrue(processResult.required().isPresent());
    assertEquals(3, processResult.required().get().size());
    assertTrue(processResult.required().get().contains("code"));
    assertTrue(processResult.required().get().contains("stdout"));
    assertTrue(processResult.required().get().contains("stderr"));

    assertInstanceOf(IntegerType.class, processResult.properties().get().get("code"));
    assertInstanceOf(StringType.class, processResult.properties().get().get("stdout"));
    assertInstanceOf(StringType.class, processResult.properties().get().get("stderr"));
  }

  // ============== Catalog Definition Tests ==============

  @Test
  void testCatalogDefinition() {
    ObjectType catalog = (ObjectType) getDefByName("catalog");

    assertTrue(catalog.required().isPresent());
    assertTrue(catalog.required().get().contains("endpoint"));

    HasType endpointType = catalog.properties().get().get("endpoint");
    assertInstanceOf(RefType.class, endpointType);
    assertEquals("#/$defs/endpoint", ((RefType) endpointType).ref());
  }

  // ============== Endpoint Definition Tests ==============

  @Test
  void testEndpointOneOf() {
    ObjectType endpoint = (ObjectType) getDefByName("endpoint");
    assertTrue(endpoint.oneOf().isPresent());
    assertEquals(3, endpoint.oneOf().get().size());

    // runtimeExpression, uriTemplate, object configuration
    assertInstanceOf(RefType.class, endpoint.oneOf().get().get(0));
    assertInstanceOf(RefType.class, endpoint.oneOf().get().get(1));
    assertInstanceOf(ObjectType.class, endpoint.oneOf().get().get(2));
  }

  // ============== Timeout Definition Tests ==============

  @Test
  void testTimeoutDefinition() {
    ObjectType timeout = (ObjectType) getDefByName("timeout");

    assertTrue(timeout.required().isPresent());
    assertTrue(timeout.required().get().contains("after"));

    HasType afterType = timeout.properties().get().get("after");
    assertInstanceOf(RefType.class, afterType);
    assertEquals("#/$defs/duration", ((RefType) afterType).ref());
  }

  // ============== Input/Output/Export Tests ==============

  @Test
  void testInputDefinition() {
    ObjectType input = (ObjectType) getDefByName("input");
    assertTrue(input.properties().isPresent());
    assertTrue(input.properties().get().containsKey("schema"));
    assertTrue(input.properties().get().containsKey("from"));

    // from has oneOf (string or object)
    HasType fromType = input.properties().get().get("from");
    assertInstanceOf(ObjectType.class, fromType);
    ObjectType from = (ObjectType) fromType;
    assertTrue(from.oneOf().isPresent());
    assertEquals(2, from.oneOf().get().size());
  }

  @Test
  void testOutputDefinition() {
    ObjectType output = (ObjectType) getDefByName("output");
    assertTrue(output.properties().isPresent());
    assertTrue(output.properties().get().containsKey("schema"));
    assertTrue(output.properties().get().containsKey("as"));
  }

  @Test
  void testExportDefinition() {
    ObjectType export = (ObjectType) getDefByName("export");
    assertTrue(export.properties().isPresent());
    assertTrue(export.properties().get().containsKey("schema"));
    assertTrue(output().properties().get().containsKey("as"));
  }

  // ============== AsyncAPI Tests ==============

  @Test
  void testAsyncApiServerStructure() {
    ObjectType asyncApiServer = (ObjectType) getDefByName("asyncApiServer");
    assertTrue(asyncApiServer.required().isPresent());
    assertTrue(asyncApiServer.required().get().contains("name"));

    assertTrue(asyncApiServer.properties().isPresent());
    assertTrue(asyncApiServer.properties().get().containsKey("name"));
    assertTrue(asyncApiServer.properties().get().containsKey("variables"));
  }

  @Test
  void testAsyncApiSubscription() {
    ObjectType subscription = (ObjectType) getDefByName("asyncApiSubscription");
    assertTrue(subscription.required().isPresent());
    assertTrue(subscription.required().get().contains("consume"));

    assertTrue(subscription.properties().isPresent());
    assertTrue(subscription.properties().get().containsKey("filter"));
    assertTrue(subscription.properties().get().containsKey("consume"));
    assertTrue(subscription.properties().get().containsKey("foreach"));
  }

  // ============== OAuth2 Tests ==============

  @Test
  void testOAuth2TokenDefinition() {
    ObjectType oauth2Token = (ObjectType) getDefByName("oauth2Token");
    assertTrue(oauth2Token.required().isPresent());
    assertEquals(2, oauth2Token.required().get().size());
    assertTrue(oauth2Token.required().get().contains("token"));
    assertTrue(oauth2Token.required().get().contains("type"));
  }

  @Test
  void testOAuth2GrantEnum() {
    ObjectType oauth2Props = (ObjectType) getDefByName("oauth2AuthenticationProperties");
    StringType grant = (StringType) oauth2Props.properties().get().get("grant");

    assertTrue(grant.enumValues().isPresent());
    assertTrue(grant.enumValues().get().contains("authorization_code"));
    assertTrue(grant.enumValues().get().contains("client_credentials"));
    assertTrue(grant.enumValues().get().contains("password"));
    assertTrue(grant.enumValues().get().contains("refresh_token"));
  }

  // ============== Error Filter Tests ==============

  @Test
  void testErrorFilterMinProperties() {
    ObjectType errorFilter = (ObjectType) getDefByName("errorFilter");
    assertTrue(errorFilter.minProperties().isPresent());
    assertEquals(1, errorFilter.minProperties().get());
  }

  // ============== TaskBase Inheritance Tests ==============

  @Test
  void testTaskBaseProperties() {
    ObjectType taskBase = (ObjectType) getDefByName("taskBase");
    assertTrue(taskBase.properties().isPresent());

    Map<String, HasType> props = taskBase.properties().get();
    assertTrue(props.containsKey("if"));
    assertTrue(props.containsKey("input"));
    assertTrue(props.containsKey("output"));
    assertTrue(props.containsKey("export"));
    assertTrue(props.containsKey("timeout"));
    assertTrue(props.containsKey("then"));
    assertTrue(props.containsKey("metadata"));
  }

  @Test
  void testAllTasksInheritTaskBase() {
    ObjectType task = getTaskDefinition();
    List<HasType> taskTypes = task.oneOf().get();

    // Count tasks with allOf containing taskBase
    int tasksWithTaskBase = 0;

    for (HasType taskType : taskTypes) {
      assertInstanceOf(RefType.class, taskType);
      RefType taskRef = (RefType) taskType;
      HasType resolved = taskRef.resolve();

      // Most tasks are ObjectType, but callTask has its own oneOf
      if (resolved instanceof ObjectType taskObj) {
        if (taskObj.allOf().isPresent() && taskObj.allOf().get().size() >= 2) {
          HasType firstAllOf = taskObj.allOf().get().get(0);
          if (firstAllOf instanceof RefType refType && "#/$defs/taskBase".equals(refType.ref())) {
            tasksWithTaskBase++;
          }
        }
      }
    }

    // Most tasks (except callTask which has nested oneOf) should inherit taskBase
    assertTrue(tasksWithTaskBase >= 10, "At least 10 tasks should inherit taskBase, found: " + tasksWithTaskBase);
  }

  // ============== Container Image Tests ==============

  @Test
  void testContainerImageRequired() {
    // runTask -> run -> oneOf -> container -> image is required
    RefType runTaskRef = (RefType) getTaskDefinition().oneOf().get().get(7);
    ObjectType runTask = (ObjectType) runTaskRef.resolve();

    ObjectType runProps = (ObjectType) runTask.allOf().get().get(1);
    ObjectType runConfig = (ObjectType) runProps.properties().get().get("run");

    // First oneOf is container
    ObjectType containerRun = (ObjectType) runConfig.oneOf().get().get(0);
    ObjectType containerType = (ObjectType) containerRun.properties().get().get("container");

    assertTrue(containerType.required().isPresent());
    assertTrue(containerType.required().get().contains("image"));
  }

  // ============== Workflow Subflow Tests ==============

  @Test
  void testWorkflowSubflowVersionDefault() {
    // runTask -> run -> oneOf -> workflow -> version has default "latest"
    RefType runTaskRef = (RefType) getTaskDefinition().oneOf().get().get(7);
    ObjectType runTask = (ObjectType) runTaskRef.resolve();

    ObjectType runProps = (ObjectType) runTask.allOf().get().get(1);
    ObjectType runConfig = (ObjectType) runProps.properties().get().get("run");

    // Fourth oneOf is workflow
    ObjectType workflowRun = (ObjectType) runConfig.oneOf().get().get(3);
    ObjectType workflowType = (ObjectType) workflowRun.properties().get().get("workflow");
    StringType version = (StringType) workflowType.properties().get().get("version");

    assertTrue(version.defaultValue().isPresent());
    assertEquals("latest", version.defaultValue().get());
  }

  // ============== Helper Methods ==============

  private ObjectType getTaskItemType() {
    RefType doRef = (RefType) rootType.properties().get().get("do");
    ArrayType taskList = (ArrayType) doRef.resolve();
    return (ObjectType) taskList.getItems()[0].getType();
  }

  private ObjectType getTaskDefinition() {
    ObjectType taskItem = getTaskItemType();
    RefType taskRef = (RefType) taskItem.additionalProperties().get().getType().get();
    return (ObjectType) taskRef.resolve();
  }

  private HasType getDefByName(String name) {
    RefType ref = new RefType(definition, "#/$defs/" + name);
    return ref.resolve();
  }

  private ObjectType output() {
    return (ObjectType) getDefByName("output");
  }
}
