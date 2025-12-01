/// src/main/java/dev/badkraft/anvil/validators/Validators.java
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
package dev.badkraft.anvil.validators;

public class Validators {
    public static ValidationResult validateFilePath(String path) {
        // use FileStatus to validate the file path
        if (path == null) {
            return new ValidationResult(FileStatus.NULL.getCode(),
                    FileStatus.NULL.formatMessage(null));
        }
        if (path.isEmpty()) {
            return new ValidationResult(FileStatus.EMPTY.getCode(),
                    FileStatus.EMPTY.formatMessage(path));
        }
        java.nio.file.Path filePath = java.nio.file.Path.of(path);
        if (!java.nio.file.Files.exists(filePath)) {
            return new ValidationResult(FileStatus.NOT_FOUND.getCode(),
                    FileStatus.NOT_FOUND.formatMessage(path));
        }
        return new ValidationResult(FileStatus.SUCCESS.getCode(),
                FileStatus.SUCCESS.formatMessage(path));
    }
}
