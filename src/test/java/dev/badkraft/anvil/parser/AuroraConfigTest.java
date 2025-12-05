/// src/test/java/dev/badkraft/anvil/parser/null.java
///
/// Copyright (c) 2025 Quantum Override. All rights reserved.
/// Author: The Badkraft
/// Date: 12 02, 2025
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
package dev.badkraft.anvil.parser;

import dev.badkraft.anvil.api.Anvil;
import dev.badkraft.anvil.api.root;
import dev.badkraft.anvil.core.data.Dialect;
import dev.badkraft.anvil.data.object;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AuroraConfigTest {

    @Test
    void loadConfig() throws Exception {
        Session session = loadSession();

        assertNotNull(session);
    }

    private record Session(
            String accessToken,
            String username,
            String uuid,
            String clientId,
            String xuId) {}
    private static Session loadSession() throws Exception {
        Path config = Paths.get("/home/badkraft/repos/aurora-mvp/config.aurora");
        // if config.aurora does not exist, perform login
        if (!Files.exists(config)) {
            System.out.println("No login found. Starting MS Login ...");
            //MinecraftAuth.loginAndSave(); // TODO: Re-enable when ready
        }

        // and the file will be created
        root r = Anvil.load(config, Dialect.AML,"aurora.config").parse();
        object auth = r.get("auth").asObject();

        long expiresAt = auth.get("expires_at").asLong();
        long now = System.currentTimeMillis() / 1000;
        if (now >= expiresAt) {
            System.out.println("Token expired. Refreshing...");
            //MinecraftAuth.refreshSession();
            // this is where we need to be able to update the value and save
            //content = Files.readString(config); // Re-read after refresh
        }

        return new Session(
                auth.get("access_token").asString(),
                auth.get("username").asString(),
                auth.get("uuid").asString(),
                auth.get("client_id").asString(),
                auth.get("xuid").asString()
        );
    }
}
