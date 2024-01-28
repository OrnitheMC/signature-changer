package io.github.gaming32.signaturechanger.visitor;

import io.github.gaming32.signaturechanger.SignatureMode;
import io.github.gaming32.signaturechanger.util.StringEscape;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SigsReader implements Closeable {
    private final BufferedReader reader;

    public SigsReader(BufferedReader reader) {
        this.reader = reader;
    }

    public void accept(SigsFileVisitor visitor) throws IOException {
        boolean inClass = false;
        SigsClassVisitor classVisitor = null;

        int lineNumber = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (line.isEmpty()) continue;
            if (line.startsWith("\t")) {
                if (!inClass) {
                    throw new IllegalArgumentException("Unexpected indent on line \"" + line + "\"");
                }
                final String[] parts = line.split("\t", -1);
                if (parts.length < 3) {
                    throw new IllegalArgumentException("Member info must have at least name and desc");
                }
                final String name = StringEscape.unescape(parts[1]);
                if (name.isBlank()) {
                    throw new IllegalArgumentException("Member name \"" + name + "\" cannot be blank");
                }
                final String desc = StringEscape.unescape(parts[2]);
                if (desc.isBlank()) {
                    throw new IllegalArgumentException("Member descriptor \"" + desc + "\" cannot be blank");
                }
                SignatureMode signatureMode;
                String newSignature = "";
                if (parts.length == 3) {
                    signatureMode = SignatureMode.KEEP;
                } else if (parts.length == 4) {
                    signatureMode = parts[3].equals(".") ? SignatureMode.REMOVE : SignatureMode.MODIFY;
                    newSignature = StringEscape.unescape(parts[3]);
                } else {
                    throw new IllegalArgumentException(
                        "Trailing parts on line " + lineNumber + ": " +
                            Arrays.stream(parts).skip(3).collect(Collectors.joining("\t"))
                    );
                }
                if (classVisitor != null) {
                    classVisitor.visitMember(name, desc, signatureMode, newSignature);
                }
                continue;
            } else if (classVisitor != null) {
                classVisitor.visitEnd();
            }
            final String[] parts = line.split("\t", -1);
            final String className = StringEscape.unescape(parts[0]);
            if (className.isBlank()) {
                throw new IllegalArgumentException("Class name \"" + className + "\" cannot be blank");
            }
            SignatureMode signatureMode;
            String newSignature = "";
            if (parts.length == 1) {
                signatureMode = SignatureMode.KEEP;
            } else if (parts.length == 2) {
                signatureMode = parts[1].equals(".") ? SignatureMode.REMOVE : SignatureMode.MODIFY;
                newSignature = StringEscape.unescape(parts[1]);
            } else {
                throw new IllegalArgumentException(
                    "Trailing parts on line " + lineNumber + ": " +
                        Arrays.stream(parts).skip(2).collect(Collectors.joining("\t"))
                );
            }
            inClass = true;
            classVisitor = visitor.visitClass(className, signatureMode, newSignature);
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
