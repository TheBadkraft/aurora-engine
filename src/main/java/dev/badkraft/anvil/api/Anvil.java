/// src/main/java/dev/badkraft/anvil/api/Anvil.java
///
/// Copyright (c) 2025 Quantum Override. All rights reserved.
/// Author: The Badkraft
/// Date: November 28, 2025
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

import dev.badkraft.anvil.core.api.Context;
import dev.badkraft.anvil.core.data.Dialect;
import dev.badkraft.anvil.utilities.Utils;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The entire public API of Anvil lives here.
 * <p>
 * There is only one runtime type you ever see: {@code Anvil}.
 * Everything else is internal.
 */
public class Anvil {

    // =================================================================== //
    // Factory methods — this is the only way users get an Anvil instance //
    // =================================================================== //

    public static AnvilRoot load(Path path, Dialect dialect, String namespace) throws IOException {
        return init(path, dialect, namespace);
    }
    public static AnvilRoot load(Path path, Dialect dialect) throws IOException {
        return load(path, dialect, Utils.createNamespaceFromPath(path));
    }
    public static AnvilRoot load(Path path) throws IOException {
        return load(path, Dialect.fromFileExtension(Utils.getFileExtension(path)), Utils.createNamespaceFromPath(path));
    }
    public static AnvilRoot read(String source, String namespace) {
        return init(source, Dialect.AML, namespace);
    }
    public static AnvilRoot read(String source) {
        return read(source, Utils.createNamespace());
    }
    private static AnvilRoot init(String source, Dialect dialect, String namespace) {
        Context ctx = Context.builder()
                .source(source)
                .dialect(dialect)
                .namespace(namespace)
                .build();
        ctx.parse();
        return buildRoot(ctx);
    }
    private static AnvilRoot init(Path path, Dialect dialect, String namespace) throws IOException {
        Context ctx = Context.builder()
                .source(path)
                .dialect(dialect)
                .namespace(namespace)
                .build();
        ctx.parse();
        return buildRoot(ctx);
    }
    private static AnvilRoot buildRoot(Context ctx) {
        return new AnvilRoot(
                ctx.attributes(),
                ctx.statements(),
                ctx.namespace()
        );
    }

}


