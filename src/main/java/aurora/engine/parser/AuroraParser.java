// src/main/java/aurora/engine/parser/AmlParser.java
package aurora.engine.parser;

import aurora.engine.parser.aml.Model;
import aurora.engine.parser.aml.NamedModel;
import aurora.engine.utilities.Utils;

import java.nio.file.*;
import java.io.IOException;
import java.util.*;

public class AuroraParser {
    private final String source;
    private final List<ParseError> errors = new ArrayList<>();
    private int pos = 0;
    private int line = 1;
    private int col = 1;

    private AuroraParser(String source) {
        this.source = source;
    }

    public static ParseResult<AuroraDocument> parse(Path path) {
        try {
            String content = Files.readString(path);
            Dialect dialect = Dialect.fromFileExtension(
                    Utils.getFileExtension(path));
            AuroraParser parser = new AuroraParser(content);
            /*
                here's the problem - before we can start getting models, we need to validate the document:
                - shebang (optional) but must be first line if present
                - if no shebang, we can still proceed, but file extension (.aml or .asl)
                  can be a hint, otherwise we assume #!asl as least restrictive
                - then we start parsing top-level statement (e.g., assignments, model definitions, etc.)
             */
            var doc = parser.parseDocument(dialect);
            return parser.errors.isEmpty()
                    ? ParseResult.success(doc)
                    : ParseResult.failure(parser.errors);
        } catch (IOException e) {
            return ParseResult.failure(List.of(
                    new ParseError(0, 0, "IO error: " + e.getMessage())));
        }
    }

    /*
        Now we have a dialect hint passed from the file extension. So if the shebang is missing,
        we can assume the dialect from the file extension. If the shebang is present, it overrides
        the file extension hint.

        What if the shebang doesn't match the file extension? We can either:
        - throw an error
        - warn and proceed with shebang dialect

        Another option is to ignore the shebang and use the file extension dialect ... except, since
        we are calling the file extension a hint, we should probably let the shebang take precedence.
     */
    private AuroraDocument parseDocument(Dialect hint) {
        // matchShebang skips leading whitespace and checks for shebang
        Optional<Dialect> shebang;
        // check for shebang
        if (matchShebang()) {
            String token = source.substring(pos - 5, pos); // "#!aml" or "#!asl"
            shebang = Optional.of(Dialect.fromShebang(token));
        } else {
            shebang = Optional.of(hint);
        }

        var doc = new AuroraDocument();
        doc.isParsed = true;
        doc.dialect = shebang.orElse(null);

        return doc;
    }

    // --- helpers ---
    private boolean isEOF() {
        return pos >= source.length();
    }

    private char peek() {
        return isEOF() ? '\0' : source.charAt(pos);
    }

    private char peekNext() {
        int next = pos + 1;
        return next >= source.length() ? '\0' : source.charAt(next);
    }

    private char advance(int n) {
        char last = '\0';
        for (int i = 0; i < n; i++) {
            last = advance();
        }
        return last;
    }

    private char advance() {
        char c = source.charAt(pos++);
        if (c == '\n') {
            line++;
            col = 1;
        } else {
            col++;
        }
        return c;
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
            else
                break;
        }
    }

    private void skipLineComment() {
        while (peek() != '\n' && !isEOF())
            advance();
    }

    private void skipBlockComment() {
        advance();
        advance();
        int nest = 1;
        while (nest > 0 && !isEOF()) {
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

    private boolean matchShebang() {
        int save = pos;
        int saveLine = line, saveCol = col;
        skipWhitespace();
        boolean matched = source.startsWith("#!", pos);
        // check for either #!asl or #!aml
        if (matched) {
            // expect either #!asl or #!aml
            if (!isExpected("#!asl") && !isExpected("#!aml")) {
                error("Invalid shebang. Expected '#!asl' or '#!aml'.");
                matched = false;
            }
        } else {
            pos = save;
            line = saveLine;
            col = saveCol;
        }

        return matched;
    }

    private boolean isExpected(String s) {
        // match string s at current position
        int save = pos;
        for (int i = 0; i < s.length(); i++) {
            if (isEOF() || peek() != s.charAt(i)) {
                pos = save;
                return false;
            }
            advance();
        }
        return true;
    }

    private void error(String msg) {
        errors.add(new ParseError(line, col, msg));
        recover();
    }

    private void recover() {
        while (!isEOF() && peek() != '\n' && peek() != ';')
            advance();
    }
}