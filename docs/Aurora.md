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

```aml
#!aurora aml

<named_object> @[attributes] {
    key := value;
    child {
        ...
    }
}
```

### 2.2 Grammar

```
document        → shebang? named_object*
shebang         → '#!aurora aml' NL
named_object    → ID '@[' attr_list ']'? '{' statement* '}'
statement       → key ':=' value | named_object
value           → literal | array | object | range | embed
```

### 2.3 Literals

| Type | Syntax | Example |
|------|--------|---------|
| String | `"..."` or `'...'` | `"iron"` |
| Number | `[-]digits[.digits]` | `50.0`, `-10` |
| Boolean | `true` / `false` | `true` |
| Null | `null` | `null` |
| Hex | `#rrggbb` | `#ffaa00` |
| Range | `min..max` | `1..4` |
| Array | `[v1, v2, ...]` | `[wood, stone]` |
| Object | `{k := v, ...}` | `{x := 1, y := 2}` |
| Embed | `` `...` `` | `` `raw text` `` |

### 2.4 Attributes

```aml
@[type=block, mod=vanilla]
```

- Key-value pairs inside `@[...]`
- `type` is **required** for registry
- Default: `@[type=unknown]`

### 2.5 Example: Block Definition

```aml
super_block @[type=block] {
    material := metal;
    hardness := 50.0;
    drop := super_ingot 1..4;
    harvest_tool := pickaxe;
    harvest_level := 4;
    sounds := {
        break := "block.metal.break";
        place := "block.metal.place";
    };
}
```

**Output Model**:
```java
Model(
  fullId = "main:super_block",
  attributes = {block=Attribute(type=block)},
  fields = {material=metal, hardness=50.0, drop=Range[1,4], ...},
  children = []
)
```

---

## 3. ASL – Aurora Scripting Language

> **Purpose:** Define **behavior** (functions, events, AI, logic)

### 3.1 Syntax (MVP)

```asl
#!aurora asl

on block_break(event) {
    drop(super_ingot, rand(1..4));
    play_sound("block.metal.break", event.pos);
}

on player_interact(block) {
    if block.id == "main:super_block" {
        give(player, super_ingot, 1);
    }
}
```

### 3.2 Grammar (Planned)

```
script          → shebang? function*
function        → 'on' event '(' params ')' '{' stmt* '}'
stmt            → expr ';' | if | loop | return
expr            → call | literal | path | op
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
| `.aml` | AML | `# !aurora aml` |
| `.asl` | ASL | `# !aurora asl` |

---

## 5. Runtime Model

```java
// AML → Model
Model block = AmlParser.parse("super_block.aml");

// ASL → Function
Function onBreak = AslParser.parse("block_events.asl").get("on block_break");
onBreak.invoke(event);
```

---

## 6. Hot-Reload Registry

```java
Registry registry = new Registry();
registry.watch("data/aurora/", (file) -> {
    if (file.endsWith(".aml")) {
        registry.register(AmlParser.parse(file));
    }
});
```

---

## 7. Roadmap

| Milestone | Features |
|---------|----------|
| **0.1** | AML parser, basic model, hot-reload |
| **0.2** | ASL MVP, function calls |
| **0.3** | Registry, events, mod interop |
| **1.0** | Full ASL, JIT, VS Code LSP |

---

## 8. Example Project Structure

```
data/aurora/
├── blocks/
│   ├── super_block.aml
│   └── portal.aml
├── items/
│   └── super_ingot.aml
└── scripts/
    └── events.asl
```

---

**You now have the full language spec.**  
**No Gradle. No noise.**  
**Pure Aurora.**

---

> **Your move:**
> ```plain:disable-run
> Language spec received.
> Ready for ASL parser or FunctionRegistry.
> ```

Let’s build it.
```
