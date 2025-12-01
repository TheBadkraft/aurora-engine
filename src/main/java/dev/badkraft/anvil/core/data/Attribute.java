/// src/main/java/dev/badkraft/anvil/core/data/Attribute.java
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
import java.util.Objects;
import java.util.Optional;

/**
 * A single attribute attached to a statement.
 * <ul>
 *   <li>Tag form:   @[code debug}</li>
 *   <li>K-V form:   @{code type=block}</li>
 * </ul>
 * The content, when present, is **not** a nested construct – only a literal (string, number, boolean, null).
 */
public record Attribute(@NotNull String key, Value value) {
    public Attribute(String key) { this(key, null); }

    @Override
    public @NotNull String toString() {
        return value == null ? key : key + "=" + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attribute a)) return false;
        return key.equals(a.key) && Objects.equals(value, a.value);
    }

    @Override
    public int hashCode() { return Objects.hash(key, value); }

    /**
     * Typed accessor returning an Optional.
     * Usage: `enchantable.getValueAs(Boolean.class).orElse(false)`
     */
    @SuppressWarnings("unchecked")
    private <T> Optional<T> getValueAs(Class<T> clazz) {
        if (clazz == Boolean.class && value instanceof Value.BooleanValue) {
            return Optional.of(clazz.cast(Boolean.valueOf(((Value.BooleanValue) value).value())));
        }
        if (clazz == String.class && value instanceof Value.StringValue) {
            return Optional.of(clazz.cast(((Value.StringValue) value).content()));
        }
        /* Numeric coercion for LongValue and DoubleValue.
         * - LongValue: allow downcasts to Integer/Short/Byte with range checks, and widen to Float/Double.
         * - DoubleValue: allow downcasts to Float (range check) and integral casts to Long/Integer/Short/Byte
         *   only when the double is a whole number and within target range.
         */
        if (value instanceof Value.LongValue) {
            long lv = ((Value.LongValue) value).value();
            if (clazz == Long.class) return Optional.of(clazz.cast(Long.valueOf(lv)));
            if (clazz == Integer.class) {
                if (lv < Integer.MIN_VALUE || lv > Integer.MAX_VALUE)
                    throw new ClassCastException("Long value out of Integer range");
                return Optional.of(clazz.cast(Integer.valueOf((int) lv)));
            }
            if (clazz == Short.class) {
                if (lv < Short.MIN_VALUE || lv > Short.MAX_VALUE)
                    throw new ClassCastException("Long value out of Short range");
                return Optional.of(clazz.cast(Short.valueOf((short) lv)));
            }
            if (clazz == Byte.class) {
                if (lv < Byte.MIN_VALUE || lv > Byte.MAX_VALUE)
                    throw new ClassCastException("Long value out of Byte range");
                return Optional.of(clazz.cast(Byte.valueOf((byte) lv)));
            }
            if (clazz == Float.class) {
                return Optional.of(clazz.cast(Float.valueOf((float) lv)));
            }
            if (clazz == Double.class) {
                return Optional.of(clazz.cast(Double.valueOf((double) lv)));
            }
        }

        if (value instanceof Value.DoubleValue) {
            double dv = ((Value.DoubleValue) value).value();
            if (clazz == Double.class) return Optional.of(clazz.cast(Double.valueOf(dv)));

            if (clazz == Float.class) {
                if (Double.isNaN(dv) || Double.isInfinite(dv) ||
                        dv < -Float.MAX_VALUE || dv > Float.MAX_VALUE)
                    throw new ClassCastException("Double value out of Float range or not finite");
                return Optional.of(clazz.cast(Float.valueOf((float) dv)));
            }

            // Integral conversions from double: only allow if dv is an exact integer
            if (Double.isFinite(dv) && dv == Math.rint(dv)) {
                long asLong = (long) dv;
                if (clazz == Long.class) {
                    return Optional.of(clazz.cast(Long.valueOf(asLong)));
                }
                if (clazz == Integer.class) {
                    if (asLong < Integer.MIN_VALUE || asLong > Integer.MAX_VALUE)
                        throw new ClassCastException("Double value out of Integer range");
                    return Optional.of(clazz.cast(Integer.valueOf((int) asLong)));
                }
                if (clazz == Short.class) {
                    if (asLong < Short.MIN_VALUE || asLong > Short.MAX_VALUE)
                        throw new ClassCastException("Double value out of Short range");
                    return Optional.of(clazz.cast(Short.valueOf((short) asLong)));
                }
                if (clazz == Byte.class) {
                    if (asLong < Byte.MIN_VALUE || asLong > Byte.MAX_VALUE)
                        throw new ClassCastException("Double value out of Byte range");
                    return Optional.of(clazz.cast(Byte.valueOf((byte) asLong)));
                }
            } else {
                throw new ClassCastException("Double value is not an integral number");
            }
        }
        if (clazz.isAssignableFrom(value.getClass())) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }

    /**
     * Convenience boolean getter for tests: `assertTrue(enchantable.getBoolean())`
     */
    public Boolean getBoolean() {
        return getValueAs(Boolean.class).orElseThrow();
    }

    public String getString() {
        return getValueAs(String.class).orElseThrow();
    }

    public Integer getInt() {
        return getValueAs(Integer.class).orElseThrow();
    }

    public Long getLong() {
        return getValueAs(Long.class).orElseThrow();
    }

    public Float getFloat() {
        return getValueAs(Float.class).orElseThrow();
    }

    public Double getDouble() {
        return getValueAs(Double.class).orElseThrow();
    }
}