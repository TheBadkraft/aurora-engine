# Aurora Language Specification (v0.1.0)

> **Aurora** — A next-gen language paradigm with **two dialects**:
> - **AML** – *Aurora Modelling Language* (declarative, data-first)
> - **ASL** – *Aurora Scripting Language* (imperative, logic-first)

---

## 1. Core Principles

| Principle | Description |
|---------|-------------|
| **Human-first syntax** | Readable by non-programmers |
| **Zero-boilerplate** | No classes, no types, no imports |
| **Dual dialects** | Model data with AML, script behavior with ASL |
| **Seamless interop** | AML objects → ASL functions |
| **Hot-reloadable** | Change files → instant update |
| **Extensible** | `@[type=...]` for custom semantics |

---

## 2. AML – Aurora Modelling Language

> **Purpose:** Define **immutable data models** (blocks, items, entities, recipes, etc.)

### 2.1 Syntax

```aurora
#!aml

<identifier> @[attributes] := value}
```

**Grammar**
```
document        → shebang? statement*
statement       → ID ':=' value (',' | NL)
shebang         → '#!aml' NL
object          → ID '@[' attr_list ']'? ':=' '{' statement* '}'
array           → ID '@[' attr_list ']'? ':=' '{' value* '}'
embed           → ID ':=' '@'ATTR`free form embedded \escaped`
value           → literal | array | object | embed
```

### 2.3 Literals

| Type    | Syntax                  | Example                   |
|---------|-------------------------|---------------------------|
| String  | `"..."` or `'...'`      | `"iron"`                  |
| Number  | `[-]digits[.digits]`    | `50.0`, `-10`             |
| Boolean | `true` / `false`        | `true`                    |
| Null    | `null`                  | `null`                    |
| Hex     | `#rrggbb`               | `#ffaa00`                 |
| Array   | `[v1, v2, ...]`         | `[wood, stone]`           |
| Object  | `{k := v, ...}`         | `{x := 1, y := 2}`        |
| Embed   | `` `...` ``             | `` @md`**raw text**` ``   |      

### 2.4 Attributes

```aml
@[type=block, mod=vanilla, method, debug=true]
```

Attributes are optional and valid only on **`object`** and **`array`**. Embedded text may have a single attribute (`` @attr`embedded` ``).


- Key-value pairs inside `@[...]`
- Single attribute tag preceding `embedded text`

The parser does not attach any function or value to attributes. It is simply metadata attached to the statement itself. The consuming client will be able to interrogate modules and discover all fields, values, and attached attributes.

### 2.5 Example: Block Definition

```aml
super_ore_block @[type=block] := {
    material := stone;
    hardness := 50.0;
    drop := super_ore;
    harvest_tool := pickaxe;
    harvest_level := 4;
    sounds := {
        break := "block.stone.break";
        place := "block.stone.place";
    };
}
```

**Output Model**:
```java
Model(
  fullId = "main:super_ore_block",
  attributes = {block=Attribute(type=block)},
  fields = {material=stone, hardness=50.0, drop=1, ...},
  children = []
)
```

---

## 3. ASL – Aurora Scripting Language

> **Purpose:** Define **behavior** (functions, events, AI, logic)

**ASL** here is incomplete. We will design on an as needed basis to ensure only what is needed will be implemented and, therefore, will not break existing features. If any of the following on **ASL** seems contradictory, incomplete, or broken, rest assured, this is just a white board for ideas, intent, and value.

### 3.1 Syntax (MVP)

```asl
#!aurora asl

block_break @[event] := (event) -> {
    drop(super_ingot, rand(1..4));
    play_sound("block.metal.break", event.pos);
}

player_interact @[event] := (block) -> {
    if block.id == "main:super_block" {
        give(player, super_ingot, 1);
    }
}
```

### 3.2 Grammar

```
script          → shebang? function*
function        → event @[attr]  '(' params ')' '{' statement* '}'
statement       → expr ';' | if | loop | return
expr            → call | literal | path | op | AML
```

### 3.3 Built-in Functions (MVP)

| Function | Signature | Description |
|--------|----------|-------------|
| `drop(item, count)` | `void` | Drop item at event pos |
| `give(player, item, count)` | `void` | Give to player |
| `play_sound(id, pos)` | `void` | Play sound |
| `rand(min..max)` | `int` | Random in range |
| `log(msg)` | `void` | Debug print |

---

## 4. File Conventions

| Extension | Dialect | Shebang |
|---------|--------|--------|
| `.aml` | AML | `# !aml` |
| `.asl` | ASL | `# !asl` |

**NOTE**: The shebang tells the parser (**AuroraEngine**) what dialect to expect. AML is the more restrictive dialect and a subset of ASL. While an _Aurora_ file can have any extension, if a shebang is not included, the parser will use the extension as a hint. If the file extension does not provide a valid hint, the dialect will be defaulted to ASL, the least restrictive.

---

## 5. Runtime Model
**Needs to be updated**

```java
// AML → Model
Model block = AmlParser.parse("super_block.aml");

// ASL → Function
Function onBreak = AslParser.parse("block_events.asl").get("on block_break");
onBreak.invoke(event);
```

---

## 6. Function Registry
**To be determined**

---

## 7. Roadmap

| Milestone | Features |
|---------|------------------|
| **0.1** | AML parser, basic model |
| **0.2** | Model attributes |
| **0.3** | `vars` & interpolation |
| **0.4** | Inheritance |
| **0.5** | ASL, type table |
| **0.6** | Module composition |
| **0.7** | Function Registry |
| **1.0** | Release Candidate |

