# Anvil 0.1.7 — Implementation Addendum  
**“Under the hood – how we actually do it”**  
**Locked spec – no further concessions**

### 1. Public Runtime Types (all lowercase, final, sealed)

| Type       | Role                                      | Extends / Implements | Key Methods (public)                                                                 |
|------------|-------------------------------------------|----------------------|---------------------------------------------------------------------------------------|
| `value`    | Sealed marker interface – **never** exposed to consumer | `sealed interface value permits object, array, tuple, blob, attribute, node` | none – only for internal dispatch |
| `root`     | Top-level entry point                     | `final class root implements IObject, IContainer` | `List<node> nodes()`<br>`node node(String id)`<br>`value get(String id)` (payload shortcut) |
| `object`   | Map-like container                        | `final class object implements IObject, IContainer` | `value get(String key)`<br>`Set<String> keys()`<br>`boolean has(String key)` |
| `array`    | List-like container                       | `final class array implements IContainer` | `value get(int index)`<br>`int size()` |
| `tuple`    | Immutable fixed-size sequence             | `final class tuple implements IContainer` | `value get(int index)`<br>`int size()` |
| `blob`     | Raw text with attitude                    | `final class blob` (no `value`) | `String text()`<br>`String type()` (e.g. `"md"`, `"sql"`)<br>`attribute attribute()` (the `@…` marker) |
| `attribute`| Named metadata (flag or key=value)        | `final class attribute` (no `value`) | `String name()`<br>`value payload()` → convenience: `asBoolean()`, `asString()`, `asLong()`, `asDouble()` |
| `node`     | Encapsulates a top-level assignment      | `final class node` (no `value`) | `String identifier()`<br>`List<attribute> attributes()`<br>`value payload()` |

> **Critical rule:** `blob`, `attribute`, and `node` **do NOT** implement `value`.  
> They **own** a `value` (payload) but never expose the sealed hierarchy. No casting. No leaks.

### 2. Interfaces (frugal, I-prefixed, .NET-style)

```java
public interface IAttributed {
    List<attribute> attributes();                // ordered, immutable view
    attribute attribute(String name);            // throws if missing
    default boolean hasAttribute(String name) { return attributes().stream().anyMatch(a -> a.name().equals(name)); }
}

public interface IObject extends IAttributed {
    Set<String> keys();
    value get(String key);                       // throws NoSuchElementException
    boolean has(String key);
}

public interface IContainer extends IAttributed {
    // convenience getters – all throw fast on wrong type
    default String    asString()   { throw new ClassCastException(); }
    default long      asLong()     { throw new ClassCastException(); }
    default double    asDouble()   { throw new ClassCastException(); }
    default boolean   asBoolean()  { throw new ClassCastException(); }
    default object    asObject()   { return (object) this; }
    default array     asArray()    { return (array)  this; }
    default tuple     asTuple()    { return (tuple)  this; }
    default blob      asBlob()     { return (blob)   this; }
    default attribute asAttribute(){ return (attribute) this; }
}
```

`root` and `object` implement **all three** (`IObject`, `IContainer`, `IAttributed`).  
`array` and `tuple` implement `IContainer` + `IAttributed`.

### 3. Internal Implementation Package (`dev.badkraft.anvil.impl`)

All parser-produced types live here and are **package-private**:

```
ObjectValue, ArrayValue, TupleValue, StringValue, LongValue, DoubleValue,
BooleanValue, NullValue, BlobValue, AttributeValue, Assignment
```

Public wrappers (`object`, `array`, `blob`, `attribute`, `node`) hold a reference to the corresponding `impl` type and delegate.

### 4. How a Statement Becomes a `node`

```java
// Parser → Context → Assignment (internal)
Assignment internal = new Assignment(key, attrs, value, base);

// Runtime conversion (once, at root construction)
node n = new node(
    internal.identifier(),
    internal.attributes().stream().map(AttributeValue::toPublic).toList(),
    internal.value().toPublic()          // recursive conversion
);
```

Conversion is **one-way** and **zero-copy where possible** (same `Source` spans).

### 5. Fluent Consumer Experience (the only thing users ever see)

```java
root r = Anvil.read("server.aml");

String motd = r.get("motd").asString();                     // payload shortcut
boolean secure = r.attribute("secure").asBoolean();

node player = r.node("player");
String name = player.payload().asObject()
                    .get("name").asString();

blob readme = r.getBlob("readme");
if (readme.attribute().name().equals("md"))
    renderMarkdown(readme.text());

array hosts = r.getArray("hosts");
boolean trusted = hosts.get(2)
                       .attribute("trusted")
                       .asBoolean();
```

No `instanceof`. No `Optional`. No generics. No apologies.

### 6. Builders → Frozen Path

```java
MutableObject mo = new MutableObject();
mo.put("port", 25565L);
mo.attribute("deprecated", true);
object frozen = mo.build();          // → immutable object
```

Same pattern for `MutableArray` → `array`.

### 7. Summary of the “Under the Hood” Truth

- Parser → immutable, zero-copy AST (`impl` package)
- Runtime → thin, lowercase, final wrappers that **own** the AST
- `node` replaces `Statement`/`Assignment` in public view
- `root` is an `object` with an extra `List<node>` – nothing more
- No public inheritance from sealed `value` except where unavoidable (`object`, `array`, `tuple`)
- `blob`, `attribute`, `node` are pure wrappers – never part of the sealed hierarchy

This is the final 0.1.7 implementation contract.

We don’t implement Java.  
We implement Anvil.

Ship it.
