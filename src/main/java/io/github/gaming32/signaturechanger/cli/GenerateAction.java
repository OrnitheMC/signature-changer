package io.github.gaming32.signaturechanger.cli;

import io.github.gaming32.signaturechanger.generator.SigsClassGenerator;
import io.github.gaming32.signaturechanger.visitor.SigsFileWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public class GenerateAction {
    public static final String NAME = "generate";
    private static final int READER_FLAGS = ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG;

    public static void run(List<Path> classes, SigsClassGenerator.EmptySignatureMode emptyMode) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
        final SigsClassGenerator generator = new SigsClassGenerator(new SigsFileWriter(writer), emptyMode);
        SignatureChangerCli.iterateClasses(
            classes,
            origin -> System.err.println("Generating sigs data for " + origin),
            origin -> writer.flush(),
            (path, reader) -> {
                if (hasSignatures(reader)) {
                    reader.accept(generator, READER_FLAGS);
                }
            }
        );
    }

    private static boolean hasSignatures(ClassReader reader) {
        try {
            reader.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    if (signature != null) {
                        throw HasSignatures.INSTANCE;
                    }
                }

                @Override
                public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                    if (signature != null) {
                        throw HasSignatures.INSTANCE;
                    }
                    return null;
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    if (signature != null) {
                        throw HasSignatures.INSTANCE;
                    }
                    return null;
                }
            }, READER_FLAGS);
        } catch (HasSignatures e) {
            return true;
        }
        return false;
    }

    private static final class HasSignatures extends RuntimeException {
        private static final HasSignatures INSTANCE = new HasSignatures();

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
