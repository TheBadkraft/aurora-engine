// src/main/java/aurora/engine/parser/ParseError.java
package aurora.engine.parser;

public record ParseError(int line, int col, String message) {
}