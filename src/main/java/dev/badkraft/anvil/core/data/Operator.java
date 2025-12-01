/// src/main/java/dev/badkraft/anvil/core/data/Operator.java
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

// We need to separate actual operators from symbols
public enum Operator {
    ASSIGN(":=", 2),
    EQUAL("=", 1),
    AT("@", 1),
    BACKTICK("`", 1), // token, length
    COLON(":", 1),
    COMMA(",", 1),
    DOT_DOT("..", 2),
    L_BRACE("{", 1),
    R_BRACE("}", 1),
    L_BRACKET("[", 1),
    R_BRACKET("]", 1),
    L_PAREN("(", 1),
    R_PAREN(")", 1),
    NEWLINE("\n", 2 ),
    QUOTE("\"", 1),
    S_QUOTE("'", 1),
    ROCKET("=>", 2),;

    private final String symbol;
    private final int length;

    Operator(String symbol, int length) {
        this.symbol = symbol;
        this.length = length;
    }

    public String symbol() {
        return symbol;
    }
    public int length() { return length; }

    public static Operator fromToken(String token) {
        for (Operator op : Operator.values()) {
            if (op.symbol.equals(token)) {
                return op;
            }
        }
        throw new IllegalArgumentException(("Unknown operator: " + token));
    }
}
