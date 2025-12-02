/// src/main/java/dev/badkraft/anvil/utilities/null.java
///
/// Copyright (c) 2025 Quantum Override. All rights reserved.
/// Author: The Badkraft
/// Date: 12 02, 2025
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
package dev.badkraft.anvil.utilities;

import dev.badkraft.anvil.api.*;
import dev.badkraft.anvil.data.object;

import java.util.NoSuchElementException;

public class Resolver implements IResolver {
    private final root root;

    private Resolver(root root) {
        this.root = root;
        root.setResolver(this);
    }

    public static IResolver of(root r) {
        return new Resolver(r);
    }

    @Override
    public node node(String id) {
        node n = root.node(id);
        if (n == null) throw new NoSuchElementException("No node: " + id);
        return n;
    }

    @Override
    public object resolveBase(String baseId) {
        return node(baseId).value().asObject();
    }
}
