// src/test/java/aurora/engine/parser/FileTest.java
package aurora.engine.parser;

import java.nio.file.*;
import java.util.stream.Collectors;

import aurora.engine.validators.ValidationResult;
import aurora.engine.validators.Validators;


public class FileTest {
    public static void main(String[] args) {
        String filePath = args.length > 0 ? args[0] : null;

        // Validate file path
        ValidationResult validationResult =
                Validators.validateFilePath(filePath);
        Path path;

        switch (validationResult.getCode()) {
            case 0:
                System.out.println(validationResult.getMessage());
                assert filePath != null;    // we know it's not null here
                path = Paths.get(filePath);

                break;
            case 1:
            case 2:
            case 3:
                System.err.println("File validation failed: " + validationResult.getMessage());
                return;
            default:
                System.err.println("Unknown validation result code: " + validationResult.getCode());
                return;
        }

        long start = System.nanoTime();
        ParseResult<?> result = AuroraParser.parse(path); // lexer-less
        long parseTime = System.nanoTime() - start;

        var module = (Module)result.result();
        if (module == null) {
            // print errors if any
            if (!result.errors().isEmpty()) {
                System.out.printf("FAILED — %d error(s)%n", result.errors().size());
                result.errors().forEach(e -> System.err.printf(" [%d:%d] %s%n", e.line(),
                    e.col(), e.message()));
            }
            else {
                System.out.println("Module failed to parse.");
            }

            return;
        }
        if (module.isEmpty()) {
            System.out.println("Module is empty.");
            return;
        }
        if (module.isParsed()) {
            System.out.println("Module [" + module.namespace() + "] is parsed.");
            if (module.hasNoDialect()) {
                System.out.println("Module dialect could not be determined.");
            } else {
                System.out.println("Module dialect: " + module.getDialect());
            }
            prettyPrintModule(module);
        }

        System.out.printf("Parse time: %.3f ms%n", parseTime / 1_000_000.0);
        if (!result.errors().isEmpty()) {
            System.out.printf("FAILED — %d error(s)%n", result.errors().size());
            result.errors().forEach(e -> System.err.printf(" [%d:%d] %s%n", e.line(),
                e.col(), e.message()));
            return;
        }

        System.out.println(result);
    }

    // Add this to FileTest.java
    private static void prettyPrintModule(Module module) {
        System.out.println("Module is parsed.");
        System.out.println("Module dialect: " + module.getDialect());
        System.out.println();

        if (module.hasStatements()) {
            System.out.println("Statements:");
            for (Statement stmt : module.statements()) {
                System.out.println(prettyPrintStatement(stmt, 0));
            }
        }
    }

    private static String prettyPrintStatement(Statement stmt, int indent) {
        String pad = "  ".repeat(indent);
        StringBuilder sb = new StringBuilder();

        sb.append(pad).append(stmt.identifier());

        if (!stmt.attributes().isEmpty()) {
            sb.append(" @[")
                    .append(stmt.attributes().stream()
                            .map(FileTest::formatAttribute)
                            .collect(Collectors.joining(", ")))
                    .append("]");
        }

        sb.append(" := ");

        // Use source text if available (preserves original formatting)
        if (stmt.value() instanceof Value.ObjectValue obj && obj.source() != null) {
            sb.append(obj.source());
        } else if (stmt.value() instanceof Value.ArrayValue arr && arr.source() != null) {
            sb.append(arr.source());
        } else {
            sb.append(prettyPrintValue(stmt.value(), indent));
        }

        return sb.toString();
    }

    private static String formatAttribute(Attribute attr) {
        if (attr.value() == null) return attr.key();
        return attr.key() + "=" + prettyPrintValue(attr.value(), 0);
    }

    // In FileTest.java
    private static String prettyPrintValue(Value value, int indent) {
        String pad = "  ".repeat(indent);
        String inner = "  ".repeat(indent + 1);

        return switch (value) {
            case Value.NullValue ignored -> "null";
            case Value.BooleanValue b -> Boolean.toString(b.value());
            case Value.NumberValue n  -> n.source() != null ? n.source() : Double.toString(n.value());
            case Value.StringValue s  -> "\"" + s.value()
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t") + "\"";
            case Value.BareLiteral b  -> b.id();
            case Value.FreeformValue f -> (f.attribute() != null ? "@" + f.attribute() : "@") + f.content();

            case Value.ArrayValue a -> {
                if (a.source() != null) yield a.source();
                if (a.elements().isEmpty()) yield "[]";
                String joined = a.elements().stream()
                        .map(e -> prettyPrintValue(e, indent + 1))
                        .collect(Collectors.joining(",\n" + inner));
                yield "[\n" + inner + joined + "\n" + pad + "]";
            }

            case Value.ObjectValue o -> {
                if (o.source() != null) yield o.source();
                if (o.fields().isEmpty()) yield "{}";
                String joined = o.fields().stream()
                        .map(e -> e.getKey() + " := " + prettyPrintValue(e.getValue(), indent + 1))
                        .collect(Collectors.joining(",\n" + inner));
                yield "{\n" + inner + joined + "\n" + pad + "}";
            }

            default -> "<unknown>";
        };
    }
}
