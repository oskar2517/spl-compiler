package me.oskar.spl.prelude;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Prelude {

    private static String readResourceAsString(String resourceName) {
        try (var inputStream = Prelude.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceName);
            }
            try (var scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
                scanner.useDelimiter("\\A");
                return scanner.hasNext() ? scanner.next() : "";
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading resource", e);
        }
    }

    public static String readNodeJsPrelude(boolean headless) {
        var s = readResourceAsString("prelude/nodejs.js");
        if (!headless) {
            s += readResourceAsString("prelude/nodejs_rendering.js");
        }

        return s;
    }
}
