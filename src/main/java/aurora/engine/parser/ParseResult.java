// src/main/java/aurora/engine/parser/ParseResult.java
package aurora.engine.parser;

import java.util.List;
import java.util.Collections;

public record ParseResult<T>(
        T result,
        List<ParseError> errors) {
    public static <T> ParseResult<T> success(T result) {
        return new ParseResult<>(result, Collections.emptyList());
    }

    public static <T> ParseResult<T> failure(List<ParseError> errors) {
        return new ParseResult<>(null, List.copyOf(errors));
    }
}