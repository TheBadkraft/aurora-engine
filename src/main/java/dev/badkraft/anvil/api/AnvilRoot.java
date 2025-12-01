/// src/main/java/dev/badkraft/anvil/api/AnvilRoot.java
///
/// Copyright (c) 2025 Quantum Override. All rights reserved.
/// Author: The Badkraft
/// Date: November 30, 2025
///
/// MIT License
/// Permission is hereby granted, free of charge, to any person obtaining a copy
/// of this software and associated documentation files (the "Software"), to deal
/// in the Software without restriction, including without limitation the rights
/// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
/// copies of the Software, and to permit persons to whom the Software is
/// furnished to do so, subject to the following conditions:
/// The above copyright notice and this permission notice shall be included in all
/// copies or substantial portions of the Software.
/// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
/// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
/// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
/// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
/// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
/// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
/// SOFTWARE.
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
