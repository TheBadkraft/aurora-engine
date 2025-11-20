// src/main/java/dev/badkraft/aurora/engine/parser/AuroraParser.java
package dev.badkraft.anvil.parser;

import dev.badkraft.anvil.*;
import dev.badkraft.anvil.Module;
import dev.badkraft.anvil.utilities.Utils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.*;
import java.io.IOException;
import java.util.*;

import static dev.badkraft.anvil.ErrorCode.*;
import static dev.badkraft.anvil.Operator.QUOTE;
import static dev.badkraft.anvil.ValueParseResult.failure;

public class AnvilParser {
    static final int MAX_SB_SIZE = 256;
    private static final Set<String> KEYWORDS = Set.of(
            "true", "false", "null", "vars", "include"
    );

    private final String source;
    private final StringBuilder sb = new StringBuilder(MAX_SB_SIZE);
    private final List<ParseError> errors = new ArrayList<>();
    private Dialect moduleDialect = Dialect.NONE;
    private int pos = 0;
    private int line = 1;
    private int col = 1;

    /**
     * Construct a new AuroraParser
     */
    private AnvilParser(Dialect hintDialect, String namespace, String source) {
        this.source = source;
        this.moduleDialect = parseDialect(hintDialect);
    }

    /**
     * Parser entry
     */
    public static ParseResult<dev.badkraft.anvil.Module> parse(Path path) {
        try {
            String content = Files.readString(path);
            Dialect hintDialect = Dialect.fromFileExtension(Utils.getFileExtension(path));
            String name = path.getFileName().toString().replaceFirst("[.][^.]+$", "");
            AnvilParser parser = new AnvilParser(hintDialect, name, content);
            dev.badkraft.anvil.Module module = new dev.badkraft.anvil.Module(name, parser.moduleDialect);
            parser.parseSource(module);

            return parser.errors.isEmpty()
                    ? ParseResult.success(module)
                    : ParseResult.failure(parser.errors);
        } catch (IOException e) {
            return ParseResult.failure(List.of(new ParseError(0, 0, ErrorCode.IO_ERROR)));
        }
    }

    // --- Main parser functions ---
    /** Parses dialect from shebang */
    private Dialect parseDialect(Dialect hint) {
        skipWhitespace();
        if (isShebang()) {
            String token = new String(consume(5));
            return Dialect.fromShebang(token);
        }
        return hint;
    }
    /** Module is our container for each file */
    private void parseSource(Module module) {
        //  1. skip whitespace
        skipWhitespace();
        while(!isEOF()) {
            //  2. parseStatement
            List<String> identifiers = new ArrayList<>();
            Statement statement = parseStatement(identifiers);
            module.addStatement(statement);
            module.addAllIdentifiers(identifiers);
        }
    }
    private Statement parseStatement(List<String> identifiers) {
        //  1. Expect an identifier
        int idLen = readIdentifier();
        if (idLen == 0) {
            error(ErrorCode.EXPECTED_IDENTIFIER, line, col);
        }
        String id = take(idLen);
        skipWhitespace();

        //  2. Look for attributes
        List<Attribute> attributes = List.of();

        //  3. Expect Operator.ASSIGN
        int opLen = readOperator(Operator.ASSIGN);
        if (opLen == 0) {
            error(ErrorCode.EXPECTED_ASSIGN, line, col);
        }
        consume(opLen);
        skipWhitespace();

        //  4. Expect value
        Value value = parseValue();
        skipWhitespace();

        //  5. Expect Operator.COMMA, NL, or EOF
        int termLen = readOperator(Operator.COMMA);
        if (termLen > 0) {
            consume(termLen);
            skipWhitespace();
        }

        //  6. Construct and return Assignment
        //  Add identifier now that we know statement is valid
        identifiers.add(id);
        return new Assignment(id, value, attributes);
    }
    private Value parseValue() {
        // 1. STRING
        if (is("\"")) {
            return parseString();
        }
        // 2. HEX VALUE
        if (is("#") || is("0x") || is("0X")) {
            // let's flag which kind and send to parser function
            return parseHexLiteral(is("#"));
        }
        // 3. Keywords — boolean and null
        if (is("true")) {
            consume(4);
            return new Value.BooleanValue(true);
        }
        if (is("false")) {
            consume(5);
            return new Value.BooleanValue(false);
        }
        if (is("null")) {
            consume(4);
            return new Value.NullValue();
        }
        // 4. BLOB - attribut is optional
        boolean hasAttrib = false;
        if ((hasAttrib = isOperator(Operator.AT)) || isOperator(Operator.BACKTICK)) {
            return parseBlob(hasAttrib);
        }
        //  5. BARE LITERAL (unquoted string)
        String bare = readBareLiteral();
        if (bare != null) {
            return new Value.BareLiteral(bare);
        }
        // 10. DECIMAL NUMBER - last chance fallback
        return parseDecimalNumber();
    }
    private Value parseString() {
        String fullSource = parseContent(Operator.QUOTE);
        return new Value.StringValue(fullSource);   // stores the whole "…"
    }
    private Value parseHexLiteral(boolean isHash) {
        int startLine = line;
        int startCol = col;

        // sanity check ... ???
        boolean is0x   = is("0x") || is("0X");
        if (!isHash && !is0x) throw new AssertionError();

        String prefix = isHash ? take(1) : take(2);  // consume "0x" or "0X"

        sb.setLength(0);
        boolean hasDigit = false;

        while (true) {
            char c = peek();
            if (isHexDigit(c)) {
                // Don't format the data, just collect it
                sb.append(consume());
                hasDigit = true;
            } else if (c == '_') {
                consume();
            } else {
                break;
            }
        }

        if (!hasDigit) {
            error(ErrorCode.INVALID_HEX_LITERAL, startLine, startCol);
        }

        String digits = sb.toString().replace("_", "");
        String fullSource = isHash ? "#" + digits : prefix + digits;

        long value = 0;
        try {
            value = Long.parseLong(digits, 16);
        } catch (NumberFormatException e) {
            error(ErrorCode.INVALID_HEX_LITERAL, startLine, startCol);
        }

        return isHash ?
                new Value.HexValue(value, fullSource) :
                new Value.LongValue(value, fullSource);
    }
    private Value parseDecimalNumber() {
        int startLine = line;
        int startCol = col;

        sb.setLength(0);
        boolean hasDigit = false;

        // Optional sign
        if (peek() == '+' || peek() == '-') {
            sb.append(consume());
        }

        // Integer part
        while (true) {
            char c = peek();
            if (c >= '0' && c <= '9') {
                sb.append(consume());
                hasDigit = true;
            } else if (c == '_') {
                consume();
            } else {
                break;
            }
        }

        boolean isFloat = false;

        // Fractional part
        if (peek() == '.') {
            sb.append(consume());
            isFloat = true;
            while (true) {
                char c = peek();
                if (c >= '0' && c <= '9') {
                    sb.append(consume());
                    hasDigit = true;
                } else if (c == '_') {
                    consume();
                } else {
                    break;
                }
            }
        }

        // Exponent
        if (peek() == 'e' || peek() == 'E') {
            sb.append(consume());
            isFloat = true;
            if (peek() == '+' || peek() == '-') {
                sb.append(consume());
            }
            boolean hasExpDigit = false;
            while (true) {
                char c = peek();
                if (c >= '0' && c <= '9') {
                    sb.append(consume());
                    hasExpDigit = true;
                } else if (c == '_') {
                    consume();
                } else {
                    break;
                }
            }
            if (!hasExpDigit) {
                error(ErrorCode.INVALID_EXPONENT, line, col);
            }
        }

        if (!hasDigit) {
            error(ErrorCode.INVALID_NUMBER, startLine, startCol);
        }

        String source = sb.toString();
        String clean = source.replace("_", "");

        try {
            if (isFloat) {
                double d = Double.parseDouble(clean);
                return new Value.DoubleValue(d, source);
            } else {
                long l = Long.parseLong(clean);
                return new Value.LongValue(l, source);
            }
        } catch (NumberFormatException e) {
            error(ErrorCode.INVALID_NUMBER, startLine, startCol);
        }
        return null; // unreachable
    }
    private Value parseBlob(boolean hasAttribute) {
        String attribute = null;
        int attrStart = -1;

        if (hasAttribute) {
            attrStart = pos;
            consume(1);                         // @
            int len = readIdentifier();
            if (len > 0) {
                attribute = take(len);
                if (KEYWORDS.contains(attribute))
                    error(ATTRIBUTE_IS_KEYWORD, line, col);
            }
        }

        String blobSource = parseContent(Operator.BACKTICK);   // includes the backticks

        // If there was an @attribute, prepend it to the source text
        if (attribute != null) {
            String attrPart = source.substring(attrStart, pos - blobSource.length());
            blobSource = attrPart + blobSource;
        }

        return new Value.BlobValue(blobSource, attribute);
    }
    private String parseContent(Operator delimiter) {
        if (delimiter != QUOTE && delimiter != Operator.BACKTICK) {
            error(ErrorCode.UNEXPECTED_TOKEN, line, col);
        }

        int start = pos;                     // <-- remember where we started
        consumeOperator(delimiter);          // consume opening " or `

        while (!isEOF()) {
            if (isOperator(delimiter) && !isEscaped(pos)) {
                break;                       // found unescaped closing delimiter
            }
            consume();                       // normal char or escaped sequence
        }

        if (!isOperator(delimiter)) {
            error(delimiter == QUOTE ? UNTERMINATED_STRING : UNTERMINATED_BLOB, line, col);
        }
        consumeOperator(delimiter);          // consume closing " or `

        // Return the exact text we just parsed (including delimiters)
        return source.substring(start, pos);
    }

    // --- Utility interrogators ---
    /*
        Interrogators - peek, is*, read* -  are passive and do not
        consume (advance) the position in the stream.
     */
    private boolean isEOF() { return isEOF(0); }
    private boolean isEOF(int offset) {return pos + offset >= source.length();}
    private char peek() { return peek(0); }
    private char peek(int offset) {
        int index = pos + offset;
        return index < source.length() ? source.charAt(index) : '\0';
    }
    private boolean is(String s) {
        return is(s, 0);
    }
    private boolean is(String s, int offset) {
        for (int i = 0; i < s.length(); i++) {
            if(peek(i + offset) != s.charAt(i)) return false;
        }
        return true;
    }
    private boolean isShebang() {
        return is("#!asl") || is("#!aml");
    }
    private boolean isAlpha(char c) {
        return Character.isLetter(c) || c == '_';
    }
    private boolean isAlphaNumeric(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
    private static boolean isDigit(char c) { return c >= '0' && c <= '9'; }
    private static boolean isHexDigit(char c) {
        return isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
    private boolean isOperator(Operator op) { return is(op.symbol()); }
    private boolean isKeyword(String s) { return KEYWORDS.contains(s); }
    private boolean isEscaped(int pos) {
        if (pos <=0) return false;

        int backslashes = 0;
        for (int i = pos - 1; i >= 0 && source.charAt(i) == '\\'; i--) backslashes++;
        return backslashes % 2 == 1;
    }
    private int readIdentifier() {
        int len = 0;
        if (!isEOF() && isAlpha(peek())) {
            while (!isEOF() && isAlphaNumeric(peek(len))) len++;
        }
        return len;
    }
    private String readBareLiteral() {
        int length = 0;
        char first = peek(length);
        if (!Character.isLetter(first) && first != '_') return null;
        length++;

        while (true) {
            char c = peek(length);
            if (c == '\0') break;
            if (Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == ':') {
                length++;
            } else {
                break;
            }
        }

        if (length == 0) return null;
        return take(length); // consumes + returns
    }
    private int readToEOL() {
        int len = 0;
        while (!isEOF() && peek() != '\n') len++;
        // we want to include EOL (`\n`)
        len++;
        return len;
    }
    private int readWhitespace() {
        int len = 0;
        while (!isEOF(len)) {
            char c = peek(len);
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') len++;
            else if (is("//", len)) len += readLineComment(len);
            else if (is("/*", len)) len += readBlockComment(len);
            else break;
        }
        return len;
    }
    private int readLineComment() { return readLineComment(0); }
    private int readLineComment(int offset) {
        int len = offset;
        if (peek(len) == '/' && peek(len + 1) == '/') {
            len += 2;
            while(!isEOF(len) && peek(len) != '\n') len++;
        }
        return len - offset;
    }
    private int readBlockComment() { return readBlockComment(0); }
    private int readBlockComment(int offset) {
        int len = offset;
        int depth = 0;
        if (peek(len) == '/' && peek(len + 1) == '*') {
            len += 2;
            depth++;

            while (depth > 0 && !isEOF(len)) {
                if (peek(len) == '*' && peek(len + 1) == '/') {
                    len += 2;
                    depth--;
                }
                else if (peek(len) == '/' && peek(len + 1) == '*') {
                    len += 2;
                    depth++;
                }
                else len++;
            }
        }
        return len - offset;
    }
    private int readOperator(Operator op) {
        int len = 0;
        if (isOperator(op)) len = op.length();
        return len;
    }
    // --- Utility consumers ---
    /*
        Consumers - skip*, consume* - advance the position in the stream. All
        consumers not taking an INT parameter should 'read*' first to verify
        what is being consumed and receive a non-negative, non-zero (>0) result.
     */
    private char consume() {
        if (isEOF()) return '\0';
        char c = source.charAt(pos++);
        if (c == '\n') { line++; col = 1; } else col++;
        return c;
    }
    private char @NotNull [] consume(int n) {
        char[] buf = new char[n];
        for (int i = 0; i < n; i++) buf[i] = consume();
        return buf;
    }
    private void consumeOperator(Operator op) {
        consume(op.length());
    }
    private String take(int length) {
        if (length <= 0) return "";
        sb.setLength(0);
        sb.append(source, pos, pos + length);
        consume(length);
        return sb.toString();
    }
    // although not used we are keeping it for artifact completeness
    private @NotNull String matchText(Operator delimiter) {
        if (delimiter != QUOTE && delimiter != Operator.BACKTICK) {
            error(ErrorCode.UNEXPECTED_TOKEN, line, col);
        }
        sb.setLength(0);
        while (!isEOF() && !isOperator(delimiter)) {
            char c = consume();
            if (c == '\\') {
                char next = peek();
                sb.append(switch (next) {
                    case 'n' -> { consume(); yield '\n'; }
                    case 't' -> { consume(); yield '\t'; }
                    case 'r' -> { consume(); yield '\r'; }
                    case '\\', '"' -> { consume(); yield next; }
                    default -> c;
                });
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    private void skipWhitespace() {
        consume(readWhitespace());
    }
    private void skipLineComment() {
        consume(readLineComment());
    }
    private void skipBlockComment() {
        consume(readBlockComment());
    }

    // --- Utility error & recovery functions ---
    /** report the error */
    private void error(ErrorCode code, int line, int col) {
        String msg = code.message();
        throw new ParseException(code, line, col, msg);
        // never recover here because we don't know what kind of recovery
    }
    /** generic recovery to start of next line */
    private void recover() {
        consume(readToEOL());
    }
}
