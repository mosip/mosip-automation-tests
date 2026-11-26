# MOSIP DSL Scenario Authoring Guide

This is the updated, detailed, beginner-friendly version for writing MOSIP DSL scenarios.

---

## 1) Big Picture: How this DSL Works

You write readable Gherkin steps.
The framework translates them into internal e2e_* actions and executes Java step classes.

**Main Files**

- **Scenarios**
  `mosip-acceptance-tests/ivv-orchestrator/src/main/resources/config/scenarios.feature`
- **Parser / Translator**
  - ReadableDslStepCodec.java
  - GherkinStepTranslator.java
- **Step Labels**
  `step-parameter-labels.json`
- **Variable Display Mappings**
  `variable-display-names.json`
- **Runtime Variable Resolver**
  `VariableStore.java`

---

## 2) Exact Step Sentence Pattern

**Canonical Form**

```text
Given|When|Then|And I <action phrase> where <label> is <value>, and <label> is <value> ...
```

**Save Output**

```text
... and store result in <saved variable display name>
```

**Reuse Output**

```text
... is the saved <saved variable display name>
```

**Example**

```gherkin
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
```

---

## 3) Variable Passing (Most Important)

### 3.1 Save a Value

Use:

```text
and store result in <name>
```

**Examples**

- registration ID
- UIN
- packet template path
- environment 1 details

---

### 3.2 Reuse a Value

Use:

```text
the saved <name>
```

**Examples**

```gherkin
registration ID is the saved registration ID
resident UIN is the saved UIN
```

---

### 3.3 Internal Mapping

Readable names map to internal variables (starting with `$$`):

| Readable Name           | Internal Variable      |
|-------------------------|-------------------------|
| registration ID         | `$$rid`                 |
| second registration ID  | `$$rid2`                |
| UIN                      | `$$uin`                 |
| email                    | `$$email`               |
| environment 1 details   | `$$details1`            |
| persona file path        | `$$personaFilePath`     |

---

### 3.4 Scope Lookup Order

1. Step scope
2. Scenario scope
3. Global scope

If missing/empty, execution fails.

---

## 4) Keywords and Parameter Formatting

**Keywords**

- Given → initial setup/start action
- And → continuation
- Then → assertions/validation

---

**Formatting Rules**

- Key-value separator: `is`
- Parameter separator: `, and`

**Correct Sample**

```gherkin
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
```

---

## 5) Common Key Meanings

| Key                       | Meaning                                              |
|----------------------------|-------------------------------------------------------|
| context key                | runtime context selector (usually env_context)        |
| pre-requisite details      | environment object read from pre-req index             |
| generate private key       | signing/key generation toggle                          |
| persona type                | adult/minor/infant/senior                              |
| guardian flag               | guardian linkage behavior                               |
| gender and biometric flags  | gender followed by the biometric boolean flags (e.g. `Male and false and false and true`) |
| packet type                 | NEW/UPDATE/LOST/BIOMETRIC_CORRECTION/etc.              |
| registration ID             | RID                                                     |
| UIN                          | unique identity number                                  |
| VID                           | virtual ID                                              |
| RID stage + stage status    | pipeline assertion values                               |

---

## 6) Recommended Scenario Build Flow

**Step A - Bootstrap**

- health checks
- read prereq
- set context
- targetenv health

---

**Step B - Prepare Inputs**

- get resident data
- optional demo/bio changes
- get packet template

---

**Step C - Execute Packet Flow**

- generate/upload
- check status
- get UIN/email/VID
- verify notification

---

**Step D - Assertions**

- stage/status assertions
- auth/eKYC validations

---

**Step E - Cleanup**

Always include:

```gherkin
And I delete packet data
```

---

## 7) Template for New Scenario

```gherkin
Scenario: <Clear business title>

Given I get ping health where component is packetcreator

And I read pre req where pre-requisite data index is 1 and store result in environment 1 details

And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false

And I get ping health where component is targetenv

And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and false and false and true and store result in persona file path

And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path

And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID

And I check status where packet status is PROCESSED, and registration ID is the saved registration ID

And I get uin by rid where source registration ID is the saved registration ID and store result in UIN

Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED

And I delete packet data
```

---

## 8) Do / Don't

**Do**

- Reuse saved variables for dynamic IDs
- Keep Then steps for assertions
- Keep cleanup in every scenario

---

**Don't**

- Use a variable before storing it
- Change saved display names randomly
- Skip failure-stage checks in negative tests

---

## 9) Common Problems and Fixes

**Problem**

the saved value is missing

**Fix**

Verify producer step has:

```text
store result in <same name>
```

---

**Problem**

parser says invalid parameter clause

**Fix**

Use exact format:

```text
label is value, and label is value
```

---

**Problem**

packet rejected unexpectedly

**Fix**

Compare with nearest working scenario of same flow type.

---

## 10) Author Checklist

- Setup steps present
- Each reused variable produced earlier
- Explicit assertions present
- Cleanup present
- Labels match `step-parameter-labels.json`

---

## 11) Full Step Reference

Authoritative action → parameter mapping is maintained in:

```text
mosip-acceptance-tests/ivv-parser/src/main/resources/step-parameter-labels.json
```

Use this file as the final contract while adding or editing scenarios.

---

## 12) Deep Explanation of scenario_2

---

**Scenario Tags**

| Tag                              | Meaning                       |
|-----------------------------------|--------------------------------|
| `@scenario_2`                    | unique scenario id             |
| `@Positive_Test`                 | expected happy-path behavior    |
| `@persona_ResidentFemaleAdult`   | persona classification          |
| `@group_Adult_New`               | business grouping               |

---

**Step 1**

```gherkin
Given I get ping health where component is packetcreator
```

**Explanation**

- get ping health = service availability check
- component = parameter key
- packetcreator = service name

---

**Step 2**

```gherkin
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
```

**Explanation**

- read pre req = load environment prereq data
- pre-requisite data index = which record to read
- 1 = prereq id
- store result in environment 1 details = save output variable for reuse

---

**Step 3**

```gherkin
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
```

**Explanation**

- set context = initialize runtime execution context
- context key = context namespace
- env_context = default environment context
- pre-requisite details = env payload argument
- the saved environment 1 details = consume previously stored value
- generate private key = key generation toggle
- false = do not generate key in this flow

---

**Step 4**

```gherkin
And I get ping health where component is targetenv
```

**Explanation**

Validates target backend readiness.

---

**Step 5**

```gherkin
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and false and false and true and store result in persona file path
```

**Explanation**

- get resident data = generate persona input
- persona type = resident category (adult)
- guardian flag = guardian requirement (false)
- gender and biometric flags = gender followed by the three biometric boolean flags
- Female = requested gender value
- store result in persona file path = save persona file location

---

**Step 6**

```gherkin
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
```

**Explanation**

- get packet template = build template from persona
- packet type = flow type key
- NEW = new enrollment template
- persona file path = input persona source
- store result in packet template path = save template location

---

**Step 7**

```gherkin
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
```

**Explanation**

- generate and upload packet skipping prereg = build and submit packet directly
- store result in registration ID = save RID for later checks

---

**Step 8**

```gherkin
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
```

**Explanation**

- check status = packet lifecycle assertion
- PROCESSED = expected success status
- registration ID = RID to query

---

**Step 9**

```gherkin
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
```

**Explanation**

- get uin by rid = fetch UIN using RID
- source registration ID = RID input
- store result in UIN = persist UIN for next steps

---

**Step 10**

```gherkin
And I get email by uin where resident UIN is the saved UIN and store result in email
```

**Explanation**

- get email by uin = fetch email linked to UIN
- store result in email = save expected recipient

---

**Step 11**

```gherkin
And I verify notification where notification type is UIN Generated, and email is the saved email
```

**Explanation**

- verify notification = confirm notification event
- notification type = event category
- UIN Generated = expected event name
- email = expected recipient

---

**Step 12**

```gherkin
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
```

**Explanation**

- check ridstage = stage-level assertion
- RID stage PRINT_SERVICE = print pipeline stage
- stage status PROCESSED = expected stage outcome

---

**Step 13**

```gherkin
Then I check ridstage where registration ID is the saved registration ID, and RID stage is BIOGRAPHIC_VERIFICATION, and stage status is SUCCESS
```

**Explanation**

- BIOGRAPHIC_VERIFICATION = biographic verification stage
- SUCCESS = expected completion status

---

**Step 14**

```gherkin
Then I check tags where registration ID is the saved registration ID
```

**Explanation**

- check tags = verify expected tags attached to RID

---

**Step 15**

```gherkin
And I delete packet data
```

**Explanation**

- Cleanup generated packet/persona/temp artifacts

---

**Data Flow Summary**

| Step    | Output Variable        |
|---------|--------------------------|
| Step 2  | environment 1 details    |
| Step 5  | persona file path         |
| Step 6  | packet template path      |
| Step 7  | registration ID            |
| Step 9  | UIN                         |
| Step 10 | email                        |

Steps 11-14 use these saved values for assertions.
