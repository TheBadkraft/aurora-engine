// src/test/java/aurora/engine/parser/FileTest.java
package aurora.engine.parser;

// import aurora.engine.parser.aml.AmlParser;
// import aurora.engine.parser.ParseResult;
// import java.nio.file.*;

import aurora.engine.validators.ValidationResult;
import aurora.engine.validators.Validators;


public class FileTest {
    public static void main(String[] args) throws Exception {
        String filePath = args.length > 0 ? args[0] : null;

        // Validate file path
        ValidationResult validationResult =
                Validators.validateFilePath(filePath);

        switch (validationResult.getCode()) {
            case 0:
                System.out.println("File is valid: " + validationResult.getMessage());
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

        // ParseResult<?> result = AmlParser.parse(path); // lexer-less
        // if (!result.errors().isEmpty()) {
        // System.out.printf("FAILED — %d error(s)%n", result.errors().size());
        // result.errors().forEach(e -> System.err.printf(" [%d:%d] %s%n", e.line(),
        // e.col(), e.message()));
        // return;
        // }

        // System.out.println("PASS");
        // System.out.println(result.result());
    }
}
