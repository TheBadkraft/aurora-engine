package dev.badkraft.anvil.api;

import dev.badkraft.anvil.core.data.Attribute;
import dev.badkraft.anvil.core.data.Statement;
import dev.badkraft.anvil.core.data.Value;

import java.util.List;
import java.util.NoSuchElementException;

public record AnvilRoot(
        Value.Attributes rootAttributes,
        List<Statement> statements,
        String namespace
) {


    public Value.Attributes getAttributes() {
        return  rootAttributes;
    }

    Value valueFor(String key) {
        return statements.stream()
                .filter(s -> s.identifier().equals(key))
                .map(Statement::value)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No statement named: " + key));
    }
    private boolean hasValueFor(String key) {
        return statements.stream()
                .anyMatch(s -> s.identifier().equals(key));
    }

    public Attribute getAttribute(String key) {
        return rootAttributes.find(key).orElse(null);
    }
    public boolean hasAttribute(String key) {
        return rootAttributes.has(key);
    }
    public boolean hasObject(String key) {
        if (!hasValueFor(key)) return false;
        return valueFor(key) instanceof Value.ObjectValue;
    }
}
