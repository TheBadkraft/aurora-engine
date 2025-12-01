/// src/main/java/dev/badkraft/anvil/core/api/ValueFactory.java
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
package dev.badkraft.anvil.core.api;

import dev.badkraft.anvil.core.data.*;

import java.util.List;
import java.util.Map;

public record ValueFactory(Source source) {

    public Value string(int start, int end) {
        return new Value.StringValue(source, start, end);
    }
    public Value blob(String attribute, int start, int end) {
        return new Value.BlobValue(source, attribute, start, end);
    }
    public Value booleanVal(boolean b, int start, int end) {
        return new Value.BooleanValue(source, b, start, end);
    }
    public Value nullVal(int start, int end) {
        return new Value.NullValue(source, start, end);
    }
    public Value longValue(long value, int start, int end) {
        return new Value.LongValue(source, value, start, end);
    }
    public Value doubleValue(double value, int start, int end) {
        return new Value.DoubleValue(source, value, start, end);
    }
    public Value hexValue(long value, int start, int end) {
        return new Value.HexValue(source, value, start, end);
    }
    public Value bare(int start, int end) {
        return new Value.BareLiteral(source, start, end);
    }
    public Value array(List<Value> elements, List<Attribute> attributes, int start, int end) {
        return new Value.ArrayValue(source, elements, attributes, start, end);
    }
    public Value tuple(List<Value> elements, List<Attribute> attributes, int start, int end) {
        return new Value.TupleValue(source, elements, attributes, start, end);
    }
    public Value object(List<Map.Entry<String, Value>> fields, List<Attribute> attributes, int start, int end) {
        return new Value.ObjectValue(source, fields, attributes, start, end);
    }
}