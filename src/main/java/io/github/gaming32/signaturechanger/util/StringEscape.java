package io.github.gaming32.signaturechanger.util;

public class StringEscape {
    public static String unescape(String escaped) {
        int i = escaped.indexOf('\\');
        if (i == -1) {
            return escaped;
        }
        final StringBuilder result = new StringBuilder(escaped.length());
        int lastI = 0;
        while (i != -1) {
            result.append(escaped, lastI, i);
            if (i + 1 >= escaped.length()) {
                throw new IllegalArgumentException("Trailing \\ in string \"" + escaped + "\"");
            }
            final char c = escaped.charAt(i + 1);
            int skip = 1;
            switch (c) {
                case '"', '\'', '\\' -> result.append(c);
                case '0', '1', '2', '3', '4', '5', '6', '7' -> {
                    final char c1 = i + 2 < escaped.length() ? escaped.charAt(i + 2) : '\0';
                    final char c2 = i + 3 < escaped.length() ? escaped.charAt(i + 3) : '\0';
                    final int digit0 = Character.digit(c, 8);
                    final int digit1 = Character.digit(c1, 8);
                    final int digit2 = Character.digit(c2, 8);
                    if (digit1 == -1) {
                        result.append((char)digit0);
                    } else if (digit2 == -1 || digit0 > 3) {
                        result.append((char)(digit0 << 3 | digit1));
                        skip = 2;
                    } else {
                        result.append((char)(digit0 << 6 | digit1 << 3 | digit2));
                        skip = 3;
                    }
                }
                case 'u' -> {
                    if (i + 5 >= escaped.length()) {
                        throw new IllegalArgumentException("\\u escape overflows in string \"" + escaped + "\"");
                    }
                    result.append((char)Integer.parseInt(escaped, i + 2, i + 6, 16));
                    skip = 5;
                }
                case 'b' -> result.append('\b');
                case 'f' -> result.append('\f');
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                case 's' -> result.append(' ');
                case 't' -> result.append('\t');
                default -> throw new IllegalArgumentException("Unknown escape \\" + c + " in string \"" + escaped + "\"");
            }
            lastI = i + skip + 1;
            i = escaped.indexOf('\\', lastI);
        }
        result.append(escaped, lastI, escaped.length());
        return result.toString();
    }

    public static String escape(String unescaped) {
        if (unescaped.isEmpty()) {
            return "";
        }
        final StringBuilder result = new StringBuilder(unescaped.length() + 4);
        boolean escaped = false;
        for (int i = 0; i < unescaped.length(); i++) {
            final char c = unescaped.charAt(i);
            boolean didEscape = true;
            switch (c) {
                case '\\' -> result.append("\\\\");
                case 'b' -> result.append("\\b");
                case 'f' -> result.append("\\f");
                case 'n' -> result.append("\\n");
                case 'r' -> result.append("\\r");
                case 't' -> result.append("\\t");
                default -> {
                    if (c >= 32 && c < 127) {
                        result.append(c);
                        didEscape = false;
                    } else {
                        result.append("\\u").append(Integer.toHexString(0x10000 + c), 1, 5);
                    }
                }
            }
            escaped |= didEscape;
        }
        if (!escaped) {
            return unescaped;
        }
        return result.toString();
    }
}
