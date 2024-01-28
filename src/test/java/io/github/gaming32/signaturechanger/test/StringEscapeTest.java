package io.github.gaming32.signaturechanger.test;

import org.junit.jupiter.api.Test;

import static io.github.gaming32.signaturechanger.util.StringEscape.unescape;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringEscapeTest {
    @Test
    public void testUnescape() {
        assertEquals("test", unescape("test"));

        // Octal
        assertEquals(" 2", unescape("\\402"));
        assertEquals("~", unescape("\\176"));
        assertEquals("\n", unescape("\\12"));
        assertEquals("\ntest", unescape("\\12test"));
        assertEquals("\u0000", unescape("\\0"));

        // Unicode
        assertEquals("\u0000", unescape("\\u0000"));
        assertEquals("\u0001", unescape("\\u0001"));

        // Other
        assertEquals("\\", unescape("\\\\"));
        assertEquals("\"", unescape("\\\""));
        assertEquals("'", unescape("\\'"));
        assertEquals("\b", unescape("\\b"));
        assertEquals("\u000C", unescape("\\f"));
        assertEquals("\n", unescape("\\n"));
        assertEquals("\r", unescape("\\r"));
        assertEquals(" ", unescape("\\s"));
        assertEquals("\t", unescape("\\t"));
    }
}
