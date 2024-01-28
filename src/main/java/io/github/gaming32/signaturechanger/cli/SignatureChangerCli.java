package io.github.gaming32.signaturechanger.cli;

import io.github.gaming32.signaturechanger.generator.SigsClassGenerator;
import io.github.gaming32.signaturechanger.visitor.SigsFileWriter;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.ext.java7.PathArgumentType;
import net.sourceforge.argparse4j.impl.type.EnumStringArgumentType;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class SignatureChangerCli {
    private static final String GENERATE = "generate";
    private static final int READER_FLAGS = ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG;

    public static void main(String[] args) throws IOException {
        final ArgumentParser parser = ArgumentParsers.newFor("signature-changer")
            .fromFilePrefix("@")
            .build()
            .defaultHelp(true)
            .description("A CLI/library");
        final Subparsers subparsers = parser.addSubparsers().dest("action");

        final Subparser generate = subparsers.addParser(GENERATE);
        generate.addArgument("-e", "--empty-mode")
            .type(new EnumStringArgumentType<>(SigsClassGenerator.EmptySignatureMode.class))
            .setDefault(SigsClassGenerator.EmptySignatureMode.IGNORE)
            .help("What to do when a signature is absent");
        generate.addArgument("classes")
            .type(new PathArgumentType().verifyExists())
            .nargs("+")
            .help("The directories or jars to load classes from");

        final var parsedArgs = new Object() {
            @Arg
            String action;

            //////////////////////
            // Action: generate //
            //////////////////////
            @Arg(dest = "empty_mode")
            SigsClassGenerator.EmptySignatureMode emptyMode;
            @Arg
            List<Path> classes;
        };

        try {
            parser.parseArgs(args, parsedArgs);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        switch (parsedArgs.action) {
            case GENERATE -> generate(parsedArgs.classes, parsedArgs.emptyMode);
            default -> throw new AssertionError("Unimplemented action " + parsedArgs.action);
        }
    }

    private static void generate(
        List<Path> classes, SigsClassGenerator.EmptySignatureMode emptyMode
    ) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
        final SigsClassGenerator generator = new SigsClassGenerator(new SigsFileWriter(writer), emptyMode);
        for (Path origin : classes) {
            System.err.println("Generating sigs data for " + origin);
            FileSystem fs = null;
            if (Files.isRegularFile(origin)) {
                fs = FileSystems.newFileSystem(origin);
                origin = fs.getRootDirectories().iterator().next();
            }
            try (Stream<Path> stream = Files.find(
                origin, Integer.MAX_VALUE, (p, a) -> a.isRegularFile() && p.toString().endsWith(".class")
            )) {
                stream.forEach(path -> {
                    try (InputStream is = Files.newInputStream(path)) {
                        final ClassReader reader = new ClassReader(is);
                        if (!hasSignatures(reader)) return;
                        reader.accept(generator, READER_FLAGS);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
            if (fs != null) {
                fs.close();
            }
            writer.flush();
        }
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
