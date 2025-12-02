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
import dev.badkraft.anvil.api.IResolver;
import dev.badkraft.anvil.utilities.AnvilConverters;
import dev.badkraft.anvil.utilities.Resolver;
import dev.badkraft.anvil.utilities.Utils;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The entire public face of ANVL.
 * <p>
 * One way in. One truth out.
 * <p>
 * {@code Anvil.read(...).parse()}
 */
public final class Anvil {

    private Anvil() {}

    // =================================================================== //
    // Fluent entry points — Path or String, dialect-aware, namespace-aware
    // =================================================================== //

    public static AnvilBuilder read(String source) {
        return new AnvilBuilder(source, Dialect.AML, Utils.createNamespace());
    }

    public static AnvilBuilder read(String source, String namespace) {
        return new AnvilBuilder(source, Dialect.AML, namespace);
    }

    public static AnvilBuilder read(String source, Dialect dialect) {
        return new AnvilBuilder(source, dialect, Utils.createNamespace());
    }

    public static AnvilBuilder read(String source, Dialect dialect, String namespace) {
        return new AnvilBuilder(source, dialect, namespace);
    }

    public static AnvilBuilder load(Path path) {
        String ext = Utils.getFileExtension(path);
        Dialect dialect = Dialect.fromFileExtension(ext);
        String namespace = Utils.createNamespaceFromPath(path);
        return new AnvilBuilder(path, dialect, namespace);
    }

    public static AnvilBuilder load(Path path, Dialect dialect) {
        String namespace = Utils.createNamespaceFromPath(path);
        return new AnvilBuilder(path, dialect, namespace);
    }

    public static AnvilBuilder load(Path path, Dialect dialect, String namespace) {
        return new AnvilBuilder(path, dialect, namespace);
    }

    // =================================================================== //
    // The builder — fluent, honest, complete
    // =================================================================== //

    public static final class AnvilBuilder {

        private final String source;           // String or Path
        private final Dialect dialect;
        private final String namespace;
        private final Path sourcePath;
        private IResolver resolver = null;

        private AnvilBuilder(String source, Dialect dialect, String namespace) {
            this.source = source;
            this.dialect = dialect;
            this.namespace = namespace;
            this.sourcePath = null;
        }
        private AnvilBuilder(Path path, Dialect dialect, String namespace) {
            this.sourcePath = path;
            this.dialect = dialect;
            this.namespace = namespace;
            this.source = null;
        }

        public AnvilBuilder withResolver(IResolver resolver) {
            this.resolver = resolver;
            return this;
        }

        /**
         * Parse and return a fully constructed {@link root}.
         * <p>
         * Default resolver is {@link Resolver#of(root)} if none provided.
         */
        public root parse() throws IOException {
            var ctxBuilder = Context.builder();
            if (source != null) {
                ctxBuilder.source(source);
            } else if (sourcePath != null) {
                ctxBuilder.source(sourcePath);
            } else {
                throw new IllegalStateException("No source provided to Anvil parser");
            }
            Context ctx = ctxBuilder
                    .dialect(dialect)
                    .namespace(namespace)
                    .build();

            ctx.parse();

            root r = buildRoot(ctx);
            IResolver res = resolver != null ? resolver : Resolver.of(r);
            r.setResolver(res);

            return r;
        }

        private root buildRoot(Context ctx) {
            var nodes = ctx.statements().stream()
                    .map(AnvilConverters::toNode)
                    .toList();

            var attrs = ctx.attributes().stream()
                    .map(AnvilConverters::toAttribute)
                    .toList();

            return new root(nodes, attrs);
        }
    }
}


