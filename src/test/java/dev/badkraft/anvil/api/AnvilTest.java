// dev.badkraft.anvil.api/AnvilTest.java
package dev.badkraft.anvil.api;

import dev.badkraft.anvil.core.data.Attribute;
import dev.badkraft.anvil.core.data.Value;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class AnvilTest {

    private static final String ROOT_ATTRIBUTES = """
        #!aml
        @[version="1.0.0", mc_version="1.21.10", debug=true]
        @[source="mojang-official-proguard"]

        name := "Test Module"
        description := "A context to test attributes"
        bonus_items @[rarity="legendary", count=3] := [
            minecraft:diamond_sword
            minecraft:enchanted_golden_apple
        ]
        """;

    private static final String SIMPLE_STATEMENTS = """
        #!aml
        name := "Badkraft"
        age := 42
        admin := true
        health := 20.0
        id := badkraft
        desc := @md`**legend**`
        """;

    private static final String NESTED_STRUCTURE = """
        #!aml
        player := {
            name := "Grok"
            pos := (10, 64, -300)
            inventory := [
                "diamond_sword"
                "netherite_chestplate"
                "elytra"
            ]
            metadata := {
                joined := "2025-11-30"
                playtime_hours := 1337
                verified := true
            }
        }
        """;

    private static final String TOP_LEVEL_ARRAY = """
        #!aml
        @[tags="core", priority=100]
        items := [
            "stick"
            "string"
            "feather"
        ]
        """;

    private static final String MIXED_TYPES_ARRAY = """
        #!aml
        mixed @[test=true] := [
            "hello"
            42
            true
            3.14
            badkraft
            @md`**bold**`
        ]
        """;

    @Test
    void loadFromFile_withRootAttributes() throws IOException {
        AnvilRoot root = Anvil.load(Paths.get("src/test/resources/anvil_modded_01.aml"));

        //  do we have a root?
        assertNotNull(root);

        //  let's get some root attributes
        //  @[mod="anvil_test", version="1.0.0", author="Generated", debug=true, size_target="7KB"]
        assertTrue(root.hasAttribute("mod"));
        assertTrue(root.hasAttribute("version"));
        Attribute modAttr = root.getAttribute("mod");
        assertNotNull(modAttr);
        assertEquals("anvil_test", modAttr.value().toString());
    }

    @Test
    void loadFromFile_withObjectAttributes() throws IOException {
        AnvilRoot root = Anvil.load(Paths.get("src/test/resources/anvil_modded_01.aml"));

        // let's touch a couple of object attributes
        assertTrue(root.hasObject("shadow_soul"));

        Value.ObjectValue item = (Value.ObjectValue) root.valueFor("shadow_soul");
        assertNotNull(item);
        Attribute enchantable = item.findAttribute("enchantable");
        Attribute durability = item.findAttribute("durability");
        assertNotNull(enchantable);
        assertNotNull(durability);
        assertTrue(enchantable.getBoolean());
        assertEquals(2643, durability.getInt());
    }
}