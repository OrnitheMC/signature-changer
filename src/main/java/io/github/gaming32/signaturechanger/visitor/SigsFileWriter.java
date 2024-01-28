package io.github.gaming32.signaturechanger.visitor;

import io.github.gaming32.signaturechanger.SignatureMode;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

public class SigsFileWriter extends SigsFileVisitor implements Closeable {
    private final Writer writer;
    private final StringBuilder builder = new StringBuilder();

    public SigsFileWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public SigsClassVisitor visitClass(String className, SignatureMode signatureMode, String signature) throws UncheckedIOException {
        try {
            builder.setLength(0);
            builder.append(className);
            if (signatureMode == SignatureMode.REMOVE) {
                builder.append("\t.");
            } else if (signatureMode == SignatureMode.MODIFY) {
                builder.append('\t').append(signature);
            }
            writer.write(builder.append('\n').toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new SigsClassVisitor() {
            @Override
            public void visitMember(String name, String desc, SignatureMode signatureMode, String signature) {
                try {
                    builder.setLength(0);
                    builder.append('\t').append(name).append('\t').append(desc);
                    if (signatureMode == SignatureMode.REMOVE) {
                        builder.append("\t.");
                    } else if (signatureMode == SignatureMode.MODIFY) {
                        builder.append('\t').append(signature);
                    }
                    writer.write(builder.append('\n').toString());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
