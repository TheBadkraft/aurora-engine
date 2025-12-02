/// src/main/java/dev/badkraft/anvil/utilities/Utils.java
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
package dev.badkraft.anvil.utilities;

import dev.badkraft.anvil.validators.ValidationResult;
import dev.badkraft.anvil.validators.Validators;

import java.io.IOException;
import java.nio.file.Path;

public class Utils {

    // Get file extension from Path
    public static String getFileExtension(Path path) {
        String fileName = path.getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return ""; // No extension found
        }
        return fileName.substring(lastDotIndex + 1);
    }

    public static String loadFile(Path path) throws IOException {
        ValidationResult result = Validators.validateFilePath(path.toString());
        if(result.getCode() == 0) {
            try {
                return java.nio.file.Files.readString(path);
            } catch (Exception e) {
                throw new RuntimeException("Failed to read file: " + path, e);
            }
        } else {
            throw new IOException("Invalid file path: " + path.toString());
        }
    }

    public static String createNamespaceFromPath(Path path) {
        // Simple namespace creation based on file path (valueBase) without extension
        String fileName = path.getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        String baseName = (lastDotIndex == -1) ? fileName : fileName.substring(0, lastDotIndex);
        return baseName + path.getParent().getFileName() + "." + baseName + "_" + genHash();
    }

    public static String createNamespace() {
        return "ns_" + genHash();
    }

    private  static String genHash() {
        // generate a simple hesadcimal (byte) hash, 9 digiets long
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            int digit = (int) (Math.random() * 16);
            sb.append(Integer.toHexString(digit));
        }
        return sb.toString();
    }
}
