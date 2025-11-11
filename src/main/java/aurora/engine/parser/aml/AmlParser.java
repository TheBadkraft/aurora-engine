// src/main/java/aurora/engine/parser/aml/AmlParser.java
package aurora.engine.parser.aml;

import aurora.engine.parser.*;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;

public class AmlParser {
    private final String source;
    private final List<ParseError> errors = new ArrayList<>();
    private int pos = 0;
    private int line = 1;
    private int col = 1;

    private AmlParser(String source) {
        this.source = source;
    }

    public static ParseResult<List<NamedModel>> parse(Path path) {
        try {
            String content = Files.readString(path);
            AmlParser parser = new AmlParser(content);
            List<NamedModel> models = parser.parseDocument();
            return parser.errors.isEmpty()
                    ? ParseResult.success(models)
                    : ParseResult.failure(parser.errors);
        } catch (IOException e) {
            return ParseResult.failure(List.of(
                    new ParseError(0, 0, "IO error: " + e.getMessage())));
        }
    }

    private List<NamedModel> parseDocument() {
        List<NamedModel> models = new ArrayList<>();
        skipWhitespace();
        if (matchShebang())
            skipLine();
        while (!isAtEnd()) {
            models.add(parseNamedObject());
            skipWhitespace();
        }
        return models;
    }

    private NamedModel parseNamedObject() {
        String id = consumeId();
        Map<String, Model.Attribute> attrs = parseAttributes();
        Map<String, Object> fields = new LinkedHashMap<>();
        List<NamedModel> children = new ArrayList<>();
        consume('{');
        skipWhitespace();
        while (peek() != '}') {
            if (isAlpha(peek())) {
                String key = consumeId();
                expectSeq(":=");
                Object value = parseValue();
                fields.put(key, value);
            } else {
                children.add(parseNamedObject());
            }
            skipWhitespace();
        }
        consume('}');
        String fullId = "main:" + id;
        return new NamedModel(fullId, attrs, Map.copyOf(fields), List.copyOf(children));
    }

    private Map<String, Model.Attribute> parseAttributes() {
        Map<String, Model.Attribute> attrs = new LinkedHashMap<>();
        if (match('@') && match('[')) {
            while (peek() != ']') {
                String key = consumeId();
                String value = "unknown";
                if (match('='))
                    value = consumeId();
                attrs.put(key, new Model.Attribute(key, value));
                if (match(','))
                    continue;
            }
            consume(']');
        } else {
            attrs.put("type", new Model.Attribute("type", "unknown"));
        }
        return attrs;
    }

    private Object parseValue() {
        skipWhitespace();
        char c = peek();
        if (c == '"' || c == '\'')
            return consumeString(c);
        if (c == '#')
            return consumeHex();
        if (isDigit(c) || c == '-')
            return consumeNumber();
        if (match("true"))
            return true;
        if (match("false"))
            return false;
        if (match("null"))
            return null;
        if (match('['))
            return parseArray();
        if (match('{'))
            return parseInlineObject();
        if (isDigit(peek()) && match("..")) {
            int min = (Integer) consumeNumber();
            int max = (Integer) consumeNumber();
            return new int[] { min, max };
        }
        if (match('`'))
            return consumeEmbed();
        error("Invalid value");
        return null;
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        consume('[');
        skipWhitespace();
        if (peek() == ']') {
            consume(']');
            return list;
        }
        do {
            list.add(parseValue());
            skipWhitespace();
        } while (match(','));
        consume(']');
        return list;
    }

    private Map<String, Object> parseInlineObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        consume('{');
        skipWhitespace();
        while (peek() != '}') {
            String key = consumeId();
            expectSeq(":=");
            map.put(key, parseValue());
            match(',');
            skipWhitespace();
        }
        consume('}');
        return map;
    }

    // --- primitives ---
    private String consumeId() {
        StringBuilder sb = new StringBuilder();
        while (isAlphaNumeric(peek()))
            sb.append(advance());
        if (sb.isEmpty()) {
            error("Expected identifier");
            return "";
        }
        return sb.toString();
    }

    private String consumeString(char quote) {
        StringBuilder sb = new StringBuilder();
        advance();
        while (peek() != quote && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                col = 1;
            }
            sb.append(advance());
        }
        if (isAtEnd()) {
            error("Unterminated string");
            return "";
        }
        advance();
        return sb.toString();
    }

    private String consumeHex() {
        advance();
        StringBuilder sb = new StringBuilder("#");
        for (int i = 0; i < 6; i++) {
            char c = advance();
            if (!isHexDigit(c)) {
                error("Invalid hex");
                break;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private Number consumeNumber() {
        boolean negative = match('-');
        StringBuilder sb = new StringBuilder();
        if (negative)
            sb.append('-');
        while (isDigit(peek()))
            sb.append(advance());
        if (match('.') && isDigit(peek())) {
            sb.append('.');
            while (isDigit(peek()))
                sb.append(advance());
            return Double.parseDouble(sb.toString());
        }
        return Integer.parseInt(sb.toString());
    }

    private String consumeEmbed() {
        advance();
        StringBuilder sb = new StringBuilder();
        while (peek() != '`' && !isAtEnd())
            sb.append(advance());
        if (isAtEnd()) {
            error("Unterminated embed");
            return "";
        }
        advance();
        return sb.toString();
    }

    // --- helpers ---
    private boolean isAtEnd() {
        return pos >= source.length();
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(pos);
    }

    private char peekNext() {
        return pos + 1 >= source.length() ? '\0' : source.charAt(pos + 1);
    }

    private char advance() {
        col++;
        return source.charAt(pos++);
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(pos) != expected)
            return false;
        pos++;
        col++;
        return true;
    }

    private boolean match(String s) {
        if (source.regionMatches(pos, s, 0, s.length())) {
            pos += s.length();
            col += s.length();
            return true;
        }
        return false;
    }

    private void consume(char expected) {
        if (!match(expected))
            error("Expected '" + expected + "'");
    }

    private void expectSeq(String seq) {
        if (!match(seq))
            error("Expected '" + seq + "'");
    }

    private void skipWhitespace() {
        while (true) {
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\r')
                advance();
            else if (c == '\n') {
                line++;
                col = 1;
                advance();
            } else if (c == '/' && peekNext() == '/')
                skipLineComment();
            else if (c == '/' && peekNext() == '*')
                skipBlockComment();
            else if (c == '#') {
                if (matchShebang()) {
                    skipLine();
                    break;
                } else
                    skipLineComment();
            } else
                break;
        }
    }

    private void skipLineComment() {
        while (peek() != '\n' && !isAtEnd())
            advance();
    }

    private void skipBlockComment() {
        advance();
        advance();
        int nest = 1;
        while (nest > 0 && !isAtEnd()) {
            if (peek() == '*' && peekNext() == '/') {
                advance();
                advance();
                nest--;
            } else if (peek() == '/' && peekNext() == '*') {
                advance();
                advance();
                nest++;
            } else
                advance();
        }
    }

    private void skipLine() {
        while (peek() != '\n' && !isAtEnd())
            advance();
        if (!isAtEnd()) {
            line++;
            col = 1;
            advance();
        }
    }

    private boolean matchShebang() {
        int save = pos;
        int saveLine = line, saveCol = col;
        skipWhitespace();
        boolean matched = source.startsWith("#!aurora aml", pos);
        if (matched) {
            pos += "#!aurora aml".length();
            col += "#!aurora aml".length();
        } else {
            pos = save;
            line = saveLine;
            col = saveCol;
        }
        return matched;
    }

    private void error(String msg) {
        errors.add(new ParseError(line, col, msg));
        recover();
    }

    private void recover() {
        while (!isAtEnd() && peek() != '\n' && peek() != ';')
            advance();
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private static boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private static boolean isHexDigit(char c) {
        return isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
}