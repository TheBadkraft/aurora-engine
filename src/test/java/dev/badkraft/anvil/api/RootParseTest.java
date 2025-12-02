// src/test/java/dev/badkraft/anvil/api/RootParseTest.java
package dev.badkraft.anvil.api;

import dev.badkraft.anvil.data.array;
import dev.badkraft.anvil.data.blob;
import dev.badkraft.anvil.data.object;
import dev.badkraft.anvil.data.value;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class RootParseTest {

    private static final String MINIMAL_ANVL = """
        #!aml
        @[deprecated]
        
        // simple port node
        port := 25565
        """;

    private static final String WITH_NODE_ATTR = """
        #!aml
        @[secure]
        
        server @[host, deprecated=true] := {
            ip   := "127.0.0.1"
            name := "lobby"
        }
        """;

    @Test
    void parsesRootWithPrimitiveAndRootAttribute() throws IOException {
        root r = Anvil.read(MINIMAL_ANVL).parse();

        // root-level attribute
        assertTrue(r.hasAttribute("deprecated"));
        assertNotNull(r.attribute("deprecated"));
        // this is a **domain** consumer function once we've verified it exists:
        // assertTrue(r.attribute("deprecated").asBoolean());

        // top-level node lookup
        node portNode = r.node("port");
        assertNotNull(portNode);
        assertEquals("port", portNode.identifier());

        value value = portNode.value();
        assertNotNull(value);
        assertEquals(25565L, value.asLong());

        // shortcut payload access
        value portVal = r.get("port");
        assertNotNull(portVal);
        assertEquals(25565L, portVal.asLong());
    }

    @Test
    void parsesNodeWithAttributes() throws IOException {
        root r = Anvil.read(WITH_NODE_ATTR).parse();

        assertTrue(r.hasAttribute("secure"));

        node server = r.node("server");
        assertEquals("server", server.identifier());

        // node-level attributes
        assertTrue(server.hasAttribute("host"));
        assertTrue(server.hasAttribute("deprecated"));
        assertTrue(server.attribute("deprecated").asBoolean());
    }

    @Test
    void parseNodeWithObjectValue() throws IOException {
        root r = Anvil.read(WITH_NODE_ATTR).parse();
        node server = r.node("server");
        value serverVal = server.value();

        assertNotNull(serverVal);

        // payload is an object
        object payload = server.value().asObject();
        assertNotNull(payload.get("ip"));
        assertEquals("127.0.0.1", payload.get("ip").asString());

        // shortcut access via root
        String name = Objects.requireNonNull(r.node("server").get("name")).asString();
        assertEquals("lobby", name);
    }

    @Test
    void throwsOnMissingNode() throws IOException {
        root r = Anvil.read(MINIMAL_ANVL).parse();
        assertThrows(NoSuchElementException.class, () -> r.node("missing"));
    }

    @Test
    void throwsOnWrongType() throws IOException {
        root r = Anvil.read(MINIMAL_ANVL).parse();
        assertThrows(ClassCastException.class, () -> r.get("port").asString());
    }

    @Test
    void parsesArrayWithMixedTypesAndIndexAccess() throws IOException {
        root r = Anvil.read("""
        #!aml
        @[experimental]

        colors @[ui] := [#ff0000, #00ff00, #0000ff]
        scores       := [42, 99, 17, 1337]
        mixed        := [true, 123, "hello", false]
        // empty     := [] -- invalid
        """).parse();

        // Direct index access via node.get(int)
        assertEquals(16711680L,   r.node("colors").get(0).asLong());
        assertEquals(255L,  r.node("colors").get(2).asLong());

        assertEquals(42L,     r.node("scores").get(0).asLong());
        assertEquals(1337L,   r.node("scores").get(3).asLong());

        assertTrue(r.node("mixed").get(0).asBoolean());
        assertEquals(123L,    r.node("mixed").get(1).asLong());
        assertEquals("hello", r.node("mixed").get(2).asString());
        assertFalse(r.node("mixed").get(3).asBoolean());

        // Attributes
        assertTrue(r.node("colors").hasAttribute("ui"));
        assertTrue(r.hasAttribute("experimental"));

        // Bounds checking
        assertThrows(IndexOutOfBoundsException.class, () -> r.node("colors").get(99));
    }

    @Test
    void parseTupleWithIndexAccess() throws IOException {
        root r = Anvil.read("""
        #!aml position := (10, 64, 10)
        """).parse();

        assertEquals(10L, r.node("position").get(0).asLong());
        assertEquals(64L, r.node("position").get(1).asLong());
        assertEquals(10L, r.node("position").get(2).asLong());

        // Bounds checking
        assertThrows(IndexOutOfBoundsException.class, () -> r.node("position").get(3));
    }

    @Test
    void fullAnvilApiShowcase() throws IOException {
        root r = Anvil.read("""
        #!aml
        @[version=2, experimental]

        server @[core] := {
            name := "Anvil Survival"
            port := 25565
            motd := "Forged in fire."
        }

        world @[seed=1337] := {
            spawn @[respawn] := (0, 64, 0)
            rules @[hardcore] := [
                "pvp", "keepInventory=false", "naturalRegeneration=true"
            ]
        }

        motd @[pinned] := {
            readme := @md`*** # Anvil Server Forged in fire. Built to last.***`
        }

        player := ("Notch", 100, true)
        """).parse();

        // Object access
        assertEquals("Anvil Survival", r.node("server").get("name").asString());
        assertEquals(25565L, r.node("server").get("port").asLong());

        // Nested tuple + array (non-empty!)
        assertEquals(64L, r.node("world").get("spawn").get(1).asLong());
        assertEquals("pvp", r.node("world").get("rules").get(0).asString());
        int arrSize = Objects.requireNonNull(r.node("world").get("rules")).asArray().size();
        assertEquals(3, arrSize);

        // Blob with proper @md syntax
        blob readme = Objects.requireNonNull(r.node("motd").get("readme")).asBlob();
        assertTrue(readme.content().contains("Forged in fire"));
        assertTrue(readme.content().contains("Built to last"));
        assertEquals("md", readme.tag().asString());

        // Tuple with node-level attribute
        assertEquals("Notch", r.node("player").get(0).asString());
        assertEquals(100L, r.node("player").get(1).asLong());
        assertTrue(r.node("player").get(2).asBoolean());

        // Root and nested attributes
        assertTrue(r.hasAttribute("experimental"));
        assertTrue(r.node("world").hasAttribute("seed"));
        assertEquals(1337L, r.node("world").attribute("seed").asLong());
        assertTrue(r.node("motd").hasAttribute("pinned"));
    }

    @Test
    void fromString_objectWithInheritance() throws IOException {
        root r = Anvil.read("""
        #!aml
        @[version=1]

        stone_block := {
            hardness := 1.5
            blast_resistance := 6.0
            sound := "stone"
        }

        diamond_block : stone_block := {
            hardness := 5.0
            light_level := 1
            sound := "metal"
        }

        iron_block : stone_block := {
            hardness := 5.0
            metal := true
        }
        """).parse();

        // Raw access — no resolution
        object diamond = r.node("diamond_block").value().asObject();
        assertTrue(diamond.hasBase());
        assertEquals("stone_block", diamond.base());

        object stone = r.node("stone_block").value().asObject();
        assertFalse(stone.hasBase());

        // Resolver-powered convenience
        object diamondBase = r.resolveBase(diamond.base());
        assertSame(stone, diamondBase);  // same instance
        assertEquals(6.0, diamondBase.get("blast_resistance").asDouble());
        assertEquals("stone", diamondBase.get("sound").asString());

        // Derived wins where present
        assertEquals(5.0, diamond.get("hardness").asDouble());
        assertEquals("metal", diamond.get("sound").asString());
        assertEquals(1, diamond.get("light_level").asInt());

        // Another derived block — shares same base
        object iron = r.node("iron_block").value().asObject();
        object ironBase = r.resolveBase(iron.base());
        assertSame(stone, ironBase);

        // Forward reference works
        root forward = Anvil.read("""
        obsidian : bedrock := { toughness := 50 }
        bedrock := { hardness := 100 }
        """).parse();

        object obsidian = forward.node("obsidian").value().asObject();
        assertTrue(obsidian.hasBase());
        assertEquals("bedrock", obsidian.base());
        object bedrock = forward.resolveBase("bedrock");
        assertEquals(100.0, bedrock.get("hardness").asDouble());
    }
}