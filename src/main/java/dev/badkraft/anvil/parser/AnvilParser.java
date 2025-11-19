// src/main/java/dev/badkraft/aurora/engine/parser/AuroraParser.java
package dev.badkraft.anvil.parser;

import dev.badkraft.anvil.*;
import dev.badkraft.anvil.Module;
import dev.badkraft.anvil.utilities.Utils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.*;
import java.io.IOException;
import java.util.*;

import static dev.badkraft.anvil.ValueParseResult.failure;

public class AnvilParser {
    public static final Set<String> KEYWORDS = Set.of(
            "true", "false", "null", "vars", "include"
    );

    private final String source;
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
        Statement statement = null;
        //  1. Expect an identifier
        int idLen;
        if ((idLen = readIdentifier()) == 0) {
            error(ErrorCode.EXPECTED_IDENTIFIER, line, col);
        }
        String id = consumeIdentifier(idLen);
        skipWhitespace();
        //  2. Look for attributes
        List<Attribute> attributes = List.of();
        //  3. Expect Operator.ASSIGN
        if (!consumeOperator(Operator.ASSIGN)) {
            error(ErrorCode.EXPECTED_ASSIGN, line, col);
        }
        skipWhitespace();
        //  4. Expect value

        // just return true -- will return check for valid parseValue
        if(true) {
            // assume we have a value and a complete statement
            identifiers.add(id);
            statement = new Assignment(id, null, attributes);
        }

        return statement;
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
    private boolean isOperator(Operator op) { return is(op.symbol()); }
    private boolean isKeyword(String s) { return KEYWORDS.contains(s); }
    private int readIdentifier() {
        int len = 0;
        if (!isEOF() && isAlpha(peek())) {
            while (!isEOF() && isAlphaNumeric(peek(len))) len++;
        }

        return len;
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
    private String consumeIdentifier(int length) {
        // although 'is*' validation is assumed, verify length is `>0`
        if (length > 0) {
            return new String(consume(length));
        }
        // else return empty
        return "";
    }
    private boolean consumeOperator(Operator op) {
        consume(op.length());
        return true;
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
        errors.add(new ParseError(line, col, code));
        // never recover here because we don't know what kind of recovery
    }
    /** generic recovery to start of next line */
    private void recover() {
        consume(readToEOL());
    }
}
