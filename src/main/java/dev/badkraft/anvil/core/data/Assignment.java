/// src/main/java/dev/badkraft/anvil/core/data/Assignment.java
///
/// Copyright (c) 2025 Quantum Override. All rights reserved.
/// Author: The Badkraft
/// Date: November 14, 2025
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
package dev.badkraft.anvil.core.data;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/*
    An assignment statement, e.g., key := content
 */
public record Assignment(String key, List<Attribute> attributes, Value value, String base) implements Statement {
    public Assignment(String key, List<Attribute> attributes, Value value) {
        this(key, List.copyOf(attributes), value, null);
    }

    public String identifier() { return key; }

    @Override
    public List<Attribute> attributes() {
        return attributes;
    }

    @Override
    public @NotNull String toString() {
        String attrs = attributes.isEmpty() ? "" :
            " @[" +
            attributes.stream().map(Attribute::toString).collect(Collectors.joining(", ")) +
            "]";

        return key + attrs + " := " + value;
    }
}
