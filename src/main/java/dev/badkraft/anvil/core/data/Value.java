/// src/main/java/dev/badkraft/anvil/core/data/Value.java
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

import java.util.*;
import java.util.stream.Stream;

public sealed interface Value
        permits Value.ArrayValue, Value.BareLiteral, Value.BlobValue, Value.BooleanValue,
        Value.DoubleValue, Value.HexValue, Value.LongValue, Value.NullValue,
        Value.ObjectValue, Value.StringValue, Value.TupleValue {

    default Attributes getAttributes() {
        return switch (this) {
            case ArrayValue a -> new Attributes(a.attributes);
            case TupleValue t -> new Attributes(t.attributes);
            case ObjectValue o -> new Attributes(o.attributes);
            default -> throw new UnsupportedOperationException(
                    "Attributes not supported on " + getClass().getSimpleName());
        };
    }
    default String getBase() {
        if (this instanceof ObjectValue o) {
            return o.getBase();
        } else {
            throw new UnsupportedOperationException(
                    "Base not supported on " + getClass().getSimpleName());
        }
    }
    default Attribute findAttribute(String key) {
        return getAttributes().find(key).orElse(null);
    }

    int start();
    int end();

    final class Attributes implements Iterable<Attribute> {
        private final List<Attribute> backing;
        public Attributes(List<Attribute> backing) {
            this.backing = Objects.requireNonNullElse(backing, new ArrayList<>());
        }
        public void add(Attribute attribute) { backing.add(attribute); }
        public void addAll(List<Attribute> attributes) { backing.addAll(attributes); }
        public boolean isEmpty() { return backing.isEmpty(); }
        public int size() { return backing.size(); }
        public Attribute get(int index) { return backing.get(index); }
        public boolean has(String key) { return backing.stream().anyMatch(a -> a.key().equals(key)); }
        public boolean hasTag(String key) { return backing.stream().anyMatch(a -> a.key().equals(key) && a.value() == null); }
        public Optional<Attribute> find(String key) { return backing.stream().filter(a -> a.key().equals(key)).findFirst(); }
        public Stream<Attribute> stream() { return backing.stream(); }

        // === Iterable METHODS ===
        @Override
        public @NotNull Iterator<Attribute> iterator() { return backing.iterator(); }

        public List<Attribute> asList() {
            return backing;
        }
    }

    // === PRIMITIVES ===
    record NullValue(ValueBase valueBase) implements Value {
        public NullValue(Source source, int start, int end) {
            this(new ValueBase(source, start, end));
        }
        @Override public @NotNull String toString() { return "null"; }
        @Override public int start() { return valueBase.start; }
        @Override public int end() { return valueBase.end; }
    }

    record BooleanValue(boolean value, ValueBase valueBase) implements Value {
        public BooleanValue(Source source, boolean value, int start, int end) {
            this(value, new ValueBase(source, start, end));
        }
        @Override public @NotNull String toString() { return valueBase.source(); }
        @Override public int start() { return valueBase.start; }
        @Override public int end() { return valueBase.end; }
    }

    record LongValue(long value, ValueBase valueBase) implements Value {
        public LongValue(Source source, long value, int start, int end) {
            this(value, new ValueBase(source, start, end));
        }
        @Override public @NotNull String toString() { return valueBase.source(); }
        @Override public int start() { return valueBase.start; }
        @Override public int end() { return valueBase.end; }
    }

    record DoubleValue(double value, ValueBase valueBase) implements Value {
        public DoubleValue(Source source, double value, int start, int end) {
            this(value, new ValueBase(source, start, end));
        }
        @Override public @NotNull String toString() { return valueBase.source(); }
        @Override public int start() { return valueBase.start; }
        @Override public int end() { return valueBase.end; }
    }

    record HexValue(long value, ValueBase valueBase) implements Value {
        public HexValue(Source source, long value, int start, int end) {
            this(value, new ValueBase(source, start, end));
        }
        @Override public @NotNull String toString() { return valueBase.source(); }
        @Override public int start() { return valueBase.start; }
        @Override public int end() { return valueBase.end; }
    }

    record StringValue(ValueBase valueBase) implements Value {
        public StringValue(Source source, int start, int end) {
            this(new ValueBase(source, start, end));
        }
        public String content() { return valueBase.substring();}
        @Override public @NotNull String toString() { return valueBase.source(); }
        @Override public int start() { return valueBase.start; }
        @Override public int end() { return valueBase.end; }
    }

    record BareLiteral(ValueBase valueBase) implements Value {
        public BareLiteral(Source source, int start, int end) {
            this(new ValueBase(source, start, end));
        }
        public String value() { return valueBase.substring();}
        @Override public @NotNull String toString() { return valueBase.source(); }
        @Override public int start() { return valueBase.start; }
        @Override public int end() { return valueBase.end; }
    }

    record BlobValue(String attribute, ValueBase valueBase) implements Value {
        public BlobValue(Source source, String attribute, int start, int end) {
            this(attribute, new ValueBase(source, start, end));
        }
        public String attribute() { return attribute; }
        public String content() { return valueBase.substring();}
        @Override public @NotNull String toString() { return (attribute != null ? "@" + attribute : "@") + content(); }
        @Override public int start() { return valueBase.start; }
        @Override public int end() { return valueBase.end; }
    }

    // === COMPOSITES ===
    record ArrayValue(List<Value> elements, List<Attribute> attributes, ValueBase valueBase) implements Value {
        public ArrayValue(Source source, List<Value> elements, List<Attribute> attributes, int start, int end) {
            this(List.copyOf(elements), new ArrayList<>(attributes != null ? attributes : List.of()), new ValueBase(source, start, end));
        }
        @Override public @NotNull String toString() { return valueBase.source(); }
        @Override public int start() { return valueBase.start; }
        @Override public int end() { return valueBase.end; }
    }

    record TupleValue(List<Value> elements, List<Attribute> attributes, ValueBase valueBase) implements Value {
        public TupleValue(Source source, List<Value> elements, List<Attribute> attributes, int start, int end) {
            this(List.copyOf(elements), new ArrayList<>(attributes != null ? attributes : List.of()), new ValueBase(source, start, end));
            if (elements.size() < 2) throw new IllegalArgumentException("Tuple must have at least 2 elements");
        }
        @Override public @NotNull String toString() { return valueBase.source(); }
        @Override public int start() { return valueBase.start; }
        @Override public int end() { return valueBase.end; }
    }

    record ObjectValue(List<Map.Entry<String, Value>> fields, List<Attribute> attributes, ValueBase valueBase, String base) implements Value {
        public ObjectValue(Source source, List<Map.Entry<String, Value>> fields, List<Attribute> attributes, String base, int start, int end) {
            this(List.copyOf(fields), new ArrayList<>(attributes != null ? attributes : List.of()), new ValueBase(source, start, end), base);
        }
        @Override
        public @NotNull String toString() { return valueBase.source(); }
        @Override
        public int start() { return valueBase.start; }
        @Override
        public int end() { return valueBase.end; }
        @Override
        public String getBase() { return base; }

        public Map<String, Value> asMap() {
            LinkedHashMap<String, Value> map = new LinkedHashMap<>();
            for (var e : fields) {
                if (map.put(e.getKey(), e.getValue()) != null) {
                    throw new IllegalStateException("Duplicate key: " + e.getKey());
                }
            }
            return Map.copyOf(map);
        }
    }
}